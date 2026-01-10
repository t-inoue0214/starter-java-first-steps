public class Loop {
    public static void main(String[] args) {
        // 配列：データをまとめて管理する箱
        int[] scores = {10, 20, 30, 40, 50};

        System.out.println("--- 1. 基本的なfor文 ---");
        // 5回繰り返す（0から4まで）
        // i++ は「iを1増やす」という意味です
        for (int i = 0; i < scores.length; i++) {
            System.out.println(i + "番目のデータ: " + scores[i]);
        }

        System.out.println("--- 2. 拡張for文（おすすめ） ---");
        // データすべてを順番に取り出す簡単な書き方
        // 「scores の中身を一つずつ s に入れて繰り返す」と読みます
        int total = 0;
        for (int s : scores) {
            System.out.println("データ: " + s);
            total = total + s; // 合計を計算
        }
        
        System.out.println("合計点: " + total);
    }
}