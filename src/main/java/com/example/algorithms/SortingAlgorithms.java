/**
 * 【なぜこのコードを学ぶのか】
 * 同じ「整列する」という処理でも、アルゴリズムの選択によって
 * 10万件のデータへの処理時間が数秒 vs 数ミリ秒と1000倍以上変わる。
 * バブルソート（O(n²)）とマージソート（O(n log n)）を自分で実装して速度差を実測し、
 * 「なぜ標準ライブラリの Arrays.sort() を使うべきか」を体験する。
 */
package com.example.algorithms;

import java.util.Arrays;
import java.util.Random;

public class SortingAlgorithms {

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // セクション1: バブルソート（O(n²)）の動作確認
        // ---------------------------------------------------------
        System.out.println("=== セクション1: バブルソート（O(n²)）===");
        System.out.println();

        int[] small1 = generateRandomArray(100);
        int[] forBubble = copyArray(small1);

        // 隣り合う2要素を比較して、大きい方を右に移動する
        // 最悪でも n*(n-1)/2 回比較する → O(n²)
        bubbleSort(forBubble);

        System.out.print("バブルソート後（先頭5件）: ");
        printFirst5(forBubble);

        System.out.println();

        // ---------------------------------------------------------
        // セクション2: 選択ソート（O(n²)）の動作確認
        // ---------------------------------------------------------
        System.out.println("=== セクション2: 選択ソート（O(n²)）===");
        System.out.println();

        int[] forSelection = copyArray(small1);

        // 未整列部分から最小値を探し、先頭と交換する
        // 比較回数は n*(n-1)/2 → O(n²)。バブルソートと同じ計算量だが交換回数は少ない
        selectionSort(forSelection);

        System.out.print("選択ソート後（先頭5件）: ");
        printFirst5(forSelection);

        System.out.println();

        // ---------------------------------------------------------
        // セクション3: マージソート（O(n log n)）の動作確認
        // ---------------------------------------------------------
        System.out.println("=== セクション3: マージソート（O(n log n)）===");
        System.out.println();

        int[] forMerge = copyArray(small1);

        // 分割統治（Divide and Conquer）: 問題を半分に分割して再帰的に解く
        // 分割: n → n/2 → n/4 → ... → 1（log n 段階）
        // マージ: 各段階で n 回の比較 → 合計 O(n log n)
        mergeSort(forMerge, 0, forMerge.length - 1);

        System.out.print("マージソート後（先頭5件）: ");
        printFirst5(forMerge);

        System.out.println();

        // ---------------------------------------------------------
        // セクション4: [アンチパターン] O(n²) を大量データに使う問題
        // 10万件のデータで3種のソートの速度差を実測する
        // ---------------------------------------------------------
        System.out.println("========== [アンチパターン] O(n²) を大量データに使う問題 ==========");
        System.out.println("// 10万件のデータで3種のソートの速度差を実測する");
        System.out.println();

        int size = 100_000;
        int[] base = generateRandomArray(size);

        // バブルソートの計測（O(n²): 10万件では数十秒かかる）
        System.out.println("--- バブルソート（O(n²)）計測中 ... ---");
        int[] forBubbleLarge = copyArray(base);
        long startBubble = System.nanoTime();
        bubbleSort(forBubbleLarge);
        long elapsedBubble = System.nanoTime() - startBubble;
        long bubbleMs = elapsedBubble / 1_000_000;
        System.out.println("バブルソート 完了: " + bubbleMs + " ms");

        // 選択ソートの計測（O(n²): バブルより交換回数は少ないが同じ計算量）
        System.out.println("--- 選択ソート（O(n²)）計測中 ... ---");
        int[] forSelectionLarge = copyArray(base);
        long startSelection = System.nanoTime();
        selectionSort(forSelectionLarge);
        long elapsedSelection = System.nanoTime() - startSelection;
        long selectionMs = elapsedSelection / 1_000_000;
        System.out.println("選択ソート 完了: " + selectionMs + " ms");

        // マージソートの計測（O(n log n): 10万件でも数十ミリ秒）
        System.out.println("--- マージソート（O(n log n)）計測中 ... ---");
        int[] forMergeLarge = copyArray(base);
        long startMerge = System.nanoTime();
        mergeSort(forMergeLarge, 0, forMergeLarge.length - 1);
        long elapsedMerge = System.nanoTime() - startMerge;
        long mergeMs = elapsedMerge / 1_000_000;
        System.out.println("マージソート 完了: " + mergeMs + " ms");

        System.out.println();
        System.out.println("=== 速度比較結果（10万件）===");
        System.out.println("バブルソート : " + bubbleMs    + " ms");
        System.out.println("選択ソート   : " + selectionMs + " ms");
        System.out.println("マージソート : " + mergeMs     + " ms");

        // マージソートを基準に倍率を計算する（0除算を避ける）
        if (mergeMs > 0) {
            System.out.println();
            System.out.println("マージソートを1とした場合の倍率:");
            System.out.println("  バブルソート : 約 " + (bubbleMs    / mergeMs) + " 倍");
            System.out.println("  選択ソート   : 約 " + (selectionMs / mergeMs) + " 倍");
        }

