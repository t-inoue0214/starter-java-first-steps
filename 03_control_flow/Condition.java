public class Condition {
    public static void main(String[] args) {
        int score = 85;

        // 条件分岐（if - else if - else）
        // 上から順番にチェックされます
        if (score >= 90) {
            System.out.println("素晴らしい！最高評価です。");
        } else if (score >= 60) {
            System.out.println("合格です。おめでとう！");
        } else {
            System.out.println("残念、不合格です...");
        }

        // 応用：複合条件
        // && は「かつ」、 || は「または」
        if (score >= 80 && score < 90) {
            System.out.println("あと少しで90点でした！");
        }
    }
}