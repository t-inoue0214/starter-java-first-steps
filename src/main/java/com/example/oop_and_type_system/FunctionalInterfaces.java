/**
 * 【なぜこのコードを学ぶのか】
 * 「絞り込む」「変換する」「消費する」「生成する」という4つの振る舞いパターンは
 * どんなプログラムにも共通する。JavaはこれらをPredicate/Function/Consumer/Supplierとして
 * 標準提供しており、ラムダ式と組み合わせることで「振る舞いを変数に代入する」
 * プログラミングスタイルが使えるようになる。これがStream APIの土台になっている。
 *
 * このファイルは Before/After 形式ではなく、「新しい道具を手に取る」体験として設計している。
 * ラムダ式（LambdaBasics.java）が「短く書く手段」なら、
 * こちらは「何を渡すかを型として表現する手段」の習得を目的とする。
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

    // ---------------------------------------------------------
    // 【用語の定義: インターフェース・抽象メソッド】（このファイルで初登場）
    // ---------------------------------------------------------
    // ■ インターフェース（interface）クラスとは
    //   「何ができるか」という約束事（仕様）だけを書いて、実装はクラスに任せる仕組み。
    //   クラスが「この振る舞いができる」と宣言するために使う。
    //   class ではなく interface キーワードで定義する。
    //
    // ■ 抽象メソッド（abstract method）とは
    //   本体 {} を持たないメソッド宣言のこと。
    //   「こういう名前・引数・戻り値のメソッドを用意してね」という約束だけを定義する。
    //   インターフェース内のメソッドはデフォルトで抽象メソッドになる。
    //
    //   interface Greeter {
    //       String greet(String name);  ← 本体 {} がない = 抽象メソッド
    //   }
    //   このインターフェースを implements したクラスは
    //   greet() の中身を必ず実装しなければコンパイルエラーになる。
    //   「約束を守っているか」をコンパイラが保証してくれる—それがインターフェースの価値。
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
        // [Java 7 不可] Predicate<T> は Java 8 以降の関数型インターフェース
        Predicate<String> isLong = s -> s.length() > 3;

        // test() で呼び出す
        System.out.println("'Hi' は長い？  " + isLong.test("Hi"));      // false
        System.out.println("'Hello' は長い？ " + isLong.test("Hello")); // true

        // なぜ Predicate に and() / or() / negate() があるのか:
        // Predicate は「条件をラムダ式で入れられる箱」です。
        // 複数の箱を「AND でつなぐ」「OR でつなぐ」「反転する」ためのメソッドが最初から用意されています。
        // 自分で if (a && b) と書く代わりに、条件を「部品化」して組み合わせられます。

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
        // [Java 7 不可] Function<T,R> は Java 8 以降の関数型インターフェース
        // [Java 7 不可] メソッド参照は Java 8 以降
        Function<String, Integer> toLength = String::length; // メソッド参照で書ける

        // apply() で呼び出す
        System.out.println("'Hello' の文字数: " + toLength.apply("Hello")); // 5
        System.out.println("'Java' の文字数:  " + toLength.apply("Java"));  // 4

        // Function.andThen() は「この変換をしてから、さらにその変換をする」をつなげる仕組みです。
        // パイプライン処理（前の結果を次の処理に渡す）を表現できます。

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
        // [Java 7 不可] Consumer<T> は Java 8 以降の関数型インターフェース
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
        // [Java 7 不可] Supplier<T> は Java 8 以降の関数型インターフェース
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
        Greeter[] greeters = new Greeter[]{ japanese, english };
        for (Greeter g : greeters) {
            System.out.println(g.greet("Bob"));
        }
    }
}
