/**
 * 【なぜこのコードを学ぶのか】
 * 例外は「プログラムが想定外の状態になったことを呼び出し元に知らせる」仕組みだ。
 * しかし「空の catch で握りつぶす」アンチパターンは、
 * バグが起きても誰も気づかない最も危険な書き方のひとつ。
 * チェック例外 vs 非チェック例外の違い、自作例外クラスの作り方、
 * try-with-resources によるリソースの確実なクローズを体験する。
 */
package com.example.modern_api;

public class ExceptionHandling {

    public static void main(String[] args) {

        System.out.println("=== 例外処理の深掘り ===");
        System.out.println();

        // ========== アンチパターン: 空の catch（例外の握りつぶし）==========
        // 最も危険なパターン。エラーが起きても完全に沈黙する。
        // バグが起きても誰も気づかない。ログすら残らない。
        // 現場では「絶対にやってはいけない」とされる。

        System.out.println("--- アンチパターン: 空の catch（例外の握りつぶし）---");
        try {
            int result = Integer.parseInt("abc"); // NumberFormatException が発生するはず
            System.out.println("解析結果: " + result); // この行には到達しない
        } catch (NumberFormatException e) {
            // ← 何もしない！これがアンチパターン。
            // バグが起きても誰も気づかない。ログすら残らない。
        }
        System.out.println("【アンチパターン】 空のcatch: エラーが起きたが何も表示されない");
        System.out.println("→ 少なくとも e.printStackTrace() や logger.error() でログを残すこと");
        System.out.println();

        // ========== チェック例外 vs 非チェック例外 ==========
        // チェック例外: Exception を継承（RuntimeException 以外）
        //   → コンパイラが catch または throws を強制する
        //   → 例: IOException, SQLException, ParseException
        //   → 「呼び出し元が回復可能な異常」に使う
        //
        // 非チェック例外: RuntimeException を継承
        //   → コンパイラが catch を強制しない（任意）
        //   → 例: NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException
        //   → 「プログラマのミス（バグ）」に使う
        //
        // 現場の判断基準:
        //   「呼び出し元が回復可能な異常」→ チェック例外
        //   「プログラマのミス（バグ）」→ 非チェック例外（RuntimeException）

        System.out.println("--- チェック例外 vs 非チェック例外 ---");
        System.out.println("チェック例外   : IOException / SQLException など（コンパイラが catch を強制）");
        System.out.println("非チェック例外 : RuntimeException のサブクラス（catch は任意）");
        System.out.println("判断基準       : 呼び出し元が回復可能な異常 → チェック例外");
        System.out.println("               : プログラマのミス（バグ）→ 非チェック例外");
        System.out.println();

        // ========== 自作例外を意図的に投げる ==========
        System.out.println("--- 自作例外を意図的に投げる ---");
        System.out.println("=== 在庫管理システムの例 ===");

        // 失敗ケース: 在庫3個なのに5個注文
        try {
            orderItem("キーボード", 5, 3); // 在庫3個なのに5個注文 → 例外発生
        } catch (InsufficientStockException e) {
            System.out.println("注文失敗: " + e.getMessage());
            System.out.println("  商品名: " + e.getItemName());
            System.out.println("  要求数: " + e.getRequested() + " 個");
            System.out.println("  在庫数: " + e.getAvailable() + " 個");
        }

        // 成功ケース: 在庫10個で2個注文
        try {
            orderItem("マウス", 2, 10); // 在庫10個で2個注文 → 成功
        } catch (InsufficientStockException e) {
            System.out.println("注文失敗: " + e.getMessage());
        }
        System.out.println();

        // ========== finally: 例外が起きても必ず実行される ==========
        // finally は try ブロックが正常終了しても、例外が発生しても必ず実行される。
        // ただし try-with-resources が使える場面では finally より try-with-resources を選ぶ。

        System.out.println("--- finally: 例外の有無にかかわらず必ず実行される ---");
        System.out.println("=== finally の動作確認 ===");
        try {
            System.out.println("try: 処理開始");
            int ignored = 10 / 0; // ArithmeticException が発生する（結果は使わない）
            System.out.println("try: この行は実行されない（ignored = " + ignored + "）");
        } catch (ArithmeticException e) {
            System.out.println("catch: ゼロ除算エラーをキャッチ: " + e.getMessage());
        } finally {
            System.out.println("finally: 例外の有無にかかわらず必ず実行される（リソース解放に使う）");
        }
        System.out.println();

        // ========== try-with-resources: リソースの自動クローズ ==========
        // Java 7 以降の機能（Java 7 から使える）
        // try の () 内で宣言したリソースは、try ブロック終了時に自動で close() が呼ばれる。
        // → finally で close() を書き忘れるバグを防ぐ
        // → ファイル・DB接続・ネットワークソケットのクローズに必ず使う

        System.out.println("--- try-with-resources: リソースの自動クローズ ---");
        System.out.println("=== try-with-resources の動作確認 ===");
        try (FakeResource resource = new FakeResource("DBコネクション")) {
            resource.use();
            // ここで例外が起きても close() は必ず呼ばれる
        } // ← ここで自動的に resource.close() が呼ばれる
        System.out.println("try-with-resources のブロックを抜けた（close() は自動で呼ばれた）");
        System.out.println();

        // ========== マルチキャッチ: 複数の例外を1つの catch で処理 ==========
        // Java 7 以降の機能（Java 7 から使える）
        // 同じ処理で複数の例外が起きうる場合に、catch を1つにまとめられる。

        System.out.println("--- マルチキャッチ: 複数の例外を1つの catch で処理 ---");
        System.out.println("=== マルチキャッチ ===");
        try {
            String[] items = {"100", "abc", null};
            for (String item : items) {
                // NumberFormatException (abc の場合) または NullPointerException (null の場合) が発生する
                int value = Integer.parseInt(item);
                System.out.println("解析成功: " + value);
            }
        } catch (NumberFormatException | NullPointerException e) {
            // 2種類の例外を1つの catch でまとめて処理する
            System.out.println("解析エラー: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        System.out.println();

        System.out.println("=== まとめ ===");
        System.out.println("  空の catch        : 絶対に避ける（バグが完全に隠れる）");
        System.out.println("  チェック例外      : 回復可能な異常に使う（コンパイラが強制）");
        System.out.println("  非チェック例外    : プログラマのミス（バグ）に使う");
        System.out.println("  try-with-resources: AutoCloseable なリソースは必ずこれで閉じる");
        System.out.println("  マルチキャッチ    : 同じ処理をする例外をまとめられる（Java 7+）");
    }

    // ---------------------------------------------------------
    // orderItem: 商品の注文を処理する（在庫不足なら例外を投げる）
    // ---------------------------------------------------------
    private static void orderItem(String itemName, int quantity, int stock) {
        if (quantity > stock) {
            // 在庫不足の場合は自作の例外を投げる
            throw new InsufficientStockException(itemName, quantity, stock);
        }
        System.out.println(itemName + " を " + quantity + " 個注文しました"
            + "（在庫残: " + (stock - quantity) + " 個）");
    }
}

// ---------------------------------------------------------
// InsufficientStockException: 在庫不足を表す自作例外
// 非チェック例外（RuntimeException を継承）なので throws 宣言が不要。
// 呼び出し元が catch を強制されない「プログラマが対処を選べる例外」。
// ---------------------------------------------------------
class InsufficientStockException extends RuntimeException {

    private final String itemName;  // 在庫不足になった商品名
    private final int requested;    // 要求された数量
    private final int available;    // 実際の在庫数

    InsufficientStockException(String itemName, int requested, int available) {
        // 親クラス（RuntimeException）に渡すメッセージを生成する
        super(itemName + " の在庫が不足しています。要求: " + requested + " 個 / 在庫: " + available + " 個");
        this.itemName  = itemName;
        this.requested = requested;
        this.available = available;
    }

    public String getItemName()  { return itemName;  }
    public int    getRequested() { return requested; }
    public int    getAvailable() { return available; }
}

// ---------------------------------------------------------
// FakeResource: AutoCloseable を実装したデモ用リソース
// try-with-resources でクローズされる様子を確認するためのクラス。
// 実際のアプリでは DB コネクション・ファイルストリームなどがこれにあたる。
// ---------------------------------------------------------
class FakeResource implements AutoCloseable {

    private final String name;

    FakeResource(String name) {
        this.name = name;
        System.out.println("  [リソース] " + name + " をオープンした");
    }

    public void use() {
        System.out.println("  [リソース] " + name + " を使用した");
    }

    @Override
    public void close() {
        // try-with-resources がブロックを抜けるときに自動で呼ぶ
        System.out.println("  [リソース] " + name + " をクローズした（try-with-resources が自動で呼ぶ）");
    }
}
