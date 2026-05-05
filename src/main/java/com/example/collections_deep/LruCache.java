/**
 * 【なぜこのコードを学ぶのか】
 * LRU（Least Recently Used: 最近最も使われていないものを捨てる）キャッシュは、
 * DBやAPIへのアクセスを減らすためによく使われる実装パターンだ。
 * LinkedHashMap の accessOrder と removeEldestEntry を活用すると、
 * わずか数行でこのキャッシュが実現できる。
 * 標準ライブラリを深く知ることで「車輪の再発明」を避けられることを体験する。
 */
package com.example.collections_deep;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache {

    public static void main(String[] args) {

        System.out.println("=== LRU キャッシュの動作確認 ===");
        System.out.println();
        System.out.println("【LRU とは】");
        System.out.println("  Least Recently Used の略。");
        System.out.println("  キャッシュが満杯になったとき「最近最も使われていない」データを捨てるポリシー。");
        System.out.println("  DBやAPIへのアクセス結果を直近N件だけメモリに保持して応答を高速化する用途で使われる。");
        System.out.println();

        // ---------------------------------------------------------
        // 容量3のLRUキャッシュを作って動作を確認する
        // ---------------------------------------------------------

        // accessOrder=true にしているため、get() するたびに「最近使った」順が更新される
        SimpleLruCache<String, String> cache = new SimpleLruCache<>(3);

        // ---------------------------------------------------------
        // ステップ1: A, B, C を順番に入れる
        // ---------------------------------------------------------
        System.out.println("--- ステップ1: A, B, C を追加 ---");
        cache.put("A", "Apple");
        cache.put("B", "Banana");
        cache.put("C", "Cherry");
        // 内部の順序（古い順）: A → B → C
        // アクセス順が古い（最も捨てられやすい）のは A
        System.out.println("キャッシュの中身: " + cache);
        System.out.println();

        // ---------------------------------------------------------
        // ステップ2: A にアクセスして「最近使った」に更新する
        // ---------------------------------------------------------
        System.out.println("--- ステップ2: A にアクセス（最近使ったに更新） ---");
        String valA = cache.get("A"); // A に get するとアクセス順が更新される
        // accessOrder=true のため、内部の順序（古い順）: B → C → A
        // 最も捨てられやすいのは B になった
        System.out.println("A の値: " + valA);
        System.out.println("アクセス後のキャッシュ（古い順に並ぶ）: " + cache);
        System.out.println("→ A が末尾（最近使った）に移動し、B が先頭（最も古い）になった");
        System.out.println();

        // ---------------------------------------------------------
        // ステップ3: D を追加→容量超過で B（最も古い）が自動削除される
        // ---------------------------------------------------------
        System.out.println("--- ステップ3: D を追加（容量3を超えるので最も古い B が削除される）---");
        cache.put("D", "Durian");
        // removeEldestEntry が size() > capacity を返すため、先頭（最も古い）の B が自動で消える
        // 残る順序: C → A → D
        System.out.println("D 追加後のキャッシュ: " + cache);
        System.out.println("→ B（最も使われていなかった）が自動で削除され、C・A・D が残る");
        System.out.println();

        // ---------------------------------------------------------
        // ステップ4: さらに E を追加→今度は C（最も古い）が削除される
        // ---------------------------------------------------------
        System.out.println("--- ステップ4: E を追加（C が削除される）---");
        cache.put("E", "Elderberry");
        // 残る順序: A → D → E
        System.out.println("E 追加後のキャッシュ: " + cache);
        System.out.println("→ C（次に古かった）が削除された");
        System.out.println();

        // ---------------------------------------------------------
        // ステップ5: get でヒット・ミスを確認する
        // ---------------------------------------------------------
        System.out.println("--- ステップ5: キャッシュヒット・ミスの確認 ---");
        System.out.println("A を get: " + cache.get("A") + " （キャッシュヒット）");
        System.out.println("B を get: " + cache.get("B") + " （キャッシュミス—すでに削除済み）");
        System.out.println("D を get: " + cache.get("D") + " （キャッシュヒット）");
        System.out.println();

        // ---------------------------------------------------------
        // 現場での利用シーンを解説する
        // ---------------------------------------------------------
        System.out.println("=== 現場での使い方イメージ ===");
        System.out.println();

        // ユーザーIDをキー、DBから取得したユーザー名を値とするキャッシュ
        SimpleLruCache<Integer, String> userCache = new SimpleLruCache<>(3);

        System.out.println("ユーザー情報キャッシュ（容量: 3件）");
        System.out.println();

        // DBアクセスを模擬するメソッドを使ってキャッシュに格納する
        for (int userId : new int[]{101, 102, 103}) {
            // キャッシュに存在しない場合だけ「DBから取得」して登録する
            String cached = userCache.get(userId);
            // null が返ってきたときの if チェックが必要になる
            // この null の扱いを安全にする手段が第08章の Optional—第08章の Optional.java で学ぶ
            if (cached == null) {
                // ここが実際にはDB/APIアクセスになる（重い処理を代替）
                String userName = fetchFromDatabase(userId);
                userCache.put(userId, userName);
                System.out.println("  DB取得してキャッシュに登録: userId=" + userId + " → " + userName);
            } else {
                System.out.println("  キャッシュヒット: userId=" + userId + " → " + cached);
            }
        }

        System.out.println("  現在のキャッシュ: " + userCache);
        System.out.println();

        // 4件目を追加—101 が最も古いので削除される
        String cached104 = userCache.get(104);
        if (cached104 == null) {
            userCache.put(104, fetchFromDatabase(104));
            System.out.println("  DB取得してキャッシュに登録: userId=104");
        }
        System.out.println("  4件目追加後（userId=101 が削除される）: " + userCache);
        System.out.println();

        System.out.println("【まとめ】");
        System.out.println("  LinkedHashMap(capacity, 0.75f, true) の accessOrder=true が「LRU の核心」。");
        System.out.println("  removeEldestEntry をオーバーライドするだけで、容量管理が自動化される。");
        System.out.println("  本番では ConcurrentHashMap ベースや Caffeine などのライブラリが使われるが、");
        System.out.println("  この仕組みを理解しておくとキャッシュの動作を深く把握できる。");
    }

    // ---------------------------------------------------------
    // DBアクセスを模擬するメソッド（実際にはDB/APIを呼ぶ処理が入る）
    // ---------------------------------------------------------
    static String fetchFromDatabase(int userId) {
        // 実際のアプリではここでDB接続・SQL実行などの重い処理をする
        // [Java 7 不可] switch 式（-> 構文）は Java 14 以降。Java 7 では switch 文で書く:
        //   switch (userId) { case 101: return "田中 太郎"; ... }
        return switch (userId) {
            case 101 -> "田中 太郎";
            case 102 -> "鈴木 花子";
            case 103 -> "佐藤 次郎";
            case 104 -> "山田 三郎";
            default  -> "Unknown User";
        };
    }
}

