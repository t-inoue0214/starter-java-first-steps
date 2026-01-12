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

        // 3. 正解するまで繰り返す (無限ループ)
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
        scanner.close(); // 後片付け
    }
}