/**
 * 【なぜこのコードを学ぶのか】
 * staticを乱用した「神クラス」はテストできない・差し替えできない・状態が混入するという
 * 3つの問題を抱える。staticが適切な場所（純粋な計算）と不適切な場所（状態を持つ処理）の
 * 境界線を知ることが、保守しやすいコードへの第一歩だ。
 * アノテーションの体験では @Override の有無が「誤字バグ」をどう防ぐかを実際に確認する。
 *
 * 「神クラス（God Class）」とは: あらゆる処理を1つのクラスに詰め込んだ肥大化した設計のことです。
 * 「神のように何でも知っている」クラスという皮肉を込めた呼び方で、
 * 現場では「避けるべきアンチパターン（悪い書き方の見本）」として知られています。
 *
 * 【第08章・第12章への伏線】
 * MathHelper のような「状態を持たず、同じ入力に対して常に同じ結果を返す」メソッドを
 * 純粋関数（pure function）という。
 * 第08章では純粋関数の設計（イミュータブル設計）を、
 * 第12章では「純粋関数はスレッドセーフ」であることを学ぶ。
 */
package com.example.oop_and_type_system;

public class StaticAndAnnotation {

    // ---------------------------------------------------------
    // ========== Before: すべてをstaticにした「神クラス」（問題のある設計） ==========
    // ---------------------------------------------------------

    static class AppUtilBefore {

        // 問題1: static なフィールドでカウントを管理している
        // クラス全体で1つの状態を共有するため、呼び出し順序によって結果が変わる
        static int processCount = 0;

        // 問題2: このメソッドはカウントを副作用として変更する
        // 「何回呼ばれたか」という状態がメソッドに隠れている
        public static String formatMessage(String text) {
            processCount++; // 呼ぶたびにクラス変数が変わる（状態の混入）
            return "[" + processCount + "] " + text;
        }

        // 問題3: テストしにくい
        // formatMessage() が何番を返すかは「何回呼ばれたか」に依存する
        // テストを独立して実行できない（前のテストの影響を受ける）
        public static void resetCount() {
            processCount = 0; // リセットが必要になる → 設計の欠陥
        }
    }

    // ---------------------------------------------------------
    // ========== After: インスタンスに状態を切り出す ==========
    // ---------------------------------------------------------

    static class AppUtil {
        // インスタンス変数なので、各インスタンスが独立した状態を持つ
        private int processCount = 0;

        // インスタンスメソッドなので、同じクラスの別インスタンスに影響しない
        public String formatMessage(String text) {
            processCount++;
            return "[" + processCount + "] " + text;
        }

        public int getProcessCount() {
            return processCount;
        }
    }

    // staticが適切な例: 純粋な計算（状態を持たず、入力だけで結果が決まる）
    // Math.max() と同じ考え方—引数が同じなら常に同じ結果を返す
    static class MathHelper {
        // 状態を持たない純粋な計算はstaticで問題ない
        static int max(int a, int b) {
            return a >= b ? a : b;
        }

        static double circleArea(double radius) {
            return Math.PI * radius * radius;
        }
    }

    // ---------------------------------------------------------
    // @Override アノテーションの体験
    // ---------------------------------------------------------

    static class DogWithoutAnnotation {
        // @Override を付けずに toString を「オーバーライドしたつもり」で書いた
        // しかし小文字の "tostring" は toString() と別のメソッドとして扱われる
        // コンパイルは通るが、意図したオーバーライドになっていない（バグ！）
        public String tostring() { // ← 小文字の 's' に気づかない
            return "Dog（tostring バグあり）";
        }
    }

    static class DogWithAnnotation {
        // @Override を付けると「親クラスのメソッドをオーバーライドする意図」を宣言する
        // もし親クラスに存在しないメソッド名を書くとコンパイルエラーになる（安全！）
        @Override
        public String toString() {
            return "Dog（@Override で正しくオーバーライド）";
        }

        // もし以下のように書くとコンパイルエラーになる:
        // @Override
        // public String tostring() { // ← コンパイルエラー: Object に tostring() は存在しない
        //     return "エラーになる";
        // }
    }

    // ---------------------------------------------------------
    // @FunctionalInterface アノテーションの体験
    // ---------------------------------------------------------

    // 正しい使い方: 抽象メソッドが1つなので @FunctionalInterface が付けられる
    // [Java 7 不可] @FunctionalInterface は Java 8 以降のアノテーション
    @FunctionalInterface
    interface SingleMethod {
        void doSomething();
    }

