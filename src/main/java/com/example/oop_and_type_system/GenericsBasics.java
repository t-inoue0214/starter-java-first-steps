/**
 * 【なぜこのコードを学ぶのか】
 * 第05章で何気なく書いた ArrayList<String> の <String> の正体はジェネリクスと呼ばれる仕組みだ。
 * Object型で「何でも受け取れる箱」を作ると、取り出し時のキャストが必要になり、
 * 型が合わなければ ClassCastException という実行時エラーが起きる。
 * ジェネリクスを使うと同じ間違いをコンパイル時に検出できる—問題の発見が大幅に早くなる。
 */
package com.example.oop_and_type_system;

import java.util.List;

public class GenericsBasics {

    // ---------------------------------------------------------
    // ========== Before: Object型を使った「なんでも箱」（問題のある書き方） ==========
    // ---------------------------------------------------------

    // Object型で何でも入る箱を作った（ジェネリクスを知らない時代の書き方）
    private static class ObjectBox {

        private Object value; // Object型なら String でも Integer でも入る

        public void set(Object value) {
            this.value = value;
        }

        public Object get() {
            return this.value; // 取り出しは Object型で返ってくる
        }
    }

    // ---------------------------------------------------------
    // ========== After: ジェネリクスを使った型安全な箱 ==========
    // ---------------------------------------------------------

    // <T> が「型パラメータ」。Box<String> と書くと T が String に置き換わる
    private static class Box<T> {

        private T value; // T型として保管する

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return this.value; // T型のまま返るのでキャスト不要
        }
    }

    // ---------------------------------------------------------
    // 応用例: 境界型パラメータ <T extends Number>
    // ---------------------------------------------------------
    // ここはやや応用的な内容です。
    // 「Box<String> と Box<Integer> を別々に作ったが、
    //  Number（Integer や Double）を同じ計算メソッドで扱いたい」
    // という現実の問題から生まれた仕組みです。
    // 「T は Number の仲間だけに限定する」という制約が <T extends Number> の意味です。
    // 初めて読むときは「こういうことも書けるんだ」と流して OK です。

    // <T extends Number> は「NumberのサブタイプならどれでもOK」という制約
    // Integer, Double, Long などを同じメソッドで受け取れる
    private static <T extends Number> double sum(List<T> list) {
        double total = 0.0;
        for (T n : list) {
            // Number型には doubleValue() が定義されているので呼び出せる
            total += n.doubleValue();
        }
        return total;
    }

    public static void main(String[] args) {

        System.out.println("=== Before: Object型の箱（危険な書き方） ===");

        ObjectBox objectBox = new ObjectBox();
        objectBox.set("こんにちは"); // String を入れた

        // 取り出すときは (String) とキャストが必要
        String text = (String) objectBox.get();
        System.out.println("取り出した文字列: " + text);

        // 問題: 別の型を入れてしまうと実行時にクラッシュする
        objectBox.set(12345); // Integer を入れてしまった（コンパイルは通る）
        try {
            // Integer を String として取り出そうとすると実行時エラーが発生する
            String wrong = (String) objectBox.get(); // ← ここで ClassCastException!
            System.out.println(wrong);
        } catch (ClassCastException e) {
            System.out.println("[Before] 実行時エラー発生: " + e.getMessage());
            System.out.println("  コンパイルは通ったのに、実行して初めて気づいた...");
        }

        System.out.println();
        System.out.println("=== After: ジェネリクスの箱（型安全な書き方） ===");

        // Box<String> と宣言すれば、String しか入れられない
        Box<String> stringBox = new Box<>();
        stringBox.set("こんにちは");

        // キャスト不要！ 型が保証されているので String として直接受け取れる
        String safeText = stringBox.get();
        System.out.println("型安全に取り出せた: " + safeText);

        // 間違った型を入れようとするとコンパイルエラーになる（実行前に気づける）
        // stringBox.set(12345); // コンパイルエラー: int は String に入れられない

        // Integer 専用の箱も作れる
        Box<Integer> intBox = new Box<>();
        intBox.set(42);
        int number = intBox.get(); // こちらも Integer として型安全に取り出せる
        System.out.println("Integer箱から取り出した値: " + number);

        System.out.println();
        System.out.println("=== 応用: 境界型パラメータ <T extends Number> ===");

        // Integer のリストを sum() に渡せる
        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
        //   List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> integers = List.of(1, 2, 3, 4, 5);
        System.out.println("整数リストの合計: " + sum(integers));

        // Double のリストも同じ sum() で処理できる
        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
        //   List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);
        List<Double> doubles = List.of(1.1, 2.2, 3.3);
        System.out.println("小数リストの合計: " + sum(doubles));

        // String のリストは渡せない（Number のサブタイプでないため）
        // sum(List.of("a", "b")); // コンパイルエラー: String は Number ではない
    }
}
