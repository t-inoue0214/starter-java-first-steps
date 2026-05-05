/**
 * 【なぜこのコードを学ぶのか】
 * keySet でループして get するコードは現場でよく見かけるが、
 * entrySet のループに比べてハッシュ計算が2倍発生する。
 * 大量データでは無視できない速度差になる—実測で差を体験する。
 */
package com.example.collections_deep;

import java.util.HashMap;
import java.util.Map;

public class MapIterationPerf {

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // 計測用データ: 10万件の HashMap を準備する
        // ---------------------------------------------------------
        int dataSize = 100_000;
        HashMap<String, String> map = new HashMap<>(dataSize * 2); // 初期容量を大きめに確保してリハッシュを避ける

        for (int i = 0; i < dataSize; i++) {
            map.put("key-" + i, "value-" + i);
        }

        System.out.println("計測対象: HashMap " + dataSize + " 件");
        System.out.println();

        // JVM のウォームアップのために1回ダミー実行する
        // （初回実行は JIT コンパイルの影響で遅くなるため、計測前に慣らす）
        for (String key : map.keySet()) { String v = map.get(key); }
        for (Map.Entry<String, String> entry : map.entrySet()) { String v = entry.getValue(); }
        map.forEach((k, v) -> {});

        // =========================================================
        // ========== Before: keySet でループして get する ==========
        // =========================================================

        System.out.println("=== Before: keySet() でループして get() する ===");
        System.out.println();

        // 現場でよく見るパターン—一見わかりやすいが無駄がある
        long keySetStart = System.nanoTime();

        long keySetSum = 0; // コンパイラ最適化を防ぐためにダミーの計算をする
        for (String key : map.keySet()) {
            // map.get(key) で内部的にもう一度ハッシュ計算が走る
            // keySet() の反復でも1回、get() でも1回 → ハッシュ計算が合計2回発生する
            String value = map.get(key);
            keySetSum += value.length(); // ダミー計算
        }

        long keySetElapsed = System.nanoTime() - keySetStart;
        System.out.println("処理時間: " + keySetElapsed + " ns (" + (keySetElapsed / 1_000_000) + " ms)");
        System.out.println("問題点: get(key) のたびにハッシュ計算が走る → ハッシュ計算が2倍発生");
        System.out.println("（sum=" + keySetSum + " ← JVM最適化防止用）");

        System.out.println();

        // =========================================================
        // ========== After: entrySet でキーと値を同時に取得 ==========
        // =========================================================

        System.out.println("=== After: entrySet() でキーと値を同時に取得 ===");
        System.out.println();

        long entrySetStart = System.nanoTime();

        long entrySetSum = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            // entry がキーと値を両方持っているので get() を呼ぶ必要がない
            // ハッシュ計算は反復中の1回だけで済む
            String key   = entry.getKey();
            String value = entry.getValue();
            entrySetSum += value.length(); // ダミー計算
        }

        long entrySetElapsed = System.nanoTime() - entrySetStart;
        System.out.println("処理時間: " + entrySetElapsed + " ns (" + (entrySetElapsed / 1_000_000) + " ms)");
        System.out.println("改善点: entry.getValue() はハッシュ計算なし → 合計1回で済む");
        System.out.println("（sum=" + entrySetSum + " ← JVM最適化防止用）");

        System.out.println();

        // =========================================================
        // ========== ラムダ版: map.forEach() ==========
        // =========================================================

        System.out.println("=== ラムダ版: map.forEach() ===");
        System.out.println();

        long forEachStart = System.nanoTime();

        long forEachSum = 0;
        // map.forEach は内部的に entrySet と同等—最もシンプルな記述
        // ラムダで (key, value) を直接受け取れるので get() は不要
        final long[] sumHolder = {0}; // ラムダ内から外の変数を変更するためのホルダー
        // [Java 7 不可] Map.forEach() のラムダ版は Java 8 以降。Java 7 では entrySet ループで書く:
        //   for (Map.Entry<String, String> entry : map.entrySet()) { forEachSum += entry.getValue().length(); }
        map.forEach((key, value) -> sumHolder[0] += value.length());
        forEachSum = sumHolder[0];

        long forEachElapsed = System.nanoTime() - forEachStart;
        System.out.println("処理時間: " + forEachElapsed + " ns (" + (forEachElapsed / 1_000_000) + " ms)");
        System.out.println("特徴: entrySet と同等の速度・最もシンプルな記述");
        System.out.println("（sum=" + forEachSum + " ← JVM最適化防止用）");

        System.out.println();

        // =========================================================
        // ========== 速度差のまとめ ==========
        // =========================================================

        System.out.println("=== 速度差のまとめ ===");
        System.out.println();
        System.out.printf("keySet()  : %,d ns%n", keySetElapsed);
        System.out.printf("entrySet(): %,d ns%n", entrySetElapsed);
        System.out.printf("forEach() : %,d ns%n", forEachElapsed);

        System.out.println();
        System.out.println("【なぜ差が生まれるのか】");
        System.out.println("  keySet() + get(key) : キーのハッシュ計算 × 2回（反復時 + get時）");
        System.out.println("  entrySet() / forEach: キーのハッシュ計算 × 1回（反復時のみ）");
        System.out.println();
        System.out.println("【現場での指針】");
        System.out.println("  キーだけ必要          → keySet() を使う（値が不要なので get() しない）");
        System.out.println("  値だけ必要            → values() を使う");
        System.out.println("  キーも値も必要         → entrySet() または forEach() を使う");
        System.out.println("  ラムダで簡潔に書きたい → forEach((k, v) -> ...) を使う");
    }
}
