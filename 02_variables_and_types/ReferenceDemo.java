public class ReferenceDemo {
    public static void main(String[] args) {
        // -------------------------
        // 【プリミティブ型】
        // -------------------------
        // データそのものが入っています。小文字で始まります (int, double, boolean)。
        int number = 100;
        
        // -------------------------
        // 【参照型】
        // -------------------------
        // データの「場所」が入っています。大文字で始まります (String など)。
        // 便利な機能（メソッド）をたくさん持っています。
        String message = "Hello Java";

        System.out.println(number);
        System.out.println(message);

        // 参照型だけの特徴：データに対して「命令」ができる！
        // 文字数を数える命令
        System.out.println("文字数: " + message.length());
        
        // 大文字に変換する命令
        System.out.println("大文字: " + message.toUpperCase());
        
        // プリミティブ型でこれをやろうとするとエラーになります
        // number.length(); // ← これはできません
    }
}