// ---------------------------------------------------------
// SimpleLruCache: LinkedHashMap を継承した LRU キャッシュ実装
// （package-private: このパッケージ内だけで使う補助クラス）
// ---------------------------------------------------------

/**
 * LRU（最近最も使われていないものを捨てる）キャッシュ。
 *
 * LinkedHashMap のコンストラクタに accessOrder=true を渡すことで、
 * get() のたびに要素が末尾（最近使った側）に移動するようになる。
 * removeEldestEntry をオーバーライドして「上限を超えたら先頭（最も古い）を削除」を実装する。
 */
class SimpleLruCache<K, V> extends LinkedHashMap<K, V> {

    // キャッシュの最大保持件数
    private final int capacity;

    /**
     * @param capacity キャッシュに保持する最大件数
     */
    SimpleLruCache(int capacity) {
        // 第3引数 accessOrder=true: put/get のたびにアクセス順（古い→新しい）で並び直す
        // 第1引数 initialCapacity: capacity+1 にしてリハッシュを防ぐ
        // 第2引数 loadFactor: 0.75f が標準（75%埋まったら内部拡張）
        super(capacity + 1, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * 新しいエントリが追加されたとき、先頭のエントリ（最も古い）を削除するか判定する。
     * size() > capacity のとき true を返すと LinkedHashMap が先頭エントリを自動で削除する。
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // size() が capacity を超えたら最も古い（先頭の）エントリを削除する
        return size() > capacity;
    }
}