        System.out.println();
        // バブル/選択ソートは O(n²) → 10万件で数秒〜数十秒かかる
        // マージソートは O(n log n) → 10万件でも数十ミリ秒
        // 現場では Arrays.sort()（TimSort: O(n log n) の最適化実装）を使う
        System.out.println("// バブル/選択ソートは O(n²) → 10万件で数秒〜数十秒かかる");
        System.out.println("// マージソートは O(n log n) → 10万件でも数十ミリ秒");
        System.out.println("// 現場では Arrays.sort()（TimSort: O(n log n) の最適化実装）を使う");
        System.out.println("// → SortingAlgorithms.java の末尾の「まとめ」を参照");

        System.out.println();

        // ---------------------------------------------------------
        // セクション5: まとめ
        // ---------------------------------------------------------
        System.out.println("=== まとめ ===");
        System.out.println("バブルソート: O(n²) — 理解しやすいが実用には使わない");
        System.out.println("選択ソート  : O(n²) — バブルより交換回数は少ないが同じ計算量");
        System.out.println("マージソート: O(n log n) — 安定ソート。大量データに使える");
        System.out.println("Arrays.sort : TimSort（挿入ソート + マージソート）— 現場で使う唯一の選択肢");
        System.out.println("→ ソートは自分で実装せず、必ず標準ライブラリを使う");
    }

    // ---------------------------------------------------------
    // ========== バブルソート: 隣同士を比較して交換する（O(n²)）==========
    // ---------------------------------------------------------
    private static void bubbleSort(int[] arr) {
        int n = arr.length;
        // 外側のループ: パスの回数（最大 n-1 回）
        for (int i = 0; i < n - 1; i++) {
            // 内側のループ: 隣同士を比較して大きい方を右へ移動する
            // 1パスで最大値が末尾に「浮き上がる（バブル）」ため、毎回末尾を1つ縮める
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 隣同士を交換する
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        // 最悪でも n*(n-1)/2 回比較する → O(n²)
    }

    // ---------------------------------------------------------
    // ========== 選択ソート: 最小値を探して先頭に置く（O(n²)）==========
    // ---------------------------------------------------------
    private static void selectionSort(int[] arr) {
        int n = arr.length;
        // 未整列部分の先頭位置を i で表す
        for (int i = 0; i < n - 1; i++) {
            // 未整列部分（i 以降）の最小値のインデックスを探す
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            // 最小値を未整列部分の先頭（i番目）と交換する
            if (minIndex != i) {
                int temp = arr[i];
                arr[i] = arr[minIndex];
                arr[minIndex] = temp;
            }
        }
        // 比較回数は n*(n-1)/2 → O(n²)。バブルソートと同じ計算量だが交換回数は少ない
    }

    // ---------------------------------------------------------
    // ========== マージソート: 分割統治で O(n log n)を実現 ==========
    // ---------------------------------------------------------

    // 再帰的に配列を半分に分割してソートする
    private static void mergeSort(int[] arr, int left, int right) {
        if (left >= right) {
            // 要素が1個以下になったらそれ以上分割できない（基底ケース）
            return;
        }
        // オーバーフロー防止のため (left + right) / 2 ではなくこの形式で中間点を計算する
        int mid = left + (right - left) / 2;

        mergeSort(arr, left, mid);       // 左半分を再帰的にソート
        mergeSort(arr, mid + 1, right);  // 右半分を再帰的にソート
        merge(arr, left, mid, right);    // ソート済みの左右をマージする
    }

    // ソート済みの2つの部分配列（arr[left..mid] と arr[mid+1..right]）をマージする
    private static void merge(int[] arr, int left, int mid, int right) {
        // 一時領域に左右のデータをコピーする
        int leftSize  = mid - left + 1;
        int rightSize = right - mid;

        int[] leftArr  = new int[leftSize];
        int[] rightArr = new int[rightSize];

        // [Java 7 不可] System.arraycopy は Java 1.0 からあるので互換性に問題ない
        System.arraycopy(arr, left,      leftArr,  0, leftSize);
        System.arraycopy(arr, mid + 1,   rightArr, 0, rightSize);

        // 左右を比較しながら元の配列に書き戻す
        int i = 0; // 左部分配列のポインタ
        int j = 0; // 右部分配列のポインタ
        int k = left; // 元配列のポインタ

        while (i < leftSize && j < rightSize) {
            if (leftArr[i] <= rightArr[j]) {
                arr[k] = leftArr[i];
                i++;
            } else {
                arr[k] = rightArr[j];
                j++;
            }
            k++;
        }

        // 残りの要素をコピーする（どちらか一方は残るかもしれない）
        while (i < leftSize) {
            arr[k] = leftArr[i];
            i++;
            k++;
        }
        while (j < rightSize) {
            arr[k] = rightArr[j];
            j++;
            k++;
        }
    }

    // ---------------------------------------------------------
    // ユーティリティメソッド
    // ---------------------------------------------------------

    // new Random(42): シード値42を固定することで、毎回同じ乱数列が生成される（再現性あり）
    private static int[] generateRandomArray(int size) {
        Random random = new Random(42);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(1_000_000); // 0〜999999 の範囲で生成
        }
        return arr;
    }

    // 配列を複製する（各ソートは同じ初期配列のコピーに対して実行するため）
    private static int[] copyArray(int[] src) {
        // [Java 7 不可] Arrays.copyOf は Java 6 以降なので互換性に問題ない（Java 6+ で利用可）
        return Arrays.copyOf(src, src.length);
    }

    // 配列の先頭5件を表示するユーティリティ
    private static void printFirst5(int[] arr) {
        System.out.print("[");
        for (int i = 0; i < Math.min(5, arr.length); i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(arr[i]);
        }
        System.out.println(", ...]");
    }
}
