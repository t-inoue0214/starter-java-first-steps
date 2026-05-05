/**
 * 【なぜこのコードを学ぶのか】
 * ソート済みの配列から値を探すとき、先頭から順番に調べる線形探索は O(n) だが、
 * 二分探索なら O(log n)—100万件のデータでも最大20回の比較で見つかる。
 * 「ソートしてあること」が前提となる二分探索を自分で実装し、
 * 線形探索と速度差を実測することでソートの価値を再認識する。
 */
package com.example.algorithms;

import java.util.Arrays;

public class BinarySearch {

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // セクション1: 線形探索（O(n)）
        // ========== Before: 線形探索（O(n)）==========
        // 先頭から1件ずつ調べる。ソートされていなくても使えるが、件数に比例して遅くなる。
        // ---------------------------------------------------------
        System.out.println("=== セクション1: Before — 線形探索（O(n)）===");
        System.out.println();

        int[] small = {10, 3, 7, 1, 9, 5, 8, 2, 6, 4};

        int target1 = 7;
        int result1 = linearSearch(small, target1);
        System.out.println("配列: " + Arrays.toString(small));
        System.out.println("線形探索 " + target1 + " → インデックス: " + result1);

        int target2 = 5;
        int result2 = linearSearch(small, target2);
        System.out.println("線形探索 " + target2 + " → インデックス: " + result2);

        System.out.println();

        // ---------------------------------------------------------
        // セクション2: 二分探索（O(log n)）
        // ========== After: 二分探索（O(log n)）==========
        // 「配列がソート済み」であることが前提。
        // 中央の値と比較して探索範囲を半分に絞る。
        // 100万件でも最大 log₂(1,000,000) ≈ 20 回の比較で見つかる。
        // ---------------------------------------------------------
        System.out.println("=== セクション2: After — 二分探索（O(log n)）===");
        System.out.println();

        // 二分探索はソート済み配列が必須なのでソートしてから使う
        int[] sorted = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        System.out.println("配列（ソート済み）: " + Arrays.toString(sorted));
        System.out.println();

        // デバッグ出力あり: 探索範囲がどう絞られるかを観察する
        int target3 = 7;
        System.out.println("二分探索 " + target3 + " を探す（探索範囲の変化を観察）:");
        int result3 = binarySearchWithDebug(sorted, target3);
        System.out.println("→ インデックス: " + result3);
        System.out.println();

        int target4 = 3;
        System.out.println("二分探索 " + target4 + " を探す（探索範囲の変化を観察）:");
        int result4 = binarySearchWithDebug(sorted, target4);
        System.out.println("→ インデックス: " + result4);
        System.out.println();

        // ---------------------------------------------------------
        // セクション3: 存在しない値の探索
        // ---------------------------------------------------------
        System.out.println("=== セクション3: 存在しない値の探索 ===");
        System.out.println();

        int notFound1 = -1;
        int result5 = binarySearch(sorted, notFound1);
        System.out.println("二分探索 " + notFound1 + " → インデックス: " + result5 + " （-1 = 見つからない）");

        int notFound2 = 9999;
        int result6 = binarySearch(sorted, notFound2);
        System.out.println("二分探索 " + notFound2 + " → インデックス: " + result6 + " （-1 = 見つからない）");

        System.out.println();

        // ---------------------------------------------------------
        // セクション4: 速度比較（10万件）
        // ========== 速度比較: 10万件の配列での線形探索 vs 二分探索 ==========
        // ---------------------------------------------------------
        System.out.println("=== セクション4: 速度比較（10万件）===");
        System.out.println();

        // 0〜99999 の連番ソート済み配列を作成する
        int largeSize = 100_000;
        int[] largeArray = new int[largeSize];
        for (int i = 0; i < largeSize; i++) {
            largeArray[i] = i;
        }

        // 「最後の要素」（99999）を 10000回検索して速度差を計測する
        int searchTarget = 99_999; // 配列の末尾にある要素（線形探索の最悪ケース）
        int repeatCount  = 10_000;

