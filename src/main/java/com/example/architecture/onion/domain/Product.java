/**
 * 【なぜこのコードを学ぶのか】
 * ドメインオブジェクトはビジネスの「言葉」をそのままコードで表現する。
 * 「商品（Product）」は id・name・price を持つ——これは DB の都合でも画面の都合でもなく、
 * ビジネスとして自然に決まる概念だ。
 * Record を使うことで、不変オブジェクト（Immutable）・equals/hashCode・toString を
 * 自動生成でき、ボイラープレートコードを大幅に削減できる。
 * コンパクトコンストラクタでバリデーションを書くことで、
 * 「不正な商品」が絶対に存在できない強固なドメインモデルを実現する。
 */
package com.example.architecture.onion.domain;

// [Java 7 不可] Record は Java 16 以降。Java 7〜15 では通常クラスで書く:
//   public class Product {
//       private final int id;
//       private final String name;
//       private final int price;
//       public Product(int id, String name, int price) { ... }
//       public int id() { return id; }
//       public String name() { return name; }
//       public int price() { return price; }
//       @Override public String toString() { return ...; }
//       @Override public boolean equals(Object o) { return ...; }
//       @Override public int hashCode() { return ...; }
//   }
public record Product(int id, String name, int price) {

    // コンパクトコンストラクタ: Record 特有の書き方（Java 16 以降）
    // [Java 7 不可] コンパクトコンストラクタは Java 16 以降
    // フィールドへの代入は自動で行われるため、ここでは検証だけ書く
    public Product {
        // [Java 7 不可] String.isBlank() は Java 11 以降。Java 7 では isEmpty() + trim() で代替
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("商品名は空にできません");
        }
        if (price <= 0) {
            throw new IllegalArgumentException(
                    "価格は1円以上を指定してください（指定値: " + price + "）");
        }
    }

    @Override
    public String toString() {
        // %,d は桁区切りカンマ付きの整数フォーマット（例: 80,000）
        return String.format("Product{id=%d, name='%s', price=%,d円}", id, name, price);
    }
}
