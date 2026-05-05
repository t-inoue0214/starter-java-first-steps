/**
 * 【なぜこのコードを学ぶのか】
 * Javaの型は「プリミティブ型」と「参照型」の2種類に分かれています。
 * この違いを知らないと、現場で以下のような混乱が生じます。
 *
 * ① なぜ String に .length() が使えて int には使えないのか
 *    → プリミティブ型はメソッドを持たない。参照型はオブジェクトなのでメソッドを持つ。
 *
 * ② なぜ == で String を比較すると期待通りにならないことがあるのか
 *    → == は「メモリ上の場所（アドレス）」を比較するため、同じ文字でも
 *       作り方によっては false になる。これは現場でも頻出のバグです。
 *
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
        // character.length(); // ← これはできません（int や char はメソッドを持っていない）

        // -------------------------
        // 【String の比較: == vs .equals()】
        // -------------------------
        // ========== Before: == で比較してしまうアンチパターン ==========
        // == は「データの場所（メモリアドレス）」を比較します。
        // 「中身の文字が同じかどうか」を比較したいなら .equals() を使う必要があります。
        String greeting1 = "Hello";
        String greeting2 = "Hello";
        // 実行すると true が表示されますが、これは「文字列定数プール」という Java の最適化によって
        // 偶然同じ場所を指しているためです。この true は信頼できません。
        System.out.println("== の比較: " + (greeting1 == greeting2));           // true になることもあるが信頼できない

        // 【実験】コメントを外して実行してみよう。== の結果が変わる！
        // String greeting3 = new String("Hello");
        // System.out.println("new String の == 比較: " + (greeting1 == greeting3)); // → false

        // ========== After: 参照型の比較には .equals() を使う ==========
        // .equals() は「中身のデータ」を比較する。String の比較は必ずこちらを使う。
        System.out.println(".equals() の比較: " + greeting1.equals(greeting2)); // 常に true
    }
}
