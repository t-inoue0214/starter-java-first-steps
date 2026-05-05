/**
 * 【なぜこのコードを学ぶのか】
 * Entity（エンティティ）は Clean Architecture の最内層だ。
 * ビジネスの核心となるオブジェクトを表し、フレームワーク・DB・UI のどれにも依存しない。
 * 第14章の Onion Architecture と同じ Product を使うことで、
 * 「アーキテクチャが変わっても Entity は変わらない」という原則を体感できる。
 *
 * Entity はビジネスルール（バリデーション）を自身で持つ。
 * コンパクトコンストラクタで「不正な商品は絶対に作れない」という制約を実装する。
 */
package com.example.clean_architecture.clean.entity;

// [Java 7 不可] Record は Java 16 以降。Java 7〜15 では通常クラスで書く:
//   public class Product {
//       private final int id;
//       private final String name;
//       private final int price;
//       public Product(int id, String name, int price) { ... バリデーション ... }
//       public int id() { return id; }
//       // equals・hashCode・toString も手動実装が必要
//   }
public record Product(int id, String name, int price) {

    // コンパクトコンストラクタ（Java 16 以降）
    // [Java 7 不可] コンパクトコンストラクタは Java 16 以降
    // フィールドへの代入は自動で行われ、ここではバリデーションだけを記述する
    public Product {
        // [Java 7 不可] String.isBlank() は Java 11 以降
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
        return String.format("Product{id=%d, name='%s', price=%,d円}", id, name, price);
    }
}
