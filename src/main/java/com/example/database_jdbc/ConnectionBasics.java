/**
 * 【なぜこのコードを学ぶのか】
 * JDBC（Java Database Connectivity）は Java とデータベースをつなぐ標準 API。
 * Spring Data JPA や MyBatis などの ORM フレームワークも内部では JDBC を使っている。
 * ここでは H2 インメモリデータベースを使ってゼロから接続・CRUD（作成・読取・更新・削除）を
 * 手書きし、フレームワークが隠している仕組みを体験する。
 *
 * 実行方法:
 *   javac -d out/ -cp lib/h2.jar src/main/java/com/example/database_jdbc/ConnectionBasics.java
 *   java -cp "out/:lib/h2.jar" com.example.database_jdbc.ConnectionBasics
 */
package com.example.database_jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC の基本操作を学ぶクラス。
 * H2 インメモリデータベースに接続し、CRUD（作成・読取・更新・削除）を体験する。
 */
public class ConnectionBasics {

    // ---------------------------------------------------------
    // JDBC 接続情報（定数）
    // ---------------------------------------------------------
    /** H2 インメモリデータベースの接続 URL。DB_CLOSE_DELAY=-1 はアプリ終了まで DB を保持する設定 */
    private static final String JDBC_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    /** ユーザー名（H2 のデフォルト） */
    private static final String USER = "sa";
    /** パスワード（H2 のデフォルトは空文字） */
    private static final String PASSWORD = "";

    // ---------------------------------------------------------
    // メインメソッド: CRUD の全工程を順番に実行する
    // ---------------------------------------------------------
    public static void main(String[] args) {
        try {
            // ステップ1: 接続情報を表示する
            showConnectionInfo();

            // ステップ2: テーブルを作成する
            createTable();

            // ステップ3: 商品データを3件 INSERT する
            insertProducts();

            // ステップ4: 全件 SELECT して現在の状態を確認する
            System.out.println("=== INSERT 直後の商品一覧 ===");
            selectAll();

            // ステップ5: id=1 の商品の価格を UPDATE する
            updateProduct();

            // ステップ6: id=3 の商品を DELETE する
            deleteProduct();

            // ステップ7: 最終状態を SELECT して確認する
            System.out.println("=== UPDATE・DELETE 後の最終一覧 ===");
            selectAll();

        } catch (SQLException e) {
            // データベース操作で問題が起きたときは SQLState と ErrorCode も表示する
            System.err.println("データベースエラー: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
        }
    }

    // ---------------------------------------------------------
    // ステップ1: JDBC 接続情報を表示し、ドライバの自動登録を説明する
    // ---------------------------------------------------------
    private static void showConnectionInfo() {
        System.out.println("=== JDBC 接続情報 ===");
        System.out.println("URL: " + JDBC_URL);
        System.out.println("User: " + USER);
        System.out.println();
        // JDBC 4.0（Java 6 以降）からドライバは META-INF/services の仕組みで自動登録される
        // Java 5 以前は Class.forName("org.h2.Driver") を明示的に呼ぶ必要があった
        System.out.println("// H2 ドライバは JDBC 4.0 以降（Java 6+）で自動登録される");
        System.out.println("// Class.forName(\"org.h2.Driver\") は不要（Java 7 以前は必要だった）");
        System.out.println();
    }

    // ---------------------------------------------------------
    // ステップ2: products テーブルを作成する
    // ---------------------------------------------------------
    /**
     * CREATE TABLE IF NOT EXISTS を使うと、テーブルが既存の場合でもエラーにならない。
     * テキストブロックで SQL を読みやすく書く。
     */
    private static void createTable() throws SQLException {
        // [Java 7 不可] テキストブロック（"""）は Java 15 以降。
        // Java 7 では文字列連結（+ "\n"）または StringBuilder で代替する:
        //   String sql = "CREATE TABLE IF NOT EXISTS products (\n"
        //              + "    id    INTEGER PRIMARY KEY AUTO_INCREMENT,\n"
        //              + "    name  VARCHAR(100) NOT NULL,\n"
        //              + "    price INTEGER NOT NULL,\n"
        //              + "    category VARCHAR(50)\n"
        //              + ")";
        String sql = """
                CREATE TABLE IF NOT EXISTS products (
                    id    INTEGER PRIMARY KEY AUTO_INCREMENT,
                    name  VARCHAR(100) NOT NULL,
                    price INTEGER NOT NULL,
                    category VARCHAR(50)
                )
                """;

        // try-with-resources で Connection と Statement を自動クローズする
        // Connection をクローズしないとリソースリークが起きる（後の SqlInjectionDemo で詳述）
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("=== テーブル作成完了 ===");
        }
    }

