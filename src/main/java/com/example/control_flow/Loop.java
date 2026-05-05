/**
 * 【なぜこのコードを学ぶのか】
 * ループは「同じ処理を繰り返す」だけでなく、配列やリストの「全要素を処理する」
 * 場面で毎日使います。インデックス（添字）を使う for 文と、
 * インデックスを隠した拡張 for 文を使い分けることで、
 * バグの少ないコードが書けるようになります。
 */
package com.example.control_flow;

public class Loop {
    public static void main(String[] args) {
        System.out.println("--- 1. 配列の基本 ---");

        // 【プリミティブ型の配列】
        // 中身は「数字そのもの」が入っています
        int[] numbers = { 10, 20, 30 };

        // 【参照型の配列】
        // 中身は「文字列への参照（場所）」が入っています
        String[] names = { "Alice", "Bob", "Charlie" };

        // 配列の番号は「1」からではなく「0」から始まります。
        // コンピュータは「先頭からの距離」で位置を管理するため、
        // 先頭は距離0（ゼロ）になります。
        System.out.println("numbers[0] = " + numbers[0]); // 10（最初の要素）
        System.out.println("numbers[1] = " + numbers[1]); // 20（2番目の要素）
        System.out.println("numbers[2] = " + numbers[2]); // 30（3番目の要素）

        // 【実験】存在しない番号にアクセスするとエラーになります。
        // 以下のコメントを外して実行すると ArrayIndexOutOfBoundsException が発生します。
        // numbers が持っている番号は 0, 1, 2 だけなので、3 は存在しません。
        // System.out.println(numbers[3]); // → ArrayIndexOutOfBoundsException!

        System.out.println("--- 2. for文で配列を処理する ---");

        // ========== Before: 基本for文（添字を使う従来の書き方）==========
        // 「全要素を順番に処理する」だけなのに、添字 i の初期化・終了条件・更新を3つ書く必要がある。
        // 特に終了条件を1つ間違えるとエラーになります。
        // → i < names.length のはずが i <= names.length と書くと names[3] にアクセスしてしまう！
        //   （試したい場合は <= に変えて実行してみてください → ArrayIndexOutOfBoundsException が出ます）
        for (int i = 0; i < names.length; i++) {
            System.out.println("名前（基本for）: " + names[i]);
        }

        // ========== After: 拡張for文（全要素を処理する現代の書き方）==========
        // 添字の管理が不要なため、off-by-one（1つズレ）エラーが起きません。
        // 「配列の中身を全部処理したい」だけなら常にこちらを使いましょう。
        // : は「の中から」と読むと覚えると分かりやすいです。
        for (String name : names) {
            System.out.println("名前（拡張for）: " + name);
        }

        System.out.println("--- 3. 回数を指定するfor文 ---");

        // 配列ではなく「1から5まで数える」のようにカウンターとして使いたい場合。
        // i++ は「iを1増やす」という意味です。
        for (int i = 1; i <= 5; i++) {
            System.out.println(i + "回目の繰り返し");
        }

        System.out.println("--- 4. 添字forが必要なケース ---");

        // 拡張for文では添字（番号）が使えません。
        // 「偶数番目だけ処理したい」「逆順に処理したい」などの場合は基本for文を使います。
        // numbers.length は配列の要素数（ここでは 3）を返します。
        for (int i = 0; i < numbers.length; i++) {
            System.out.println("numbers[" + i + "] = " + numbers[i]);
        }

        System.out.println("--- 5. while文（終わりが決まっていない繰り返し）---");

        // for文は繰り返し回数が決まっているときに向いています。
        // while文は「ある条件を満たす間ずっと繰り返す」ときに向いています。
        // 例: ゲームのメインループ（プレイヤーが終了するまでずっと動かす）
        int countdown = 3; // カウントダウン開始の数字
        System.out.println("--- カウントダウン開始 ---");
        // countdown が 0 より大きい間、繰り返す
        while (countdown > 0) {
            System.out.println(countdown + "...");
            // countdown-- は「countdownを1減らす」という意味（countdown = countdown - 1 と同じ）
            // ★ これを消すと countdown が永遠に減らず「無限ループ」になります！
            //   もし止まらなくなったら、ターミナルで Ctrl+C を押してください。
            countdown--;
        }
        System.out.println("発射！");
    }
}
