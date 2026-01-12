public class Loop {
    public static void main(String[] args) {
        System.out.println("--- 1. 配列の違い ---");

        // 【プリミティブ型の配列】
        // 中身は「数字そのもの」が入っています
        int[] numbers = { 10, 20, 30 };

        // 【参照型の配列】
        // 中身は「文字列への参照（場所）」が入っています
        String[] names = { "Alice", "Bob", "Charlie" };

        System.out.println("--- 2. 拡張for文（データを取り出す） ---");

        // 配列の中身を全部見るときは、この書き方が一番簡単で安全です。
        // 「namesから1つずつ name に入れて繰り返す」と読みます。
        for (String name : names) {
            System.out.println("名前: " + name);
        }

        System.out.println("--- 3. 基本的なfor文（回数を指定） ---");

        // 「何回繰り返すか」を数字で管理したいときは、こちらを使います。
        // i++ は「iを1増やす」という意味です。
        for (int i = 1; i <= 5; i++) {
            System.out.println(i + "回目の繰り返し");
        }
    }
}