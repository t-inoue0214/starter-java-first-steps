/**
 * 【なぜこのコードを学ぶのか】
 * SQLインジェクションは「OWASP Top 10」で長年1位に君臨してきた脆弱性。
 * ユーザー入力を文字列連結でSQLに埋め込むと、攻撃者に任意のSQLを実行される。
 * PreparedStatement を使うとプレースホルダーでパラメータを安全にバインドし、
 * インジェクションを原理的に防げることを実行で体験する。
 */
package com.example.database_jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLインジェクション脆弱性と PreparedStatement による防御の対比デモ。
 *
 * <p>実行方法:</p>
 * <pre>
 * javac -d out/ -cp lib/h2.jar src/main/java/com/example/database_jdbc/SqlInjectionDemo.java
 * java -cp "out/:lib/h2.jar" com.example.database_jdbc.SqlInjectionDemo
 * </pre>
 */
public class SqlInjectionDemo {

    // ---------------------------------------------------------
    // DB接続設定（H2 インメモリDB）
    // DB_CLOSE_DELAY=-1 は「全接続が閉じてもDBを保持する」設定
    // ---------------------------------------------------------
    private static final String JDBC_URL = "jdbc:h2:mem:injection_demo;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            // ---------------------------------------------------------
            // セットアップ: テーブル作成とサンプルデータ投入
            // ---------------------------------------------------------
            setup();

            System.out.println("========== Before: Statement（文字列連結）==========");
            System.out.println();

            System.out.println("--- ① 正常なログイン試行 ---");
            vulnerableLogin("alice", "password123");  // → 成功（正常）

            System.out.println();
            System.out.println("--- ② SQLインジェクション攻撃 ---");
            // ' OR 1=1 -- を入力すると SQL が以下に変化する:
            //   WHERE username = '' OR 1=1 --' AND password = 'anything'
            // 「--」以降はSQLコメントになるため password 条件が無効化され、
            // OR 1=1 によって全レコードが取得される（全員ログイン成功扱い）
            vulnerableLogin("' OR 1=1 --", "anything");  // → 全ユーザーが取得される！

            System.out.println();
            System.out.println("========== After: PreparedStatement（プレースホルダー）==========");
            System.out.println();

            System.out.println("--- ③ 正常なログイン試行（PreparedStatement）---");
            safeLogin("alice", "password123");  // → 成功

            System.out.println();
            System.out.println("--- ④ SQLインジェクション試み（PreparedStatement）---");
            // Before と同じ攻撃文字列を使う → PreparedStatement では攻撃が成立しない
            safeLogin("' OR 1=1 --", "anything");  // → 失敗（攻撃不成立）

            System.out.println();
            System.out.println("=== まとめ ===");
            System.out.println("Statement:         文字列連結でSQLを組み立てる → インジェクション脆弱");
            System.out.println("PreparedStatement: ? でパラメータを分離する → インジェクション防御");
            System.out.println("現場ルール: ユーザー入力を含むSQLは必ず PreparedStatement を使う");

        } catch (SQLException e) {
            System.err.println("エラー: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // セットアップ: users テーブル作成とサンプルデータ投入
    // ---------------------------------------------------------
    private static void setup() throws SQLException {
        // try-with-resources で接続を自動クローズする（Java 7 で導入済み）
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // users テーブルを作成する
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users ("
                + "  id       INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "  username VARCHAR(100),"
                + "  password VARCHAR(100),"
                + "  role     VARCHAR(20)"
                + ")"
            );

            // サンプルユーザーを3件 INSERT する
            stmt.execute("INSERT INTO users (username, password, role) VALUES ('alice', 'password123', 'user')");
            stmt.execute("INSERT INTO users (username, password, role) VALUES ('bob', 'secret456', 'user')");
            stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin_pass', 'admin')");
        }
        System.out.println("セットアップ完了: users テーブルに 3 件のデータを投入しました");
        System.out.println();
    }

    // ---------------------------------------------------------
    // ========== Before: Statement で文字列連結 — SQLインジェクション脆弱 ==========
    // [アンチパターン] ユーザー入力を文字列連結でSQLに埋め込む
    // 攻撃者が username に " ' OR '1'='1 " を入力すると WHERE 条件が常に真になる
    // ---------------------------------------------------------
    private static void vulnerableLogin(String username, String password) throws SQLException {
        // 文字列連結でSQLを組み立てる（危険！）
        String sql = "SELECT * FROM users WHERE username = '" + username
                   + "' AND password = '" + password + "'";
        System.out.println("[脆弱] 実行SQL: " + sql);

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("[脆弱] ログイン成功: username=" + rs.getString("username")
                        + ", role=" + rs.getString("role"));
                // 全レコードを表示（インジェクション成功時は複数行出る）
                do {
                    System.out.println("  → 取得レコード: " + rs.getString("username"));
                } while (rs.next());
            } else {
                System.out.println("[脆弱] ログイン失敗");
            }
        }
    }

    // ---------------------------------------------------------
    // ========== After: PreparedStatement — SQLインジェクション防御 ==========
    // ? プレースホルダーでパラメータをバインドする
    // パラメータはSQLの構造と分離されるため、どんな文字列を入れても安全
    // ---------------------------------------------------------
    private static void safeLogin(String username, String password) throws SQLException {
        // SQL構造とパラメータを分離する（安全）
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        System.out.println("[安全] テンプレートSQL: " + sql);

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // setString でパラメータを個別にバインドする
            // 入力値がどんなSQL文字列でも「ただの文字列値」として扱われる
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[安全] ログイン成功: username=" + rs.getString("username")
                            + ", role=" + rs.getString("role"));
                } else {
                    System.out.println("[安全] ログイン失敗（インジェクション文字列はそのまま検索値として扱われる）");
                }
            }
        }
    }
}
