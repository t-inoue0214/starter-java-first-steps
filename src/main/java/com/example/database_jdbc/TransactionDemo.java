/**
 * 【なぜこのコードを学ぶのか】
 * 「口座Aから1万円引いて口座Bに1万円足す」という2つのSQL更新は、
 * 片方だけ成功すると残高が消える。トランザクションは「全部成功（commit）か
 * 全部失敗（rollback）か」を保証する仕組み。
 * JDBCではデフォルトが auto-commit（1SQL1コミット）なので、
 * 複数SQLを1単位にするには明示的にトランザクション制御が必要。
 */
package com.example.database_jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC トランザクション（commit / rollback）の基礎デモ。
 * auto-commit の問題を体験した後、明示的トランザクションで解決する。
 *
 * <p>実行方法:</p>
 * <pre>
 * javac -d out/ -cp lib/h2.jar src/main/java/com/example/database_jdbc/TransactionDemo.java
 * java -cp "out/:lib/h2.jar" com.example.database_jdbc.TransactionDemo
 * </pre>
 */
public class TransactionDemo {

    // ---------------------------------------------------------
    // DB接続設定（H2 インメモリDB）
    // DB_CLOSE_DELAY=-1 は「全接続が閉じてもDBを保持する」設定
    // ---------------------------------------------------------
    private static final String JDBC_URL = "jdbc:h2:mem:bank;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            // ---------------------------------------------------------
            // セットアップ: テーブル作成と初期データ投入
            // ---------------------------------------------------------
            setup();

            System.out.println("=== 初期状態 ===");
            showBalances();
            System.out.println();

            // ---------------------------------------------------------
            // Before: auto-commit（途中でエラーが起きるとお金が消える）
            // ---------------------------------------------------------
            transferWithAutoCommit(1, 2, 30_000);
            System.out.println("=== auto-commit 後の状態（お金が消えた！）===");
            showBalances();

            System.out.println();

            // DBを初期状態に戻す（残高をリセット）
            resetBalances();
            System.out.println("=== リセット後の状態 ===");
            showBalances();
            System.out.println();

            // ---------------------------------------------------------
            // After: トランザクション（成功ケース）
            // ---------------------------------------------------------
            transferWithTransaction(1, 2, 30_000);
            System.out.println("=== トランザクション送金後の状態 ===");
            showBalances();
            System.out.println();

