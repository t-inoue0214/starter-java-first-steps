/**
 * 【なぜこのコードを学ぶのか】
 * null を返すメソッドは呼び出し元に「if (null チェック)」を強制する。
 * null チェックを1か所でも忘れると NullPointerException が本番で爆発する。
 * Optional を使うと「値がないこと」を型で表現でき、
 * null チェック漏れをコンパイル段階で気づかせることができる。
 * 第07章 LruCache.java で「null が返ってくるので if チェックが必要」と
 * コメントした問題への答えがここにある。
 */
package com.example.modern_api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OptionalBasics {

    // ---------------------------------------------------------
    // UserRepository: ユーザー情報を管理するリポジトリ（内部クラス）
    // ---------------------------------------------------------
    static class UserRepository {
        private final Map<Integer, String> store = new HashMap<>();

        UserRepository() {
            store.put(1, "田中 太郎");
            store.put(2, "鈴木 花子");
            store.put(3, "佐藤 次郎");
        }

        // Before: null を返す（存在しない ID では null が返る）
        public String findByIdBefore(int id) {
            return store.get(id); // 存在しない id では null が返る
        }

        // After: Optional を返す（値がないことを型で表現する）
        // [Java 7 不可] Optional は Java 8 以降。Java 7 では null チェックで代替する:
        //   public String findById(int id) {
        //       String value = store.get(id);
        //       if (value == null) { throw new NoSuchElementException(); }
        //       return value;
        //   }
        public Optional<String> findById(int id) {
            return Optional.ofNullable(store.get(id)); // null なら empty な Optional を返す
        }
    }

    public static void main(String[] args) {

        System.out.println("=== Optional: null を安全に扱う ===");
        System.out.println();

        UserRepository repo = new UserRepository();

        // ========== Before: null が返るメソッド ==========
        // 呼び出し元が null チェックを忘れると NullPointerException になる

        System.out.println("--- Before: null が返るメソッド ---");

        String user = repo.findByIdBefore(99); // 存在しない ID

        // null チェックを忘れてしまった場合:
        // System.out.println(user.length()); // ← NullPointerException! 本番で爆発する

        // null チェックを書けば防げるが、書き忘れが起きやすい
        if (user != null) {
            System.out.println("ユーザー名: " + user);
        } else {
            System.out.println("ユーザーが見つかりませんでした（null チェックで対処）");
        }

        // 存在する ID では正常に返ってくる
        String user1 = repo.findByIdBefore(1);
        if (user1 != null) {
            System.out.println("ユーザー名: " + user1);
        }
        System.out.println();

        // ========== After: Optional を返すメソッド ==========
        // Optional を使うと「値がないかもしれない」という意図が型で伝わる。
        // 呼び出し元は Optional のメソッドを呼ばない限り中の値を取り出せないため、
        // null チェック漏れが起きにくい。

        System.out.println("--- After: Optional を返すメソッド ---");
        System.out.println();

        // ---------------------------------------------------------
        // 1. orElse(): 値がなければデフォルト値を返す
        // [Java 7 不可] Optional は Java 8 以降
        // ---------------------------------------------------------
        // [Java 7 不可] Optional は Java 8 以降
        String name1 = repo.findById(99)
            .orElse("デフォルトユーザー"); // 値がなければこの値を使う
        System.out.println("orElse    (ID=99): " + name1);

        String name1b = repo.findById(1)
            .orElse("デフォルトユーザー"); // 値があればそちらが使われる
        System.out.println("orElse    (ID=1 ): " + name1b);
        System.out.println();

        // ---------------------------------------------------------
        // 2. orElseGet(): 値がなければサプライヤーで生成する
        //    orElse との違い: orElse はデフォルト値を常に評価する。
        //    orElseGet はラムダが呼ばれるのは値が空のときだけ（重い処理に有利）。
        // [Java 7 不可] Optional.orElseGet() とラムダ式は Java 8 以降
        // ---------------------------------------------------------
        String name2 = repo.findById(99)
            .orElseGet(() -> "DB接続して生成したデフォルト値"); // 空のときだけラムダが実行される
        System.out.println("orElseGet (ID=99): " + name2);
        System.out.println();

        // ---------------------------------------------------------
        // 3. orElseThrow(): 値がなければ例外を投げる
        //    「存在しないのはプログラムのバグ」という場合に使う。
        // [Java 7 不可] Optional.orElseThrow() とラムダ式は Java 8 以降
        // ---------------------------------------------------------
        System.out.println("orElseThrow (ID=1): ");
        try {
            String name3 = repo.findById(1)
                .orElseThrow(() -> new IllegalArgumentException("ID: 1 は存在しません"));
            System.out.println("  → " + name3);
        } catch (IllegalArgumentException e) {
            System.out.println("  → 例外: " + e.getMessage());
        }

        System.out.println("orElseThrow (ID=99): ");
        try {
            String name3b = repo.findById(99)
                .orElseThrow(() -> new IllegalArgumentException("ID: 99 は存在しません"));
            System.out.println("  → " + name3b);
        } catch (IllegalArgumentException e) {
            System.out.println("  → 例外: " + e.getMessage());
        }
        System.out.println();

        // ---------------------------------------------------------
        // 4. ifPresent(): 値がある場合のみ処理する
        //    if (user != null) { ... } を1行で表現できる。
        // [Java 7 不可] Optional.ifPresent() とラムダ式は Java 8 以降
        // ---------------------------------------------------------
        System.out.print("ifPresent (ID=2): ");
        repo.findById(2)
            .ifPresent(name -> System.out.println(name + " が見つかりました"));

        System.out.print("ifPresent (ID=99): ");
        repo.findById(99)
            .ifPresent(name -> System.out.println(name + " が見つかりました")); // 空のため何も出力されない
        System.out.println("（空のため何も表示されない）");
        System.out.println();

        // ---------------------------------------------------------
        // 5. map(): 値がある場合のみ変換する
        //    値が空なら変換はスキップされ、空の Optional がそのまま返る。
        // [Java 7 不可] Optional.map() とラムダ式は Java 8 以降
        // ---------------------------------------------------------
        // toUpperCase() は英字にのみ作用する。日本語では変化しないことに注意。
        String name5 = repo.findById(3)
            .map(name -> name.toUpperCase()) // 値があれば大文字に変換する（英字のみ変化）
            .orElse("（名前なし）");
        System.out.println("map (ID=3): " + name5 + "（日本語は toUpperCase で変化しない）");

        String name5b = repo.findById(99)
            .map(name -> name.toUpperCase()) // 空なのでスキップされる
            .orElse("（名前なし）");
        System.out.println("map (ID=99): " + name5b);
        System.out.println();

        // ---------------------------------------------------------
        // Optional.of と Optional.ofNullable の違い
        // [Java 7 不可] Optional は Java 8 以降。Java 7 では null チェックで代替する。
        // ---------------------------------------------------------
        System.out.println("=== Optional.of vs Optional.ofNullable ===");

        // Optional.ofNullable(null) → 空の Optional を返す（安全）
        Optional<String> safe = Optional.ofNullable(null);
        System.out.println("ofNullable(null).isPresent(): " + safe.isPresent()); // false

        // Optional.of(null) → NullPointerException が発生する（危険）
        System.out.println("Optional.of(null) を試みる...");
        try {
            Optional<String> unsafe = Optional.of(null); // ← NullPointerException!
            System.out.println("  → " + unsafe); // この行には到達しない
        } catch (NullPointerException e) {
            System.out.println("  → NullPointerException が発生した");
            System.out.println("     null が来る可能性があるなら Optional.ofNullable を使う");
        }
        System.out.println();

        System.out.println("【まとめ】");
        System.out.println("  null を返す   : 呼び出し元に null チェックを強制。漏れると本番で爆発する");
        System.out.println("  Optional を返す: 「値がないこと」を型で表現。漏れがコードに現れやすい");
        System.out.println("  ただし Optional は「毎回 null チェックを書かなくていい魔法」ではない。");
        System.out.println("  「値がないことが正常な状態である」場合に使うのが適切だ。");
    }
}