    // 間違った使い方: 抽象メソッドが2つあると @FunctionalInterface はコンパイルエラーになる
    // @FunctionalInterface // ← これを付けると以下のインターフェースでコンパイルエラー
    interface TwoMethods {
        void first();
        void second();
        // エラー: "Invalid '@FunctionalInterface' annotation;
        //          TwoMethods is not a functional interface"
    }

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // Before: static な状態変数の問題
        // ---------------------------------------------------------
        System.out.println("=== Before: static な状態変数の問題 ===");

        // 1回目の呼び出し
        System.out.println(AppUtilBefore.formatMessage("処理A")); // [1] 処理A
        System.out.println(AppUtilBefore.formatMessage("処理B")); // [2] 処理B

        // 別の場所から独立して使おうとしても、前の呼び出し回数が引き継がれる
        System.out.println("現在のカウント: " + AppUtilBefore.processCount); // 2 ← 状態が混入している

        // リセットしないと次の呼び出しもカウントが継続する
        AppUtilBefore.resetCount();
        System.out.println(AppUtilBefore.formatMessage("処理C")); // [1] 処理C（リセット後）

        // ---------------------------------------------------------
        // After: インスタンスに状態を切り出した設計
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== After: インスタンスごとに独立した状態 ===");

        AppUtil utilA = new AppUtil(); // インスタンスAは独自のカウントを持つ
        AppUtil utilB = new AppUtil(); // インスタンスBは別のカウントを持つ

        System.out.println(utilA.formatMessage("処理A-1")); // [1] 処理A-1
        System.out.println(utilA.formatMessage("処理A-2")); // [2] 処理A-2
        System.out.println(utilB.formatMessage("処理B-1")); // [1] 処理B-1 ← Aに影響されない
        System.out.println("A のカウント: " + utilA.getProcessCount()); // 2
        System.out.println("B のカウント: " + utilB.getProcessCount()); // 1

        // ---------------------------------------------------------
        // staticが適切な例: 純粋な計算
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== staticが適切: 純粋な計算メソッド ===");

        // 状態を持たないので、いつ呼んでも同じ引数なら同じ結果が返る
        System.out.println("max(3, 7) = " + MathHelper.max(3, 7));
        System.out.println("max(3, 7) = " + MathHelper.max(3, 7)); // 何度呼んでも同じ
        System.out.printf("半径5の円の面積: %.2f%n", MathHelper.circleArea(5));

        // ---------------------------------------------------------
        // @Override アノテーションの体験
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== @Override アノテーションの体験 ===");

        DogWithoutAnnotation dogWithout = new DogWithoutAnnotation();
        DogWithAnnotation dogWith       = new DogWithAnnotation();

        // toString() を呼んでいる（System.out.println は内部で toString() を呼ぶ）
        System.out.println("@Override なし: " + dogWithout);
        // → Object クラスの toString() が呼ばれる（DogWithoutAnnotation@xxxxxx のようなアドレス表示）
        // → tostring() （小文字のs）は別メソッドとして存在するが、toString() はオーバーライドされていない

        System.out.println("@Override あり: " + dogWith);
        // → DogWithAnnotation の toString() が呼ばれる（意図通りのオーバーライド）

        // @Override の教訓: アノテーションはコンパイラへの「意図の宣言」
        // 意図を宣言することで、コンパイラが「その意図が実現できているか」を確認してくれる
        System.out.println();
        System.out.println("@Override の教訓:");
        System.out.println("  @Override なし → tostring() を書いても別メソッド扱い（バグに気づけない）");
        System.out.println("  @Override あり → tostring() を書くとコンパイルエラー（バグをすぐ発見）");

        // ---------------------------------------------------------
        // @FunctionalInterface の体験
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== @FunctionalInterface アノテーションの体験 ===");

        // SingleMethod はラムダ式で実装できる（抽象メソッドが1つなので）
        // [Java 7 不可] ラムダ式は Java 8 以降
        SingleMethod action = () -> System.out.println("SingleMethod をラムダで実装！");
        action.doSomething();

        // TwoMethods は抽象メソッドが2つあるのでラムダでは実装できない
        // （ラムダ式は「どちらのメソッドを実装しているか」が特定できないため）
        TwoMethods two = new TwoMethods() {
            @Override public void first()  { System.out.println("first()"); }
            @Override public void second() { System.out.println("second()"); }
        };
        two.first();
        two.second();

        System.out.println();
        System.out.println("@FunctionalInterface の教訓:");
        System.out.println("  抽象メソッドが1つ → ラムダ式で実装できる関数型インターフェース");
        System.out.println("  @FunctionalInterface を付けると、2つ以上書いたときにコンパイルエラーで守られる");
    }
}