            // ---------------------------------------------------------
            // After: トランザクション（残高不足でロールバック）
            // ---------------------------------------------------------
            System.out.println("=== 残高不足でロールバックするケース ===");
            transferWithTransaction(1, 2, 1_000_000);  // 残高以上の送金
            System.out.println("=== ロールバック後の状態（変化なし）===");
            showBalances();

        } catch (SQLException e) {
            System.err.println("エラー: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // セットアップ: accounts テーブル作成と初期データ投入
    // ---------------------------------------------------------
    private static void setup() throws SQLException {
        // try-with-resources で接続を自動クローズする（Java 7 で導入済み）
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // accounts テーブルを作成する
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS accounts ("
                + "  id      INTEGER PRIMARY KEY,"
                + "  name    VARCHAR(50),"
                + "  balance INTEGER"
                + ")"
            );

            // 初期データ: Alice 10万円、Bob 5万円
            // アンダースコアは数値リテラルの区切り（Java 7 で導入済み）
            stmt.execute("INSERT INTO accounts (id, name, balance) VALUES (1, 'Alice', 100000)");
            stmt.execute("INSERT INTO accounts (id, name, balance) VALUES (2, 'Bob', 50000)");
        }
        System.out.println("セットアップ完了: accounts テーブルに 2 件のデータを投入しました");
    }

    // ---------------------------------------------------------
    // 残高表示: 全口座の現在残高を表示する
    // ---------------------------------------------------------
    private static void showBalances() throws SQLException {
        String sql = "SELECT id, name, balance FROM accounts ORDER BY id";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("--- 口座残高 ---");
            while (rs.next()) {
                // [Java 7 不可] String.formatted() は Java 15 以降。Java 7 では String.format() を使う:
                //   System.out.println(String.format("  %s: %,d 円", rs.getString("name"), rs.getInt("balance")));
                System.out.printf("  %s: %,d 円%n", rs.getString("name"), rs.getInt("balance"));
            }
        }
    }

    // ---------------------------------------------------------
    // 残高リセット: 初期値（Alice: 100,000円 / Bob: 50,000円）に戻す
    // ---------------------------------------------------------
    private static void resetBalances() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE accounts SET balance = 100000 WHERE id = 1");
            stmt.execute("UPDATE accounts SET balance = 50000  WHERE id = 2");
        }
        System.out.println("残高をリセットしました（Alice: 100,000円 / Bob: 50,000円）");
    }

    // ---------------------------------------------------------
    // サーバー障害のシミュレーション用ヘルパー
    // auto-commit の問題を体験させるため、意図的に例外を投げる
    // ---------------------------------------------------------
    private static void simulateServerFailure() {
        throw new RuntimeException("サーバー障害シミュレーション！");
    }

    // ---------------------------------------------------------
    // ========== Before: auto-commit（デフォルト）の問題 ==========
    // JDBCはデフォルトで auto-commit=true。
    // 1SQLごとに自動コミットされるので、途中エラーで「引いた分だけ失われる」問題が起きる
    // ---------------------------------------------------------
    private static void transferWithAutoCommit(int fromId, int toId, int amount) {
        System.out.println("--- [アンチパターン] auto-commit での送金: " + amount + " 円 ---");
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            // conn.getAutoCommit() は true（デフォルト）
            System.out.println("  auto-commit: " + conn.getAutoCommit());  // → true

            // Step1: Alice から引く（この時点で即コミットされる）
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE id = ?")) {
                pstmt.setInt(1, amount);
                pstmt.setInt(2, fromId);
                pstmt.executeUpdate();
                System.out.println("  Step1: Alice から " + amount + " 円引いた（即コミット）");
            }

            // Step2: 意図的に例外を発生させる（サーバー障害をシミュレート）
            // → Step1 は既にコミット済みのため、ここで止まると Alice の残高だけ減る
            simulateServerFailure();

            // Step3: Bob に足す
            // ↑ の例外により、ここには到達しない（Bob への入金がスキップされる）
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
                pstmt.setInt(1, amount);
                pstmt.setInt(2, toId);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("  SQLエラー: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("  [問題] 例外発生: " + e.getMessage());
            System.out.println("  → Alice の残高は減ったが Bob には届いていない！（お金が消えた）");
        }
    }

    // ---------------------------------------------------------
    // ========== After: 明示的トランザクション（commit / rollback）==========
    // setAutoCommit(false) でトランザクションを開始し、
    // 全ステップが成功したら commit()、途中で失敗したら rollback() する
    // ---------------------------------------------------------
    private static void transferWithTransaction(int fromId, int toId, int amount) {
        System.out.println("--- [安全] トランザクションでの送金: " + amount + " 円 ---");
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);  // auto-commit を無効化してトランザクション開始
            System.out.println("  auto-commit: " + conn.getAutoCommit());  // → false

            try {
                // Step1: fromId から引く（まだコミットされない）
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE accounts SET balance = balance - ? WHERE id = ?")) {
                    pstmt.setInt(1, amount);
                    pstmt.setInt(2, fromId);
                    pstmt.executeUpdate();
                    System.out.println("  Step1: Alice から " + amount + " 円引いた（まだコミットされていない）");
                }

                // Step2: 残高チェック（マイナスになる場合はロールバック）
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT balance FROM accounts WHERE id = ?")) {
                    pstmt.setInt(1, fromId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt("balance") < 0) {
                            throw new SQLException("残高不足: balance=" + rs.getInt("balance"));
                        }
                    }
                }

                // Step3: toId に足す（まだコミットされない）
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
                    pstmt.setInt(1, amount);
                    pstmt.setInt(2, toId);
                    pstmt.executeUpdate();
                    System.out.println("  Step2: Bob に " + amount + " 円足した");
                }

                // 全ステップ成功 → コミット（ここで初めてDBに確定する）
                conn.commit();
                System.out.println("  → commit: 送金完了");

            } catch (SQLException e) {
                // 途中失敗 → ロールバック（全ステップをなかったことにする）
                conn.rollback();
                System.out.println("  → rollback: 例外発生のため全て取り消し: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("接続エラー: " + e.getMessage());
        }
    }
}