        // 線形探索の計測
        long startLinear = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            linearSearch(largeArray, searchTarget);
        }
        long elapsedLinear = System.nanoTime() - startLinear;

        // 二分探索の計測
        long startBinary = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            binarySearch(largeArray, searchTarget);
        }
        long elapsedBinary = System.nanoTime() - startBinary;

        System.out.println("線形探索（" + repeatCount + "回）: " + (elapsedLinear / 1_000_000) + " ms");
        System.out.println("二分探索（" + repeatCount + "回）: " + (elapsedBinary / 1_000_000) + " ms");

        // 二分探索の速度を基準に「線形探索は約XX倍遅い」を表示する
        if (elapsedBinary > 0) {
            long ratio = elapsedLinear / elapsedBinary;
            System.out.println("→ 線形探索は二分探索より約 " + ratio + " 倍遅い");
        }

        System.out.println();

        // ---------------------------------------------------------
        // セクション5: 前提条件の確認（ソートされていない配列では使えない）
        // ========== 注意: 二分探索はソート済み配列が前提 ==========
        // ソートされていない配列に二分探索を使うと誤った結果になる
        // ---------------------------------------------------------
        System.out.println("=== セクション5: 注意 — 未ソート配列に二分探索は使えない ===");
        System.out.println();

        int[] unsorted = {5, 3, 8, 1, 9, 2};
        int wrongTarget = 8;
        int wrongResult = binarySearch(unsorted, wrongTarget);

        // 「見つからない」または「誤った位置」が返る
        System.out.println("未ソート配列: " + Arrays.toString(unsorted));
        System.out.println("未ソートに二分探索 " + wrongTarget + ": インデックス=" + wrongResult + "（誤り）");
        System.out.println("正しい線形探索の結果: インデックス=" + linearSearch(unsorted, wrongTarget));
        System.out.println();
        System.out.println("→ Arrays.sort() でソートしてから Arrays.binarySearch() を使う");
        System.out.println("→ 次のファイル StandardLibrarySort.java で Arrays.binarySearch() を使う方法を学ぶ");
    }

    // ---------------------------------------------------------
    // 線形探索（O(n)）: 先頭から1件ずつ比較する
    // 見つかったインデックスを返す。見つからなければ -1 を返す。
    // ---------------------------------------------------------
    private static int linearSearch(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i; // 見つかった位置のインデックスを返す
            }
        }
        return -1; // 最後まで見つからなかった
    }

    // ---------------------------------------------------------
    // 二分探索（O(log n)）: 中央の値と比較して探索範囲を半分ずつ絞る
    // 見つかったインデックスを返す。見つからなければ -1 を返す。
    // 前提: arr はソート済みであること
    // ---------------------------------------------------------
    private static int binarySearch(int[] arr, int target) {
        int left  = 0;
        int right = arr.length - 1;

        while (left <= right) {
            // オーバーフロー防止のため (left + right) / 2 ではなくこの書き方を使う
            // （left と right が大きい整数のとき、加算がオーバーフローする可能性がある）
            int mid = left + (right - left) / 2;

            if (arr[mid] == target) {
                return mid; // 中央がちょうど目的の値
            } else if (arr[mid] < target) {
                left = mid + 1;  // 目的の値は右半分にある
            } else {
                right = mid - 1; // 目的の値は左半分にある
            }
        }
        return -1; // 見つからなかった
    }

    // ---------------------------------------------------------
    // デバッグ出力付き二分探索: 探索範囲の変化を表示する
    // 動作理解のためのメソッド（本番コードでは使わない）
    // ---------------------------------------------------------
    private static int binarySearchWithDebug(int[] arr, int target) {
        int left  = 0;
        int right = arr.length - 1;
        int step  = 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            // 探索範囲を表示する（学習用デバッグ出力）
            System.out.println("  ステップ" + step + ": left=" + left
                + " right=" + right + " mid=" + mid + " arr[mid]=" + arr[mid]);
            step++;

            if (arr[mid] == target) {
                return mid;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }
}
