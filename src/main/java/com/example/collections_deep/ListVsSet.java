/**
 * 【なぜこのコードを学ぶのか】
 * List は順序を保証するが重複を許す。Set は重複を排除するが、実装によって順序保証が異なる。
 * 何百万件の contains 呼び出しは List では遅く HashSet では速い。
 * 「どのコレクションを使うか」の選択が、アプリケーションの速度を大きく左右することを体験する。
 */
package com.example.collections_deep;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ListVsSet {

    public static void main(String[] args) {

        // =========================================================
        // ========== List の特性: 順序保証・重複許可 ==========
        // =========================================================

        System.out.println("=== List の特性 ===");
        System.out.println();

        // ---------------------------------------------------------
        // ArrayList: 配列ベースのリスト（ランダムアクセスが速い）
        // ---------------------------------------------------------
        System.out.println("--- ArrayList の特性 ---");

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("バナナ");
        arrayList.add("りんご");
        arrayList.add("みかん");
        arrayList.add("バナナ"); // ← 重複を許す—List の特性

        System.out.println("ArrayList の中身: " + arrayList);
        System.out.println("重複あり: " + arrayList); // バナナが2つある

        // インデックスで直接アクセスできる（配列ベースなので高速）
        System.out.println("インデックス1の要素: " + arrayList.get(1)); // りんご

        System.out.println();

        // ---------------------------------------------------------
        // Set: 重複なしのコレクション
        // ---------------------------------------------------------
        System.out.println("--- HashSet: 重複を自動で排除する ---");

        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("バナナ");
        hashSet.add("りんご");
        hashSet.add("みかん");
        hashSet.add("バナナ"); // ← 重複は無視される（追加されない）

        System.out.println("HashSet の中身: " + hashSet);
        System.out.println("→ 重複が自動で排除された（バナナは1つだけ）");
        System.out.println("→ ただし順序は不定—実行するたびに順番が変わりうる");

        System.out.println();

        // =========================================================
        // ========== 計測体験: List.contains() vs HashSet.contains() ==========
        // =========================================================

        System.out.println("=== 計測体験: contains() の速度差 ===");
        System.out.println();

        // 10万件のデータを準備する
        int dataSize = 100_000;
        ArrayList<Integer> list = new ArrayList<>();
        HashSet<Integer> set = new HashSet<>();

        for (int i = 0; i < dataSize; i++) {
            list.add(i);
            set.add(i);
        }

        // 存在しない値（データ範囲外）を何度も検索して速度差を計測する
        int searchTarget = dataSize + 1; // リストにもセットにも存在しない値
        int searchCount = 1_000; // 1000回検索して差を計測する

        // ========== Before: List.contains() ==========
        long listStart = System.nanoTime();
        for (int i = 0; i < searchCount; i++) {
            list.contains(searchTarget); // 先頭から全件スキャン: O(n)
        }
        long listElapsed = System.nanoTime() - listStart;

        // ========== After: HashSet.contains() ==========
        long setStart = System.nanoTime();
        for (int i = 0; i < searchCount; i++) {
            set.contains(searchTarget); // ハッシュ関数で直接位置を計算: O(1)
        }
        long setElapsed = System.nanoTime() - setStart;

        System.out.println("データ件数: " + dataSize + " 件 / 検索回数: " + searchCount + " 回");
        System.out.println();
        System.out.println("List.contains()    処理時間: " + listElapsed + " ns");
        System.out.println("HashSet.contains() 処理時間: " + setElapsed + " ns");

        // どちらが速かったか計算して表示する
        if (setElapsed > 0) {
            long ratio = listElapsed / setElapsed;
            System.out.println("→ HashSet は List の約 " + ratio + " 倍速い!");
        }

        System.out.println();
        System.out.println("【なぜ HashSet が速いのか】");
        System.out.println("  List.contains(): 先頭から1件ずつ比較 → O(n) = 要素数に比例して遅くなる");
        System.out.println("  HashSet.contains(): ハッシュ関数で格納位置を計算して直接アクセス → O(1) = 件数によらず一定速度");
        System.out.println("  ハッシュ関数とは「値から格納場所の番地を計算する数式」のこと");

        System.out.println();

        // =========================================================
        // ========== LinkedList: キューとしての使い所 ==========
        // =========================================================

        System.out.println("=== LinkedList: キュー（先入れ先出し）として使う ===");
        System.out.println();

        // LinkedList はキュー（Queue）インターフェースも実装している
        // offer() で末尾に追加、poll() で先頭から取り出す
        LinkedList<String> queue = new LinkedList<>();

        // タスクをキューに追加する（処理待ちリストのイメージ）
        queue.offer("タスク1: メール送信");
        queue.offer("タスク2: レポート生成");
        queue.offer("タスク3: データバックアップ");

        System.out.println("キューの中身: " + queue);
        System.out.println();

        // 先頭から順番に取り出して処理する（先入れ先出し: FIFO）
        System.out.println("処理開始（先に追加したものから処理される）:");
        while (!queue.isEmpty()) {
            String task = queue.poll(); // 先頭から取り出す（取り出した要素はキューから消える）
            System.out.println("  処理中: " + task);
        }

        System.out.println();

        // ---------------------------------------------------------
        // まとめ: どれを使うか
        // ---------------------------------------------------------
        System.out.println("=== まとめ: コレクションの使い分け ===");
        System.out.println();
        System.out.println("ArrayList   : ランダムアクセス（get(i)）が多い / 末尾への追加が多い");
        System.out.println("LinkedList  : 先頭への追加・削除が頻繁 / キュー（FIFO）として使いたい");
        System.out.println("HashSet     : 重複を排除したい / contains() を高速にしたい / 順序は不要");
        System.out.println();
        System.out.println("【判断の基準】");
        System.out.println("  1. 重複を排除したい     → Set 系を選ぶ");
        System.out.println("  2. contains() が多い   → HashSet（O(1)）を選ぶ");
        System.out.println("  3. 順序を保持したい     → List 系、または次章の LinkedHashMap");
        System.out.println("  4. キュー（FIFO）が必要 → LinkedList の offer()/poll() を使う");

        // ---------------------------------------------------------
        // おまけ: List から重複を取り除く慣用句
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("--- おまけ: List から重複を取り除く慣用句 ---");

        // 重複を含む List を Set に変換して重複を除去し、また List に戻す
        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
        //   List<String> withDuplicates = Arrays.asList("A", "B", "A", "C", "B", "D");
        List<String> withDuplicates = List.of("A", "B", "A", "C", "B", "D");
        ArrayList<String> deduplicated = new ArrayList<>(new HashSet<>(withDuplicates));

        System.out.println("元の List（重複あり）: " + withDuplicates);
        System.out.println("重複除去後（順序は不定）: " + deduplicated);
        System.out.println("→ Set を経由することで重複を一発で除去できる");
    }
}
