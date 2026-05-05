/**
 * 【なぜこのコードを学ぶのか】
 * 第04章の Dog クラスに setter を書かなかった理由への答えがここにある。
 * setter を持つ mutable なオブジェクトは、外部から不正な値を設定できてしまう。
 * private final フィールドで「作った後は変わらない」イミュータブルな設計にすることで、
 * バグの混入リスクを根本から断ち切れる。
 * さらに Java 16 の record を使うと、同じイミュータブルなクラスを1行で定義できる。
 */
package com.example.modern_api;

public class ImmutableDesign {

    // ========== Before: setter を持つ mutable なクラス（JavaBeans スタイル）==========
    // 【第04章の伏線回収】第04章の Dog クラスに setAge() を書かなかった理由がここで分かる。
    // setter があると、作成後も外部から自由に状態を変えられてしまう。
    private static class UserBean {
        private String name;
        private int age;

        public UserBean(String name, int age) {
            this.name = name;
            this.age  = age;
        }

        public String getName() { return name; }
        public int    getAge()  { return age;  }

        public void setName(String name) { this.name = name; } // ← 問題: 後の改造で意図しない上書きをされる可能性がある
        public void setAge(int age)      { this.age  = age;  } // ← 問題: 不正な値も通ってしまう
    }

    // ========== Middle: 手動イミュータブル（private final + コンストラクタのみ）==========
    // setter を削除し、フィールドを final にする。
    // → オブジェクトは作った瞬間から状態が変わらない（イミュータブル）。
    // これが第04章の Dog クラスが目指していた設計と同じ形だ。
    private static class UserImmutable {
        private final String name; // final: 一度代入したら変更不可
        private final int age;     // final: 一度代入したら変更不可

        public UserImmutable(String name, int age) {
            // コンストラクタでバリデーションする（不正な値を作成時点で弾く）
            if (age < 0) {
                throw new IllegalArgumentException("年齢は0以上でなければなりません: " + age);
            }
            this.name = name;
            this.age  = age;
        }

        public String getName() { return name; }
        public int    getAge()  { return age;  }
        // setter は定義しない → 外部から変更不可能
    }

    // ========== After: Records（Java 16 以降）==========
    // [Java 7 不可] record キーワードは Java 16 以降。
    //   Java 7 での代替: private final フィールド + コンストラクタ + getter のみのクラス（Middle の形）
    //
    // record は以下を自動生成する:
    //   - private final フィールド
    //   - 全フィールドを引数に取るコンストラクタ
    //   - getter（フィールド名と同じ名前のメソッド: name() / age()）
    //   - toString() / equals() / hashCode()
    private record UserRecord(String name, int age) {
        // コンパクトコンストラクタ: フィールドへの代入は record が自動で行う
        UserRecord {
            if (age < 0) {
                throw new IllegalArgumentException("年齢は0以上でなければなりません: " + age);
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("=== イミュータブル設計: 第04章の伏線回収 ===");
        System.out.println();

        // ---------------------------------------------------------
        // Before: mutable（setter あり）の問題を体験する
        // ---------------------------------------------------------
        System.out.println("--- Before: mutable なクラス（setter あり）---");

        UserBean user = new UserBean("田中 太郎", 25);
        System.out.println("作成後: " + user.getName() + "（" + user.getAge() + "歳）");

        // setter 経由で不正な値を設定できてしまう
        user.setAge(-1); // 年齢がマイナスになっても setter は通してしまう
        user.setName("田中 次郎"); // 名前も自由に変更できてしまう
        System.out.println("setAge(-1) 後: " + user.getName() + "（" + user.getAge() + "歳）");
        System.out.println("→ 不正な状態になってしまった（バグの温床）");
        System.out.println();

        // ---------------------------------------------------------
        // Middle: 手動イミュータブル（private final + コンストラクタのみ）
        // ---------------------------------------------------------
        System.out.println("--- Middle: 手動イミュータブル（private final + setter なし）---");

        UserImmutable immutableUser = new UserImmutable("鈴木 花子", 30);
        System.out.println("作成後: " + immutableUser.getName() + "（" + immutableUser.getAge() + "歳）");

        // setter が存在しないのでコンパイルエラーになる
        // immutableUser.setAge(-1); // ← コンパイルエラー: setAge() は存在しない

        // コンストラクタで不正な値を弾く
        System.out.println("new UserImmutable(\"エラー\", -1) を試みる...");
        try {
            UserImmutable invalid = new UserImmutable("エラー", -1);
            System.out.println("  → 作成成功（この行には到達しない）: " + invalid.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("  → 不正な値の検出: " + e.getMessage());
        }
        System.out.println();

        // ---------------------------------------------------------
        // After: Records（Java 16+）で同じクラスを1行で定義する
        // ---------------------------------------------------------
        System.out.println("--- After: record（Java 16+）---");

        UserRecord record = new UserRecord("佐藤 次郎", 28);
        // getter は getXxx() ではなくフィールド名と同じ名前のメソッド（name() / age()）
        System.out.println("name()    : " + record.name());
        System.out.println("age()     : " + record.age());
        System.out.println("toString(): " + record);         // 自動生成された toString()

        // equals() も自動生成される（同じ値なら true）
        UserRecord record2 = new UserRecord("佐藤 次郎", 28);
        System.out.println("equals（同値）: " + record.equals(record2)); // true

        UserRecord record3 = new UserRecord("伊藤 美咲", 28);
        System.out.println("equals（別値）: " + record.equals(record3)); // false

        // record のコンパクトコンストラクタでも不正な値を弾く
        System.out.println("new UserRecord(\"エラー\", -1) を試みる...");
        try {
            UserRecord invalid = new UserRecord("エラー", -1);
            System.out.println("  → 作成成功（この行には到達しない）: " + invalid.name());
        } catch (IllegalArgumentException e) {
            System.out.println("  → 不正な値の検出: " + e.getMessage());
        }
        System.out.println();

        // ---------------------------------------------------------
        // まとめと第12章への橋渡し
        // ---------------------------------------------------------
        System.out.println("=== まとめ ===");
        System.out.println("  mutable（setter あり）: 状態が変わりうる。不正な値の混入リスクあり");
        System.out.println("  immutable（final + setter なし）: 作った後は変わらない。スレッドセーフ");
        System.out.println("  record: immutable クラスを1行で定義できる（Java 16+）");
        System.out.println();
        System.out.println("【第12章への橋渡し】");
        System.out.println("  setter を削除することで、複数のスレッドが同じオブジェクトを");
        System.out.println("  同時に読んでも状態が壊れない（スレッドセーフ）。");
        System.out.println("  → 詳しくは第12章「並行処理・非同期処理の基礎」で体験する");
    }
}
