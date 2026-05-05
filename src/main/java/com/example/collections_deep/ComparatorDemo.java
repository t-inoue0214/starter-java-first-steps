/**
 * 【なぜこのコードを学ぶのか】
 * Comparator は「比較の基準」を外から渡す仕組み。
 * 第06章で学んだ高階関数・ラムダ式と同じ考え方で、「どう並べるか」という振る舞いを引数として渡せる。
 * Comparable（クラス内部にソート基準を固定）との違いを体験することで、
 * 「外から振る舞いを差し込む」設計の柔軟さを実感する。
 */
package com.example.collections_deep;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComparatorDemo {

    // ---------------------------------------------------------
    // Product クラス: ソートの対象となる商品データ
    // （同一ファイルに static クラスとして定義する）
    // ---------------------------------------------------------
    static class Product implements Comparable<Product> {

        private final String name;
        private final int price;
        private final String category;

        Product(String name, int price, String category) {
            this.name = name;
            this.price = price;
            this.category = category;
        }

        public String getName()     { return name; }
        public int getPrice()       { return price; }
        public String getCategory() { return category; }

        // ---------------------------------------------------------
        // Comparable の compareTo: クラス内部にソート基準を「固定」する
        // これが Before（古いやり方）の問題点になる
        // ---------------------------------------------------------
        @Override
        public int compareTo(Product other) {
            // 価格の昇順をこのクラスの「自然順序」として固定してしまう
            // → 名前順でソートしたい場合はどうする? 別のクラスを作るしかない
            return Integer.compare(this.price, other.price);
        }

        @Override
        public String toString() {
            return name + "(" + category + ", " + price + "円)";
        }
    }

    public static void main(String[] args) {

        // テストデータ: 商品リスト
        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
        //   List<Product> productList = Arrays.asList(new Product("キーボード", 8_000, "PC周辺機器"), ...);
        //   ArrayList<Product> products = new ArrayList<>(productList);
        ArrayList<Product> products = new ArrayList<>(List.of(
            new Product("キーボード",   8_000, "PC周辺機器"),
            new Product("マウス",       3_000, "PC周辺機器"),
            new Product("モニター",    45_000, "ディスプレイ"),
            new Product("ウェブカメラ", 6_000, "PC周辺機器"),
            new Product("スピーカー",  12_000, "オーディオ"),
            new Product("ヘッドセット", 9_000, "オーディオ")
        ));

        // =========================================================
        // ========== Before: Comparable でソート基準をクラス内部に固定 ==========
        // =========================================================

        System.out.println("=== Before: Comparable（ソート基準がクラス内部に固定） ===");
        System.out.println();

        // compareTo() の実装（価格昇順）が使われる
        ArrayList<Product> byNaturalOrder = new ArrayList<>(products);
        byNaturalOrder.sort(null); // null を渡すと Comparable の compareTo を使う

        System.out.println("Comparable（自然順序 = 価格昇順）:");
        byNaturalOrder.forEach(p -> System.out.println("  " + p));

        System.out.println();
        System.out.println("問題点: 名前順でソートしたい場合は?");
        System.out.println("→ compareTo を変えると価格順ソートが壊れる");
        System.out.println("→ Comparable では「1種類のソート基準しか持てない」という制約がある");

        System.out.println();

        // =========================================================
        // ========== After: Comparator をラムダで渡す ==========
        // =========================================================

        System.out.println("=== After: Comparator（ソート基準を外から渡す） ===");
        System.out.println();

        // ---------------------------------------------------------
        // ラムダ式で価格昇順
        // ---------------------------------------------------------
        System.out.println("--- 価格昇順（ラムダ式）---");

        ArrayList<Product> byPriceAsc = new ArrayList<>(products);
        // (a, b) -> ... は Comparator<Product> をラムダで渡している
        // 第06章で学んだ「振る舞いを引数として渡す高階関数」と同じ考え方
        // [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では匿名クラスで書く:
        //   byPriceAsc.sort(new Comparator<Product>() {
        //       @Override public int compare(Product a, Product b) { return a.getPrice() - b.getPrice(); }
        //   });
        byPriceAsc.sort((a, b) -> a.getPrice() - b.getPrice());

        byPriceAsc.forEach(p -> System.out.println("  " + p));

        System.out.println();

        // ---------------------------------------------------------
        // Comparator.comparing でメソッド参照を使う（より読みやすい書き方）
        // ---------------------------------------------------------
        System.out.println("--- 名前順（Comparator.comparing + メソッド参照）---");

        ArrayList<Product> byName = new ArrayList<>(products);
        // Comparator.comparing は「何のキーで比べるか」をメソッド参照で渡す
        // Product::getName は「Product から name を取り出す Function<Product, String>」
        // [Java 7 不可] Comparator.comparing() とメソッド参照は Java 8 以降。Java 7 では匿名クラスで書く:
        //   byName.sort(new Comparator<Product>() {
        //       @Override public int compare(Product a, Product b) { return a.getName().compareTo(b.getName()); }
        //   });
        byName.sort(Comparator.comparing(Product::getName));

        byName.forEach(p -> System.out.println("  " + p));

        System.out.println();

        // ---------------------------------------------------------
        // reversed() で降順にする
        // ---------------------------------------------------------
        System.out.println("--- 価格降順（Comparator.comparing + reversed）---");

        ArrayList<Product> byPriceDesc = new ArrayList<>(products);
        // メソッドチェーンで reversed() を付けるだけで逆順になる
        byPriceDesc.sort(Comparator.comparing(Product::getPrice).reversed());

        byPriceDesc.forEach(p -> System.out.println("  " + p));

        System.out.println();

        // ---------------------------------------------------------
        // 複合ソート: カテゴリ昇順 → 価格昇順（2段階ソート）
        // ---------------------------------------------------------
        System.out.println("--- 複合ソート（カテゴリ昇順 → 価格昇順）---");

        ArrayList<Product> byCategory = new ArrayList<>(products);
        // thenComparing で「同じカテゴリ内では価格順」という2段階ソートを表現する
        // 複数の「比較基準」をメソッドチェーンでつなぐだけでよい
        byCategory.sort(
            Comparator.comparing(Product::getCategory)
                      .thenComparing(Product::getPrice)
        );

        byCategory.forEach(p -> System.out.println("  " + p));

        System.out.println();

        // ---------------------------------------------------------
        // 第06章との接続: Comparator はまさに高階関数の応用
        // ---------------------------------------------------------
        System.out.println("=== 第06章との接続 ===");
        System.out.println();
        System.out.println("Comparator<T> は関数型インターフェース:");
        System.out.println("  compare(T o1, T o2) という抽象メソッドを1つ持つだけ");
        System.out.println();
        System.out.println("list.sort(comparator) は高階関数:");
        System.out.println("  「どう比べるか」という振る舞いをラムダ（= Comparator）として受け取る");
        System.out.println();
        System.out.println("これは第06章で学んだ Predicate / Function / Consumer と同じ考え方。");
        System.out.println("「ロジックを外から差し込む」ことで、sort メソッド自体を変えずに");
        System.out.println("価格順・名前順・カテゴリ順など何通りでも並べ替えられる。");

        // ---------------------------------------------------------
        // おまけ: Comparator を変数に保存して再利用する
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("--- おまけ: Comparator を変数に保存して再利用 ---");

        // 「カテゴリ → 価格」の複合ソートを変数に保存する
        Comparator<Product> catalogOrder =
            Comparator.comparing(Product::getCategory)
                      .thenComparing(Product::getPrice);

        // 同じ Comparator を複数箇所で再利用できる
        ArrayList<Product> sorted1 = new ArrayList<>(products);
        sorted1.sort(catalogOrder);
        System.out.println("catalogOrder で並べ替えた最初の商品: " + sorted1.get(0));

        // 逆順も簡単に作れる
        ArrayList<Product> sorted2 = new ArrayList<>(products);
        sorted2.sort(catalogOrder.reversed());
        System.out.println("catalogOrder.reversed() で並べ替えた最初の商品: " + sorted2.get(0));
    }
}
