/**
 * 【なぜこのコードを学ぶのか】
 * Arrays.sort() は単純なマージソートではなく「TimSort」という高度な実装で、
 * 現実のデータ（ほぼソート済み・重複が多い等）に最適化されている。
 * 自前でソートを実装する理由はほぼない。
 * 第07章で学んだ Comparator と組み合わせることで、あらゆる基準でソートできる。
 */
package com.example.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StandardLibrarySort {

    // ---------------------------------------------------------
    // Product クラス: ソートの対象となる商品データ
    // 第07章 ComparatorDemo.java と同じ構造で定義する
    // ---------------------------------------------------------
    static class Product {
        private final String name;
        private final int price;
        private final String category;

        Product(String name, int price, String category) {
            this.name     = name;
            this.price    = price;
            this.category = category;
        }

        public String getName()     { return name; }
        public int getPrice()       { return price; }
        public String getCategory() { return category; }

        @Override
        public String toString() {
            return name + "(" + price + "円)";
        }
    }

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // セクション1: Arrays.sort() — プリミティブ配列のソート
        // ========== Arrays.sort(): プリミティブ配列のソート ==========
        // Arrays.sort() の内部実装（Java 7+）: TimSort（挿入ソート + マージソート の複合）
        // ---------------------------------------------------------
        System.out.println("=== セクション1: Arrays.sort() — プリミティブ配列のソート ===");
        System.out.println();

        // int[] の昇順ソート
        int[] numbers = {64, 25, 12, 22, 11, 90, 3, 47};
        System.out.println("ソート前: " + Arrays.toString(numbers));

        Arrays.sort(numbers); // 破壊的操作（元の配列を直接変更する）

        System.out.println("ソート後: " + Arrays.toString(numbers));
        System.out.println();

        // String[] の辞書順ソート
        String[] fruits = {"バナナ", "りんご", "ぶどう", "みかん", "いちご"};
        System.out.println("ソート前: " + Arrays.toString(fruits));

        Arrays.sort(fruits); // 文字列は Comparable を実装しているため自動的に辞書順ソート

        System.out.println("ソート後（辞書順）: " + Arrays.toString(fruits));
        System.out.println();

        // Arrays.sort(arr, fromIndex, toIndex) で部分ソート
        int[] partial = {5, 3, 8, 1, 9, 2, 7, 4};
        System.out.println("部分ソート前: " + Arrays.toString(partial));

        // インデックス 2〜5（2, 3, 4, 5: toIndex は含まない）だけソートする
        Arrays.sort(partial, 2, 6);

        System.out.println("部分ソート後（インデックス2〜5のみ）: " + Arrays.toString(partial));
        System.out.println();

        // ---------------------------------------------------------
        // セクション2: Collections.sort() と List.sort() — リストのソート
        // ========== Collections.sort() / List.sort(): リストのソート ==========
        // ---------------------------------------------------------
        System.out.println("=== セクション2: Collections.sort() / List.sort() ===");
        System.out.println();

        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
        //   ArrayList<String> cities = new ArrayList<>(Arrays.asList("大阪", "東京", "名古屋", "福岡", "札幌"));
        ArrayList<String> cities = new ArrayList<>(List.of("大阪", "東京", "名古屋", "福岡", "札幌"));

        System.out.println("ソート前: " + cities);

        Collections.sort(cities); // 辞書順ソート

        System.out.println("Collections.sort 後: " + cities);

        // Collections.sort(list) と list.sort(null) は等価
        // [Java 7 不可] List.sort() は Java 8 以降。Java 7 では Collections.sort() を使う
        cities.sort(null); // null を渡すと要素の自然順序（Comparable）を使う

        System.out.println("list.sort(null) 後（結果同じ）: " + cities);
        System.out.println();

        // ---------------------------------------------------------
        // セクション3: Comparator を使ったカスタムソート
        // 第07章 ComparatorDemo.java で学んだ Comparator の応用
        // ========== Comparator でカスタムソート ==========
        // ---------------------------------------------------------
        System.out.println("=== セクション3: Comparator でカスタムソート ===");
        System.out.println("（第07章 ComparatorDemo.java で学んだ Comparator の応用）");
        System.out.println();

        // テストデータ: 商品リスト
        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
        //   ArrayList<Product> products = new ArrayList<>(Arrays.asList(
        //       new Product("キーボード", 8_000, "PC周辺機器"), ...));
        ArrayList<Product> products = new ArrayList<>(List.of(
            new Product("キーボード",    8_000, "PC周辺機器"),
            new Product("マウス",        3_000, "PC周辺機器"),
            new Product("モニター",     45_000, "ディスプレイ"),
            new Product("ウェブカメラ",  6_000, "PC周辺機器"),
            new Product("スピーカー",   12_000, "オーディオ"),
            new Product("ヘッドセット",  9_000, "オーディオ")
        ));

        // 価格昇順（ラムダ式）
        // [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では匿名クラスで書く:
        //   products.sort(new Comparator<Product>() {
        //       @Override public int compare(Product a, Product b) { return a.getPrice() - b.getPrice(); }
        //   });
        ArrayList<Product> byPrice = new ArrayList<>(products);
        byPrice.sort((a, b) -> a.getPrice() - b.getPrice());
        System.out.println("価格昇順（ラムダ式）: " + byPrice);

        // 名前順（メソッド参照）
        // [Java 7 不可] Comparator.comparing() とメソッド参照（::）は Java 8 以降。
        //   Java 7 では匿名クラスで書く:
        //   products.sort(new Comparator<Product>() {
        //       @Override public int compare(Product a, Product b) { return a.getName().compareTo(b.getName()); }
        //   });
        ArrayList<Product> byName = new ArrayList<>(products);
        byName.sort(Comparator.comparing(Product::getName));
        System.out.println("名前順（メソッド参照）: " + byName);

        // 複合ソート: カテゴリ昇順 → 価格昇順
        // thenComparing で「同じカテゴリ内では価格順」という2段階ソートを表現する
        ArrayList<Product> byCategory = new ArrayList<>(products);
        byCategory.sort(
            Comparator.comparing(Product::getCategory)
                      .thenComparing(Product::getPrice)
        );
        System.out.println("複合ソート（カテゴリ昇順→価格昇順）:");
        // [Java 7 不可] forEach + ラムダは Java 8 以降。Java 7 では拡張 for ループで書く:
        //   for (Product p : byCategory) { System.out.println("  " + p); }
        byCategory.forEach(p -> System.out.println("  " + p));

        System.out.println();

        // ---------------------------------------------------------
        // セクション4: Arrays.binarySearch() — ソート済み配列への高速検索
        // ========== Arrays.binarySearch(): ソート済み配列への高速検索 ==========
        // ※ 必ずソート後に呼ぶこと。未ソートに使うと結果は不定
        // ---------------------------------------------------------
        System.out.println("=== セクション4: Arrays.binarySearch() ===");
        System.out.println();

        int[] searchArray = {3, 7, 15, 22, 38, 54, 67, 81, 90, 99};
        System.out.println("配列（ソート済み）: " + Arrays.toString(searchArray));

        // ソート後に Arrays.binarySearch() で検索する
        int found1 = Arrays.binarySearch(searchArray, 54);
        System.out.println("Arrays.binarySearch(arr, 54) → インデックス: " + found1); // 5

        int found2 = Arrays.binarySearch(searchArray, 15);
        System.out.println("Arrays.binarySearch(arr, 15) → インデックス: " + found2); // 2

        // 存在しない値は負の値が返る（挿入すべき位置の情報が含まれる）
        int notFound = Arrays.binarySearch(searchArray, 50);
        System.out.println("Arrays.binarySearch(arr, 50) → " + notFound + " （負の値 = 見つからない）");
        System.out.println();

        // ソート前に使うと誤動作する
        System.out.println("--- 注意: ソート前に使うと誤動作する ---");
        int[] unsortedArr = {90, 7, 54, 22, 3, 99};
        System.out.println("未ソート配列: " + Arrays.toString(unsortedArr));
        int wrongResult = Arrays.binarySearch(unsortedArr, 54);
        // 「見つからない」または「誤った位置」が返る（動作は不定）
        System.out.println("未ソートに Arrays.binarySearch(arr, 54) → " + wrongResult + " （誤り・不定）");
        System.out.println("→ 必ず Arrays.sort(arr) を先に実行してから Arrays.binarySearch() を呼ぶ");
        System.out.println();

        // 正しい使い方: ソートしてから検索する
        int[] sortFirst = {90, 7, 54, 22, 3, 99};
        Arrays.sort(sortFirst);
        int correctResult = Arrays.binarySearch(sortFirst, 54);
        System.out.println("ソート後の配列: " + Arrays.toString(sortFirst));
        System.out.println("Arrays.sort() → Arrays.binarySearch(arr, 54) → インデックス: " + correctResult);
        System.out.println();

        // ---------------------------------------------------------
        // セクション5: まとめ
        // ---------------------------------------------------------
        System.out.println("=== 現場での指針 ===");
        System.out.println("int[] / double[] のソート : Arrays.sort(arr)");
        System.out.println("List<T> のソート          : list.sort(comparator) または Collections.sort(list)");
        System.out.println("オブジェクトのカスタムソート: Comparator.comparing() + thenComparing()");
        System.out.println("ソート済み配列の検索      : Arrays.binarySearch(arr, target)");
        System.out.println("→ ソートは自分で実装しない。標準ライブラリが最速かつ最も安全");
    }
}
