/**
 * 【なぜこのコードを学ぶのか】
 * 「とりあえず動けばいい」の精神で書き続けると、データ管理・ビジネスロジック・
 * 表示処理がすべて1つのクラスに混在する「大きな泥団子（Big Ball of Mud）」になる。
 * このファイルは意図的にその悪い状態を再現している。
 * 何が問題か——変更しにくい・テストが書けない・DB を変えると全体を直す必要がある——
 * を体験することで、レイヤー分割の必要性が腑に落ちる。
 *
 * 第10〜13章ではそれぞれのテーマ（I/O・JDBC・並行・HTTP）を個別に学んだ。
 * 実際の業務コードではそれらが混在しやすく、このクラスのような状態に陥りがちだ。
 */
package com.example.architecture;

import java.util.ArrayList;
import java.util.List;

public class BigMainAntiPattern {

    // ★ 問題1: データ（状態）が String[] のまま。型安全性がない。
    //   p[0] が id か name かは読まないとわからない。
    //   DB に移行したいとき、このフィールド定義から直す必要がある。
    // [Java 7 不可] List.of() は Java 9 以降。Java 7 では new ArrayList<>() + add() を使う:
    //   List<String[]> products = new ArrayList<>();
    //   products.add(new String[]{"1", "ノートPC", "80000"});
    private static List<String[]> products = new ArrayList<>(List.of(
            new String[]{"1", "ノートPC", "80000"},
            new String[]{"2", "マウス", "3000"},
            new String[]{"3", "モニター", "50000"}
    ));

    // このメソッドを単体で実行して動作確認できる
    public static void main(String[] args) {
        run();
    }

    // Main.java から呼び出せるように static run() を公開している
    public static void run() {

        System.out.println("--- BigMainAntiPattern（Before）---");
        System.out.println();

        // ========== 商品一覧表示 ==========
        // ★ 問題2: 表示ロジック（書式整形）がここに直書きされている。
        //   Web API に変えたいとき、この for ループを探して書き直す必要がある。
        System.out.println("【商品一覧】");
        for (String[] p : products) {
            System.out.println("  ID=" + p[0] + " 商品名=" + p[1] + " 価格=" + p[2] + "円");
        }
        System.out.println();

        // ========== 商品検索 ==========
        // ★ 問題3: 検索ロジックがここに直書きされている。
        //   同じ検索を別の場所でも使いたいとき、コピペが発生する（重複コード）。
        String searchId = "2";
        System.out.println("【ID=" + searchId + " の商品を検索】");
        String[] found = null;
        for (String[] p : products) {
            if (p[0].equals(searchId)) {
                found = p;
                break;
            }
        }
        if (found != null) {
            System.out.println("  見つかった: " + found[1] + " (" + found[2] + "円)");
        } else {
            System.out.println("  見つからなかった");
        }
        System.out.println();

        // ========== 商品追加 ==========
        // ★ 問題4: バリデーション・ID採番・保存が1か所に混在している。
        //   「価格は0より大きい」というビジネスルールがここにしかなく、
        //   別の追加処理を書いたときに同じチェックを書き忘れる危険がある。
        String newName = "キーボード";
        int newPrice = 8000;
        System.out.println("【商品を追加: " + newName + " " + newPrice + "円】");
        // [Java 7 不可] String.isBlank() は Java 11 以降。Java 7 では isEmpty() + trim() で代替
        if (newName == null || newName.isBlank() || newPrice <= 0) {
            System.out.println("  [エラー] 無効な商品データ");
        } else {
            String newId = String.valueOf(products.size() + 1);
            products.add(new String[]{newId, newName, String.valueOf(newPrice)});
            System.out.println("  追加完了");
        }
        System.out.println();

        // ========== 追加後の一覧表示（上と同じ表示ロジックをもう一度書いている） ==========
        // ★ 問題5: 同じ表示処理をコピペしている。修正が必要になったとき両方直さなければならない。
        System.out.println("【商品一覧（追加後）】");
        for (String[] p : products) {
            System.out.println("  ID=" + p[0] + " 商品名=" + p[1] + " 価格=" + p[2] + "円");
        }
        System.out.println();

        System.out.println("【このコードの問題点】");
        System.out.println("  1. データ・ビジネスロジック・表示が1つのクラスに混在");
        System.out.println("     → 変更の影響範囲が広く、どこを直せばよいかわかりにくい");
        System.out.println("  2. 商品を String[] で扱っており型安全性がない");
        System.out.println("     → p[1] が名前か価格かをコードを読まないと判断できない");
        System.out.println("  3. テストを書けない");
        System.out.println("     → main を実行しないと動作確認できない（自動テスト不可）");
        System.out.println("  4. 永続化方法（ArrayList）の変更が全体に波及する");
        System.out.println("     → DB に移行するとき、どこを直せばよいかが不明確");
    }
}
