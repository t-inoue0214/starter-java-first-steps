/**
 * 【なぜこのコードを学ぶのか】
 * ラムダ式は「匿名クラスの省略形」だ。
 * forループで毎回同じ構造（ループの枠組み）を書く代わりに、
 * 「何をするか（振る舞い）」だけを切り出してメソッドに渡せる。
 * 同じ処理が3段階でどう短くなるかを並べて体験することで、
 * なぜ現場でラムダ式が使われるかが腑に落ちる。
 */
package com.example.oop_and_type_system;

import java.util.List;
import java.util.function.Consumer;

public class LambdaBasics {

    public static void main(String[] args) {

        var names = List.of("Alice", "Bob", "Charlie", "Dave");

        // ---------------------------------------------------------
        // 段階1: 通常のforループ（古典的な書き方）
        // ---------------------------------------------------------
        System.out.println("=== Step1 Before: 通常のforループ ===");

        // 「ループの枠組み」と「何をするか（println）」が混在している
        for (String name : names) {
            System.out.println(name);
        }

        // ---------------------------------------------------------
        // 段階2: 匿名クラスを使った forEach（中間段階）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== Step2 中間: 匿名クラスで forEach ===");

        // forEach は「各要素に対して何かをする」というConsumer<T>を受け取る
        // Consumer<T> は関数型インターフェース（詳しくは FunctionalInterfaces.java で学ぶ）
        names.forEach(new Consumer<String>() {
            @Override
            public void accept(String name) {
                // ここが「何をするか」の部分だが、
                // 毎回 new Consumer<>() { public void accept(...) {} } と書くのは冗長
                System.out.println(name);
            }
        });

        // ---------------------------------------------------------
        // 段階3: ラムダ式で簡潔に書く（After）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== Step3 After: ラムダ式 ===");

        // name -> System.out.println(name) が Consumer<String> の匿名クラスと同じ意味
        // 「引数 -> 処理」という形が ラムダ式の基本構文
        names.forEach(name -> System.out.println(name));

        // ---------------------------------------------------------
        // 段階4: メソッド参照でさらに短く（After2）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== Step4 After2: メソッド参照 ===");

        // 「name -> System.out.println(name)」は
        // 「引数をそのまま println に渡すだけ」なので、メソッド参照で書ける
        // クラス名::メソッド名 の形式（:: がメソッド参照の記号）
        names.forEach(System.out::println);

        // ---------------------------------------------------------
        // 対比: 「偶数だけ表示する」処理をfor+if版とラムダ版で並べる
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 対比: 偶数だけ表示（for+if版 vs ラムダ版） ===");

        var numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // --- for + if 版（ループの枠組みと条件判定が混在している） ---
        System.out.print("for+if版: ");
        for (int n : numbers) {
            if (n % 2 == 0) {
                System.out.print(n + " ");
            }
        }
        System.out.println();

        // --- ラムダ版（「絞り込む」と「表示する」を分けて読める） ---
        // stream().filter() で条件に合う要素だけを通し、forEach() で表示する
        // 「何をするか」の意図がコードの流れとして読める
        System.out.print("ラムダ版:  ");
        numbers.stream()
               .filter(n -> n % 2 == 0)    // 偶数だけ通す
               .forEach(n -> System.out.print(n + " ")); // 表示する
        System.out.println();

        // ---------------------------------------------------------
        // ラムダ式の基本構文まとめ
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== ラムダ式の基本構文パターン ===");

        // パターン1: 引数なし
        Runnable greet = () -> System.out.println("こんにちは");
        greet.run();

        // パターン2: 引数1つ（括弧は省略できる）
        Consumer<String> shout = msg -> System.out.println(msg.toUpperCase());
        shout.accept("hello");

        // パターン3: 引数2つ以上（括弧が必要）
        // Comparator は2つの引数を受け取る関数型インターフェース
        java.util.Comparator<String> byLength = (a, b) -> a.length() - b.length();
        var sorted = names.stream().sorted(byLength).toList();
        System.out.println("長さ順に並び替え: " + sorted);

        // パターン4: 処理が複数行の場合はブロック {}を使う
        Consumer<Integer> classify = num -> {
            // 複数の処理が必要なときは {} で囲む
            if (num > 0) {
                System.out.println(num + " は正の数");
            } else {
                System.out.println(num + " は0以下");
            }
        };
        classify.accept(5);
        classify.accept(-3);
    }
}
