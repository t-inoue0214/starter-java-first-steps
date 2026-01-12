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
    }
}