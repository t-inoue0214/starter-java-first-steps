/**
 * 【なぜこのコードを学ぶのか】
 * 型を意識せずにプログラムを書くと「なぜか割り算の答えが整数になる」
 * 「数字と消費税を掛けたらコンパイルエラーになった」といった落とし穴にはまります。
 * Javaは型に厳格な言語です。型のルールを体験することで、
 * エラーを読んで自分で直せる力が身につきます。
 */
package com.example.variables_and_types;

public class Calc {
    public static void main(String[] args) {
        // -------------------------
        // 1. 変数の定義と更新（= の意味）
        // -------------------------
        int number = 10;
        System.out.println("元の数字: " + number);

        // 「右側(10 + 1)を計算してから、左側(number)に入れ直す」
        number = number + 1;
        System.out.println("1増えた: " + number);
        // 数学だと 10 = 10 + 1 は矛盾しますが、プログラムでは正解です。
        // 「右側(10 + 1)を計算してから、左側(number)に入れ直す」という動きをします。

        // -------------------------
        // 2. いろいろな型
        // -------------------------
        double pi = 3.14;
        char initial = 'J'; // 1文字はシングルクォート
        boolean isJavaFun = true;
        String message = "Java"; // 文字列はダブルクォート

        System.out.println(initial + "ava"); // 文字をつなげることもできます

        // -------------------------
        // 3. 整数の計算と「割り算の罠」
        // -------------------------
        int apple = 5;
        int people = 2;
        System.out.println("整数の割り算: " + (apple / people)); // 結果は 2.5 ではなく 2 になってしまう！

        // -------------------------
        // 4. キャスト（型変換）を使って期待値通りの計算をする
        // -------------------------
        // (double) をつけることで、「一時的に実数として扱って！」と命令します
        System.out.println("正確な割り算: " + ((double) apple / people));

        // -------------------------
        // 5. 型の不一致エラー
        // -------------------------
        double tax = 0.1;
        int price = 1000;

        // 以下の行のコメント（//）を消すと、赤い波線（エラー）が出ます。試してみましょう。
        // int result = price * tax;

        // 理由：計算結果は double (100.0) になるため、小さな int の箱には入りません。
        // これを無理やり入れるときもキャストを使います。
        int result = (int) (price * tax);
        System.out.println("消費税: " + result);

        // -------------------------
        // 6. 「余り」の計算 (%)
        // -------------------------
        System.out.println("10 ÷ 3 の余りは: " + (10 % 3)); // 1が表示される

        // -------------------------
        // 7. var キーワード（Java 10以降の型推論）
        // -------------------------
        // 右辺から型が明らかなときは、左辺の型名を var に省略できます。
        // コンパイラが「これは String だ」「これは int だ」と自動で推論します。
        var text = "Java is fun"; // コンパイラが String と判断する
        var count = 42;           // コンパイラが int と判断する
        var pi2 = 3.14;           // コンパイラが double と判断する

        System.out.println("var text  の型: " + ((Object) text).getClass().getSimpleName());
        System.out.println("var count の型: " + ((Object) count).getClass().getSimpleName());
        System.out.println("var pi2   の型: " + ((Object) pi2).getClass().getSimpleName());
        // 注意: var はメソッド内のローカル変数にしか使えません（フィールドには使えない）。
        // また、宣言時に右辺がないと型を推論できないため、
        // 「var x;」のように初期値なしで書くとコンパイルエラーになります。

        // -------------------------
        // 8. 文字列結合の罠と StringBuilder
        // -------------------------
        // ========== Before: ループ内で += を使う問題のあるコード ==========
        // String は「不変（イミュータブル）」なオブジェクトです。
        // += で文字列を繋ぐたびに、裏側では「新しい String オブジェクト」が毎回生成されています。
        // 少量なら問題になりませんが、繰り返し回数が増えるとメモリと時間を大量に消費します。
        String resultBefore = "";
        long startBefore = System.nanoTime(); // 計測開始
        for (int i = 0; i < 10_000; i++) {
            resultBefore += i; // 毎回新しい String が生まれる！
        }
        long elapsedBefore = System.nanoTime() - startBefore;
        System.out.println("Before（+=）: " + elapsedBefore + " ns, 文字数=" + resultBefore.length());

        // ========== After: StringBuilder を使う改善したコード ==========
        // StringBuilder は「変更可能な文字列バッファ」です。
        // append() で文字を追加しても新しいオブジェクトは生まれず、同じバッファを使い回します。
        // 最後に toString() で String に変換します。
        StringBuilder sb = new StringBuilder();
        long startAfter = System.nanoTime(); // 計測開始
        for (int i = 0; i < 10_000; i++) {
            sb.append(i); // バッファに追記するだけ。オブジェクト生成なし
        }
        String resultAfter = sb.toString(); // 最後に一度だけ String に変換
        long elapsedAfter = System.nanoTime() - startAfter;
        System.out.println("After（StringBuilder）: " + elapsedAfter + " ns, 文字数=" + resultAfter.length());
        // 実行してみると Before より After の方が大幅に速いことが確認できます。
        // ※ StringBuilder の詳細は第07章で改めて学びます。
    }
}
