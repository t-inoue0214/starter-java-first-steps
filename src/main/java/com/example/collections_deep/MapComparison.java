/**
 * 【なぜこのコードを学ぶのか】
 * HashMap・TreeMap・LinkedHashMap は同じ Map インターフェースを実装しているが、
 * 内部構造が違うため「順序」と「速度」が異なる。
 * ログや設定値の表示順が毎回変わるバグは HashMap の誤用から起きる。
 * 3種の違いを同一ファイルで対比して、「いつ何を使うか」を体験する。
 */
package com.example.collections_deep;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class MapComparison {

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // 同じデータを3種の Map に入れて順序の違いを可視化する
        // ---------------------------------------------------------

        // テストデータ: HTTPステータスコードとその意味
        // 挿入順: 404 → 200 → 500 → 301 → 403
        String[][] entries = new String[][]{
            {"404", "Not Found"},
            {"200", "OK"},
            {"500", "Internal Server Error"},
            {"301", "Moved Permanently"},
            {"403", "Forbidden"}
        };

        // =========================================================
        // ========== HashMap: 順序不定・最も高速 ==========
        // =========================================================

        System.out.println("=== HashMap: 順序は保証されない ===");
        System.out.println();

        // HashMap はキーのハッシュ値によって格納位置が決まる
        // そのため挿入順とは無関係な順番で表示されることがある
        HashMap<String, String> hashMap = new HashMap<>();
        for (String[] entry : entries) {
            hashMap.put(entry[0], entry[1]);
        }

        System.out.println("挿入した順番: 404 → 200 → 500 → 301 → 403");
        System.out.print("HashMap の表示順: ");
        // [Java 7 不可] Map.forEach() のラムダ版は Java 8 以降。Java 7 では entrySet ループで書く:
        //   for (Map.Entry<String, String> e : hashMap.entrySet()) { System.out.print(e.getKey() + " "); }
        hashMap.forEach((key, value) -> System.out.print(key + " "));
        System.out.println();
        System.out.println("→ 毎回同じとは限らない（JVMの実装・データ内容によって変わりうる）");
        System.out.println("→ 検索・追加・削除が O(1) で最速—順序が不要な場面ではこれを選ぶ");

        System.out.println();

        // =========================================================
        // ========== TreeMap: キーで自動ソート ==========
        // =========================================================

        System.out.println("=== TreeMap: キーで自動ソート（内部は赤黒木） ===");
        System.out.println();

        // TreeMap はキーを自然順（文字列なら辞書順、数値なら昇順）で並べる
        // 内部構造は「赤黒木」という自己平衡二分探索木—検索・追加は O(log n)
        TreeMap<String, String> treeMap = new TreeMap<>();
        for (String[] entry : entries) {
            treeMap.put(entry[0], entry[1]);
        }

        System.out.println("挿入した順番: 404 → 200 → 500 → 301 → 403");
        System.out.print("TreeMap の表示順: ");
        treeMap.forEach((key, value) -> System.out.print(key + " "));
        System.out.println();
        System.out.println("→ キー（ステータスコード）の昇順に自動でソートされる");
        System.out.println("→ 検索・追加は O(log n)—HashMap より少し遅いがソートが自動");

        // TreeMap 特有の便利メソッド
        System.out.println("最小キー（firstKey）: " + treeMap.firstKey());
        System.out.println("最大キー（lastKey）:  " + treeMap.lastKey());
        // 300以上の最初のキーを取得する（ステータスコードの範囲検索のイメージ）
        System.out.println("300以上の最初のキー（ceilingKey）: " + treeMap.ceilingKey("300"));

        System.out.println();

        // =========================================================
        // ========== LinkedHashMap: 挿入順を保持 ==========
        // =========================================================

        System.out.println("=== LinkedHashMap: 挿入順を保持（内部はリンクリスト+ハッシュ） ===");
        System.out.println();

        // LinkedHashMap は HashMap の速度を持ちつつ、挿入順を覚えている
        // 内部にリンクリストを持つことで、put した順番を保証する
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        for (String[] entry : entries) {
            linkedHashMap.put(entry[0], entry[1]);
        }

        System.out.println("挿入した順番: 404 → 200 → 500 → 301 → 403");
        System.out.print("LinkedHashMap の表示順: ");
        linkedHashMap.forEach((key, value) -> System.out.print(key + " "));
        System.out.println();
        System.out.println("→ 挿入した順番どおりに表示される");
        System.out.println("→ 速度は HashMap とほぼ同じ—メモリ使用量が少し多い");

        System.out.println();

        // =========================================================
        // ========== 3種の対比まとめ ==========
        // =========================================================

        System.out.println("=== 3種の対比まとめ ===");
        System.out.println();

        // 全エントリを見やすく表示する
        System.out.println("種類            | 順序                   | 速度（追加・検索）| 使いどころ");
        System.out.println("----------------|------------------------|------------------|-----------------------------------");
        System.out.println("HashMap         | 不定（ハッシュ値で決まる）| O(1) 最速        | 順序不要・とにかく速く検索したいとき");
        System.out.println("TreeMap         | キーで自動ソート         | O(log n)         | ソート済みで取り出したい・範囲検索をしたいとき");
        System.out.println("LinkedHashMap   | 挿入順を保持             | O(1) ほぼ最速    | 設定値・ログ出力など順序が重要なとき");

        System.out.println();

        // ---------------------------------------------------------
        // 実際の現場でよくある誤用例
        // ---------------------------------------------------------
        System.out.println("--- 現場でよくある誤用 ---");
        System.out.println();

        // 設定ファイルを読み込んで順番どおりに処理したいのに HashMap を使うケース
        System.out.println("例: 設定値を HashMap に入れると順序が変わるバグ");

        HashMap<String, String> configWrong = new HashMap<>();
        configWrong.put("step1", "データベース接続");
        configWrong.put("step2", "キャッシュ初期化");
        configWrong.put("step3", "サービス起動");

        System.out.print("HashMap で処理順（バグ）: ");
        configWrong.forEach((k, v) -> System.out.print(k + " "));
        System.out.println(" ← step1→step2→step3 の順にならないことがある!");

        // LinkedHashMap に変えるだけで解決する
        LinkedHashMap<String, String> configCorrect = new LinkedHashMap<>();
        configCorrect.put("step1", "データベース接続");
        configCorrect.put("step2", "キャッシュ初期化");
        configCorrect.put("step3", "サービス起動");

        System.out.print("LinkedHashMap で処理順（正解）: ");
        configCorrect.forEach((k, v) -> System.out.print(k + " "));
        System.out.println(" ← 必ず挿入順どおりになる");

        System.out.println();
        System.out.println("【結論】");
        System.out.println("  順序が重要な場面（設定・ログ・表示）→ LinkedHashMap か TreeMap を選ぶ");
        System.out.println("  順序が不要でとにかく速く検索したい  → HashMap を選ぶ");
        System.out.println("  ソート済みで取り出したい・範囲検索  → TreeMap を選ぶ");
    }
}
