/**
 * 【なぜこのコードを学ぶのか】
 * 「終わる条件が実行してみるまでわからないループ」は while 文が最も自然に表現できます。
 * また、Scanner でユーザーの入力を受け取る体験は、
 * 「プログラムが人間と対話する」仕組みの基礎です。
 * ゲームという親しみやすい題材を通じて、while・条件分岐・入力を一度に体験します。
 */
package com.example.practical_java;

import java.util.Random;
import java.util.Scanner;

public class Game {
    public static void main(String[] args) {
        System.out.println("★ 数当てゲーム ★");
        System.out.println("0から99の間で、隠された数字を当ててください！");

        // 1. ランダムな答えを決める (0〜99)
        Random rand = new Random();
        int answer = rand.nextInt(100);

        // 2. ユーザーの入力を受け取る準備
        Scanner scanner = new Scanner(System.in);
        int count = 0;

        // 3. 正解するまで繰り返す（意図的な無限ループ + break パターン）
        // while (true) で「最初から条件を true にする」意図的な無限ループを作り、
        // break で出口を用意する。終了条件が実行前に決まらないときに現場でよく使われる定番パターン。
        // ★ break を消すと、正解しても永遠にゲームが続きます（第03章の無限ループとの違いに注目）。
        while (true) {
            count++;
            System.out.print("数字を入力してください > ");

            // 入力された数字を受け取る
            int guess = scanner.nextInt();

            // 判定ロジック
            if (guess == answer) {
                System.out.println("正解！！！ " + count + "回目で当たりました。");
                break; // ループを抜ける（終了）
            } else if (guess > answer) {
                System.out.println("もっと小さい数字です ↓");
            } else {
                System.out.println("もっと大きい数字です ↑");
            }
        }

        System.out.println("ゲーム終了");
        scanner.close(); // 使い終わったリソースは必ず close() で閉じる（現場の鉄則）
    }
}
