/**
 * 【なぜこのコードを学ぶのか】
 * Javaの型は「プリミティブ型」と「参照型」の2種類に分かれており、
 * この違いを知らないと「なぜ String に .length() が使えて int には使えないのか」
 * 「なぜ == で比較すると期待通りにならないのか」という混乱が生じます。
 * 2つの型の構造の違いを体験することで、Javaの型システムの根本が理解できます。
 */
package com.example.variables_and_types;

public class ReferenceDemo {
    public static void main(String[] args) {
        // -------------------------
        // 【プリミティブ型】
        // -------------------------
        // データそのものが入っています。小文字で始まります (int, double, boolean)。
        char character = 'a';

        // -------------------------
        // 【参照型】
        // -------------------------
        // データの「場所」が入っています。大文字で始まります (String など)。
        // 便利な機能（メソッド）をたくさん持っています。
        String message = "Hello Java";

        System.out.println(character);
        System.out.println(message);

        // 参照型だけの特徴：データに対して「命令」ができる！
        // 文字数を数える命令
        System.out.println("文字数: " + message.length());

        // 大文字に変換する命令
        System.out.println("大文字: " + message.toUpperCase());

        // プリミティブ型でこれをやろうとするとエラーになります
        // character.length(); // ← これはできません
    }
}
