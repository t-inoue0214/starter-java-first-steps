/**
 * 【なぜこのコードを学ぶのか】
 * 配列は宣言時にサイズを決めなければならない。
 * 要素数が動的に変わる現場（ユーザー入力・APIレスポンス等）では配列は使えず、
 * List が必要になる。配列の「限界」を体験することで、List の価値を実感する。
 */
package com.example.collections_deep;

import java.util.ArrayList;

public class ArrayLimitation {

    public static void main(String[] args) {

        // =========================================================
        // ========== Before: 配列の限界を体験する ==========
        // =========================================================

        System.out.println("=== Before: 配列の限界 ===");
        System.out.println();

        // ---------------------------------------------------------
        // 限界1: サイズが固定—宣言時に決めた数を超えられない
        // ---------------------------------------------------------
        System.out.println("--- 限界1: サイズが固定 ---");

        // 5個のスコアを格納できる配列を宣言する
        int[] scores = new int[5];
        scores[0] = 90;
        scores[1] = 85;
        scores[2] = 78;
        scores[3] = 92;
        scores[4] = 88;

        System.out.println("5個のスコアを格納: OK");

        // 6個目を入れようとすると実行時エラーになる
        // 現場では「ユーザーが予想より多く入力した」などで起こりうる
        try {
            scores[5] = 95; // ← 配列の範囲外! ArrayIndexOutOfBoundsException が発生する
            System.out.println("6個目のスコア: " + scores[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("エラー発生! " + e.getClass().getSimpleName());
            System.out.println("→ 配列はサイズを超えた要素を追加できない");
        }

        System.out.println();

        // ---------------------------------------------------------
        // 限界2: 途中への挿入が苦痛—手動でずらす必要がある
        // ---------------------------------------------------------
        System.out.println("--- 限界2: 途中への挿入が苦痛 ---");

        // インデックス2番目に新しいスコア(80)を挿入したい場合…
        // まず配列をコピーして後ろの要素を1つずつ手動でずらす必要がある
        int[] original = {10, 20, 30, 40, 50};
        int[] expanded = new int[original.length + 1]; // 1つ大きい配列を新規作成

        int insertAt = 2;    // インデックス2番目に挿入する
        int insertVal = 99;  // 挿入する値

        // 挿入位置より前はそのままコピーする
        for (int i = 0; i < insertAt; i++) {
            expanded[i] = original[i];
        }
        // 挿入する値を入れる
        expanded[insertAt] = insertVal;
        // 挿入位置より後ろは1つ後ろへずらしてコピーする
        for (int i = insertAt; i < original.length; i++) {
            expanded[i + 1] = original[i];
        }

        System.out.print("途中挿入の結果: ");
        for (int val : expanded) {
            System.out.print(val + " ");
        }
        System.out.println();
        System.out.println("→ これを毎回書くのは大変…しかも間違えやすい");

        System.out.println();

        // =========================================================
        // ========== After: ArrayList なら動的に操作できる ==========
        // =========================================================

        System.out.println("=== After: ArrayList の柔軟な操作 ===");
        System.out.println();

        // ---------------------------------------------------------
        // 解決1: add() で要素を好きなだけ追加できる
        // ---------------------------------------------------------
        System.out.println("--- 解決1: 動的な追加 ---");

        // 初期サイズを決めなくていい—必要に応じて内部が自動で拡張される
        ArrayList<Integer> scoreList = new ArrayList<>();
        scoreList.add(90);
        scoreList.add(85);
        scoreList.add(78);
        scoreList.add(92);
        scoreList.add(88);
        scoreList.add(95); // ← 6個目も問題なく追加できる!
        scoreList.add(100); // さらに7個目も

        System.out.println("スコア一覧: " + scoreList);
        System.out.println("要素数: " + scoreList.size());

        System.out.println();

        // ---------------------------------------------------------
        // 解決2: add(index, value) で途中挿入が1行で書ける
        // ---------------------------------------------------------
        System.out.println("--- 解決2: 途中への挿入が簡単 ---");

        ArrayList<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
        numbers.add(40);
        numbers.add(50);

        // インデックス2番目に 99 を挿入する—後ろのずらし処理は ArrayList が内部でやってくれる
        numbers.add(2, 99);
        System.out.println("途中挿入後: " + numbers);

        System.out.println();

        // ---------------------------------------------------------
        // 解決3: remove() で要素の削除も簡単
        // ---------------------------------------------------------
        System.out.println("--- 解決3: 要素の削除が簡単 ---");

        // 値を指定して削除（Integer にキャストして値指定であることを明示する）
        numbers.remove(Integer.valueOf(99));
        System.out.println("99を削除後: " + numbers);

        // インデックス0番目を削除する
        numbers.remove(0);
        System.out.println("先頭を削除後: " + numbers);

        System.out.println();

        // ---------------------------------------------------------
        // 注意: contains() の O(n) 問題
        // ---------------------------------------------------------
        System.out.println("--- 注意: contains() は全要素を順番に調べる ---");

        ArrayList<Integer> bigList = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            bigList.add(i);
        }

        // contains() は先頭から1件ずつ比較するため、大量データでは遅くなる（O(n) 計算量）
        long start = System.nanoTime();
        boolean found = bigList.contains(99_999); // 最後の要素を探す—最も時間がかかる
        long elapsed = System.nanoTime() - start;

        System.out.println("10万件のリストで contains(99999): " + found);
        System.out.println("処理時間: " + elapsed + " ns (" + (elapsed / 1_000_000) + " ms)");
        System.out.println("→ 大量データへの contains は HashSet を使う—次のファイル(ListVsSet.java)で詳しく学ぶ");
    }
}
