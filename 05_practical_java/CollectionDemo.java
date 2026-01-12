import java.util.ArrayList;
import java.util.HashMap;

public class CollectionDemo {
    public static void main(String[] args) {

        System.out.println("--- 1. List (リスト) の実験 ---");
        // 配列と違って、後から自由にデータを追加・削除できます。
        // <String> は「中身は文字だよ」という指定です。
        ArrayList<String> weapons = new ArrayList<>();

        weapons.add("木の剣");
        weapons.add("鉄の剣");
        weapons.add("勇者の剣");

        // 中身を順番に表示
        for (String w : weapons) {
            System.out.println("武器: " + w);
        }

        System.out.println("個数: " + weapons.size()); // .length ではなく .size()

        System.out.println("\n--- 2. Map (マップ) の実験 ---");
        // 「キー」と「値」をセットで保存します（辞書のようなもの）。
        // <String, Integer> は「キーは文字、値は数字」という指定です。
        HashMap<String, Integer> items = new HashMap<>();

        items.put("薬草", 100);
        items.put("毒消し", 150);

        // 名前（キー）を使って、値段（値）を取り出す
        int price = items.get("薬草");
        System.out.println("薬草の値段: " + price + "ゴールド");
    }
}