    // ---------------------------------------------------------
    // ステップ3: PreparedStatement で安全に INSERT する
    // ---------------------------------------------------------
    /**
     * PreparedStatement を使う理由: パラメータを ? で分離することで SQL インジェクションを防ぐ。
     * Statement（文字列連結）を使った危険な書き方は SqlInjectionDemo.java で体験する。
     */
    private static void insertProducts() throws SQLException {
        // ? はパラメータのプレースホルダー。setXxx() でバインドする
        String sql = "INSERT INTO products (name, price, category) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // ----- 1件目: ノートPC -----
            pstmt.setString(1, "ノートPC");      // 第1引数: カラム位置（1 始まり）
            pstmt.setInt(2, 150_000);             // アンダースコアで桁を読みやすくする
            pstmt.setString(3, "PC");
            pstmt.executeUpdate();                // INSERT / UPDATE / DELETE には executeUpdate()

            // ----- 2件目: マウス -----
            pstmt.setString(1, "マウス");
            pstmt.setInt(2, 3_500);
            pstmt.setString(3, "周辺機器");
            pstmt.executeUpdate();

            // ----- 3件目: キーボード -----
            pstmt.setString(1, "キーボード");
            pstmt.setInt(2, 8_000);
            pstmt.setString(3, "周辺機器");
            pstmt.executeUpdate();

            System.out.println("=== 3件の商品を INSERT しました ===");
        }
    }

    // ---------------------------------------------------------
    // ステップ4・7: 全件 SELECT して一覧表示する（共通メソッド）
    // ---------------------------------------------------------
    /**
     * ResultSet は行ごとに next() で進み、getXxx() でカラム値を取得する。
     * カラム名（文字列）でも列インデックス（整数）でも取得できるが、
     * 可読性のためカラム名指定を推奨する。
     */
    private static void selectAll() throws SQLException {
        String sql = "SELECT id, name, price, category FROM products ORDER BY id";

        // ResultSet も try-with-resources でクローズする
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {  // SELECT には executeQuery()

            System.out.println("--- 商品一覧 ---");
            // printf でカラム幅をそろえて見やすく表示する
            System.out.printf("%-5s %-20s %10s %s%n", "ID", "名前", "価格(円)", "カテゴリ");
            System.out.println("-".repeat(50));

            // rs.next() が true を返す間、次の行にカーソルが進む
            while (rs.next()) {
                System.out.printf("%-5d %-20s %10d %s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("price"),
                        rs.getString("category"));
            }
            System.out.println();
        }
    }

    // ---------------------------------------------------------
    // ステップ5: PreparedStatement で安全に UPDATE する
    // ---------------------------------------------------------
    /**
     * executeUpdate() の戻り値は「影響を受けた行数」。
     * WHERE 条件でヒットしなかった場合は 0 が返る。
     */
    private static void updateProduct() throws SQLException {
        String sql = "UPDATE products SET price = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 145_000);  // 新しい価格（150,000 → 145,000 に値下げ）
            pstmt.setInt(2, 1);         // 対象: id=1（ノートPC）
            int updated = pstmt.executeUpdate();
            System.out.println("=== UPDATE 完了: " + updated + "件更新 (id=1 の価格を 145,000 に変更) ===");
        }
    }

    // ---------------------------------------------------------
    // ステップ6: PreparedStatement で安全に DELETE する
    // ---------------------------------------------------------
    /**
     * DELETE も executeUpdate() を使う。
     * WHERE 条件を付け忘れると全件削除になるため注意する。
     */
    private static void deleteProduct() throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 3);  // 対象: id=3（キーボード）
            int deleted = pstmt.executeUpdate();
            System.out.println("=== DELETE 完了: " + deleted + "件削除 (id=3 キーボードを削除) ===");
        }
    }
}
