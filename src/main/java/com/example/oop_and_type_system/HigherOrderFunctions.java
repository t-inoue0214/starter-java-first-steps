/**
 * 【なぜこのコードを学ぶのか】
 * 「名前で絞り込む」「長さで絞り込む」など絞り込みロジックだけが異なる
 * メソッドを複数本書くと、構造がほぼ同じコードが増殖する。
 * 振る舞い（ラムダ式）を引数として受け取る「高階関数」にまとめると
 * 1本のメソッドで全ケースに対応できる。
 * これが Stream API の .filter() や .map() の正体だ。
 */
package com.example.oop_and_type_system;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class HigherOrderFunctions {

    // ---------------------------------------------------------
    // ========== Before: 絞り込みロジックごとに別メソッドを定義（重複だらけ） ==========
    // ---------------------------------------------------------

    // 「名前に 'A' が含まれるものだけ残す」専用メソッド
    private static List<String> filterByContainsA(List<String> list) {
        ArrayList<String> result = new ArrayList<>();
        for (String s : list) {
            // ここの条件だけが他のメソッドと違う
            if (s.contains("A") || s.contains("a")) {
                result.add(s);
            }
        }
        return result;
    }

    // 「4文字以上の名前だけ残す」専用メソッド
    // → filterByContainsA とループ構造がまったく同じ！ 違いは if の条件だけ
    private static List<String> filterByLengthAtLeast4(List<String> list) {
        ArrayList<String> result = new ArrayList<>();
        for (String s : list) {
            // ここの条件だけが違う（コードの重複が増えていく）
            if (s.length() >= 4) {
                result.add(s);
            }
        }
        return result;
    }

    // ---------------------------------------------------------
    // ========== After: 振る舞いを引数で受け取る汎用高階関数 ==========
    // ---------------------------------------------------------

    // Predicate<T> を引数に受け取ることで、条件を外から「差し込める」
    // これが「高階関数」—関数を引数として受け取る関数のこと
    // [Java 7 不可] Predicate<T> は Java 8 以降。Java 7 では独自インターフェースを定義して代替する
    private static <T> List<T> filter(List<T> list, Predicate<T> condition) {
        ArrayList<T> result = new ArrayList<>();
        for (T item : list) {
            // condition.test() で渡されたラムダ式を実行する
            if (condition.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    // ---------------------------------------------------------
    // 戻り値で「関数」を返す例（関数ファクトリ）
    // ---------------------------------------------------------

    // 税率を受け取り、「税込み金額を計算する関数」を返す
    // 返り値が Function<Integer, Integer>（整数→整数の関数）であることに注目
    private static Function<Integer, Integer> taxCalculator(double rate) {
        // ラムダ式をそのまま return できる
        return price -> (int) (price * (1 + rate));
    }

    public static void main(String[] args) {

        // [Java 7 不可] List.of() は Java 9 以降
        List<String> names = List.of("Alice", "Bob", "Charlie", "Dave", "Anna", "Ed");

        // ---------------------------------------------------------
        // Before: 専用メソッドを呼ぶ（メソッドが増殖する問題）
        // ---------------------------------------------------------
        System.out.println("=== Before: 専用メソッドを呼ぶ ===");

        List<String> withA = filterByContainsA(names);
        System.out.println("'a'を含む名前: " + withA);

        List<String> longNames = filterByLengthAtLeast4(names);
        System.out.println("4文字以上の名前: " + longNames);

        // 「3文字以下の名前が欲しい」という新しい条件が来たら
        // さらに filterByLengthAtMost3() を追加するのか…？（コードが増殖し続ける）

        // ---------------------------------------------------------
        // After: 汎用 filter() にラムダを渡す（1本のメソッドで全対応）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== After: 汎用 filter() にラムダを渡す ===");

        // 'a' を含む名前だけ絞り込む（条件はラムダで渡す）
        List<String> withA2 = filter(names, s -> s.contains("A") || s.contains("a"));
        System.out.println("'a'を含む名前: " + withA2);

        // 4文字以上の名前だけ絞り込む（条件だけが違う、メソッドは同じ）
        List<String> longNames2 = filter(names, s -> s.length() >= 4);
        System.out.println("4文字以上の名前: " + longNames2);

        // 3文字以下の名前も、追加メソッド不要で対応できる
        List<String> shortNames = filter(names, s -> s.length() <= 3);
        System.out.println("3文字以下の名前: " + shortNames);

        // 整数のリストにも同じ filter() が使える（ジェネリクスのおかげ）
        // [Java 7 不可] List.of() は Java 9 以降
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> evens = filter(numbers, n -> n % 2 == 0);
        System.out.println("偶数だけ: " + evens);

        // ---------------------------------------------------------
        // 戻り値が関数の例: taxCalculator
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 戻り値が関数: taxCalculator ===");

        // 10% 税率の計算関数を生成する
        Function<Integer, Integer> tax10 = taxCalculator(0.10);
        // 8% 税率（軽減税率）の計算関数を生成する
        Function<Integer, Integer> tax8  = taxCalculator(0.08);

        int priceA = 1000;
        int priceB = 500;

        // 生成した関数を apply() で呼び出す
        System.out.println(priceA + "円 → 10%税込: " + tax10.apply(priceA) + "円");
        System.out.println(priceA + "円 →  8%税込: " + tax8.apply(priceA) + "円");
        System.out.println(priceB + "円 → 10%税込: " + tax10.apply(priceB) + "円");

        // ---------------------------------------------------------
        // まとめ: Stream API との関係
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== Stream API との関係 ===");

        // この filter(list, predicate) の考え方が、
        // 第08章で学ぶ stream.filter(predicate).map(function).forEach(consumer) の正体です。
        // ここで作った「条件をラムダで渡す」パターンは Stream API そのものの設計思想だ。
        // 以下は Java 標準の Stream API を使った同等の処理:
        // [Java 7 不可] Stream API は Java 8 以降。toList() は Java 16 以降
        List<String> result = names.stream()
                          .filter(s -> s.length() >= 4)    // 高階関数: Predicate を受け取る
                          .map(String::toUpperCase)         // 高階関数: Function を受け取る
                          .toList();
        System.out.println("Stream APIで4文字以上を大文字に: " + result);
        System.out.println("→ この .filter() / .map() の仕組みは、今学んだ高階関数そのものです。");
    }
}
