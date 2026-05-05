/**
 * 【なぜこのコードを学ぶのか】
 * 配列は要素数が固定のため、後からデータを追加するには不向きです。
 * 現場では「何件来るかわからない」データを扱う場面が多く、
 * そのために ArrayList（可変長リスト）と HashMap（キーと値の対応表）が標準的に使われます。
 * この章では追加・取り出しを中心に体験することで、データ構造の選択眼を養います。
 */
package com.example.practical_java;

import java.util.ArrayList;
import java.util.HashMap;

public class CollectionDemo {
    public static void main(String[] args) {

        System.out.println("--- 1. Before: 配列の制約を確認する ---");

        // ========== Before: 配列は要素数を最初に決めなければいけない ==========
        // 武器を3本格納する配列。サイズは宣言時に固定されるため、後から変えられない。
        String[] weaponsArray = new String[3];
        weaponsArray[0] = "木の剣";
        weaponsArray[1] = "鉄の剣";
        weaponsArray[2] = "勇者の剣";
        // 問題: 4本目の武器を追加しようとするとエラーになってしまう
        // weaponsArray[3] = "炎の剣"; // → ArrayIndexOutOfBoundsException!
        System.out.println("配列の要素数（固定）: " + weaponsArray.length);

        System.out.println("\n--- 2. After: List (リスト) の実験 ---");

        // ========== After: ArrayList はサイズ可変で後から自由にデータを追加できる ==========
        // 配列と違って、後から自由にデータを追加できます。
        // <String> は「中身は文字（String）だよ」という指定（ジェネリクス。詳しくは第06章）。
        ArrayList<String> weapons = new ArrayList<>();

        weapons.add("木の剣");
        weapons.add("鉄の剣");
        weapons.add("勇者の剣");
        weapons.add("炎の剣"); // 配列と違って後から追加できる！

        // 中身を順番に表示
        // ※ この for ループ、もっと短く書けないか気になった人は第06章へ（ラムダ式で1行になります）
        // [Java 7 不可] forEach(w -> ...) はラムダ式（Java 8 以降）。Java 7 では拡張 for ループを使う（現在の書き方）。
        for (String w : weapons) {
            System.out.println("武器: " + w);
        }

        // 注意: 配列は .length（括弧なし）、ArrayList は .size()（括弧あり）
        System.out.println("個数: " + weapons.size());

        System.out.println("\n--- 3. Map (マップ) の実験 ---");
        // 「キー」と「値」をセットで保存します（辞書のようなもの）。
        // <String, Integer> は「キーは文字、値は数字」という指定です。
        HashMap<String, Integer> items = new HashMap<>();

        items.put("potion", 100);
        items.put("buffItem", 150);

        // 名前（キー）を使って、値段（値）を取り出す
        // 注意: 存在しないキーを指定すると null が返り、int に代入しようとした瞬間に
        //       NullPointerException が発生します（「確認してみよう」問2を試してみましょう）
        //       → この問題の解決策（Optional クラス）は第08章で学びます
        int price = items.get("potion");
        System.out.println("ポーションの値段: " + price + "ゴールド");
    }
}
