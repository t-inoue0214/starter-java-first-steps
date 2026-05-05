/**
 * 【なぜこのコードを学ぶのか】
 * 「絞り込む」「変換する」「消費する」「生成する」という4つの振る舞いパターンは
 * どんなプログラムにも共通する。JavaはこれらをPredicate/Function/Consumer/Supplierとして
 * 標準提供しており、ラムダ式と組み合わせることで「振る舞いを変数に代入する」
 * プログラミングスタイルが使えるようになる。これがStream APIの土台になっている。
 */
package com.example.oop_and_type_system;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionalInterfaces {

    // ---------------------------------------------------------
    // 自作の関数型インターフェース
    // ---------------------------------------------------------

    // @FunctionalInterface を付けると「抽象メソッドが1つだけ」であることをコンパイラが保証する
    // もし抽象メソッドを2つ書こうとするとコンパイルエラーになる（下で説明）
    @FunctionalInterface
    interface Greeter {
        // 「名前を受け取って挨拶文を返す」振る舞いを表すインターフェース
        String greet(String name);

        // もう1つ抽象メソッドを追加しようとするとコンパイルエラー:
        // "Invalid '@FunctionalInterface' annotation; Greeter is not a functional interface"
        // String farewell(String name); // ← コメントアウトしているが、外すとコンパイルエラー
    }

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // 1. Predicate<T>: 条件を判定して true/false を返す
        // ---------------------------------------------------------
        System.out.println("=== 1. Predicate<T>: 条件判定 ===");

        // 文字列が3文字より長いか判定する Predicate
        // T = String、戻り値は boolean
        Predicate<String> isLong = s -> s.length() > 3;

        // test() で呼び出す
        System.out.println("'Hi' は長い？  " + isLong.test("Hi"));      // false
        System.out.println("'Hello' は長い？ " + isLong.test("Hello")); // true

        // Predicateは and() / or() / negate() で組み合わせられる
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> isLongAndStartsWithA = isLong.and(startsWithA);
        System.out.println("'Alice' は長くてAで始まる？ " + isLongAndStartsWithA.test("Alice")); // true
        System.out.println("'Bob' は長くてAで始まる？   " + isLongAndStartsWithA.test("Bob"));   // false

        // ---------------------------------------------------------
        // 2. Function<T, R>: 値を受け取って別の型の値に変換する
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 2. Function<T, R>: 変換 ===");

        // T = String（入力）、R = Integer（出力）
        // 文字列を受け取って、その文字数（整数）を返す関数
        Function<String, Integer> toLength = String::length; // メソッド参照で書ける

        // apply() で呼び出す
        System.out.println("'Hello' の文字数: " + toLength.apply("Hello")); // 5
        System.out.println("'Java' の文字数:  " + toLength.apply("Java"));  // 4

        // Function は andThen() で連鎖できる
        // 文字数を取得した後、さらに「偶数なら"偶数"、奇数なら"奇数"」に変換する
        Function<Integer, String> evenOrOdd = n -> n % 2 == 0 ? "偶数" : "奇数";
        Function<String, String> lengthParity = toLength.andThen(evenOrOdd);
        System.out.println("'Hello' の文字数は偶数か奇数か: " + lengthParity.apply("Hello")); // 奇数
        System.out.println("'Java' の文字数は偶数か奇数か:  " + lengthParity.apply("Java"));  // 偶数

        // ---------------------------------------------------------
        // 3. Consumer<T>: 値を受け取って「消費」する（戻り値なし・副作用専用）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 3. Consumer<T>: 消費（副作用） ===");

        // T = String、戻り値は void（何も返さない）
        // 「副作用」とは画面表示・ファイル書き込みなど、値を返す以外の処理のこと
        Consumer<String> printer = System.out::println;

        // accept() で呼び出す
        printer.accept("Consumer から出力: こんにちは");

        // 整形して出力する Consumer
        Consumer<String> fancyPrinter = msg -> System.out.println(">>> " + msg + " <<<");
        fancyPrinter.accept("重要なメッセージ");

        // Consumer は andThen() で連鎖できる（両方とも実行される）
        Consumer<String> both = printer.andThen(fancyPrinter);
        both.accept("2つのConsumerを連鎖");

        // ---------------------------------------------------------
        // 4. Supplier<T>: 引数なしで値を「生成」して返す
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 4. Supplier<T>: 生成 ===");

        // 引数なし、T = String の値を返す
        // 遅延評価（必要になったタイミングで値を生成する）に役立つ
        Supplier<String> greeting = () -> "こんにちは、Javaの世界へ！";

        // get() で呼び出す
        System.out.println(greeting.get());

        // Supplier の実用例: 「デフォルト値を遅延生成する」
        // 重い処理（DBアクセスなど）をいきなり実行せず、必要になってから呼ぶ
        Supplier<String> heavyDefault = () -> {
            // 本来ここに重い処理が入る（例: DBからデフォルト設定を読む）
            return "（遅延生成されたデフォルト値）";
        };
        System.out.println("デフォルト: " + heavyDefault.get());

        // ---------------------------------------------------------
        // 自作 @FunctionalInterface の体験
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 自作 @FunctionalInterface の体験 ===");

        // Greeter インターフェースをラムダ式で実装できる
        Greeter japanese = name -> "こんにちは、" + name + "さん！";
        Greeter english  = name -> "Hello, " + name + "!";

        // greet() で呼び出す
        System.out.println(japanese.greet("田中"));
        System.out.println(english.greet("Alice"));

        // 配列に入れて切り替えることもできる（振る舞いの差し替え）
        var greeters = new Greeter[]{ japanese, english };
        for (var g : greeters) {
            System.out.println(g.greet("Bob"));
        }
    }
}
