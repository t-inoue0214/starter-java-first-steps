public class Condition {
    public static void main(String[] args) {
        int score = 85;

        // 1. 基本的なif文
        // カッコの中身は必ず「true」か「false」になる式でなければいけません
        if (score >= 60) {
            System.out.println("合格です！");
        } else {
            System.out.println("不合格...");
        }

        // 2. 複数の条件 ( && は「かつ」、 || は「または」)
        if (score >= 80 && score < 90) {
            System.out.println("あと少しで90点！惜しい！");
        }

        // 【実験】 他の言語（JavaScriptなど）との違い
        // Javaでは、数字や文字列をそのまま条件にすることはできません。
        // 以下のコメントを外すとエラーになります。

        // if (1) { System.out.println("これはエラーになります"); }
    }
}