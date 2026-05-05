/**
 * 【なぜこのコードを学ぶのか】
 * Map<String, Object> はコンテキストデータを詰め込むのに便利だが、
 * 「何が何型で入っているか」が実行時にしかわからない。
 * Enum と専用クラスでコンパイル時に誤りを検出できる。
 * また「似ているデータを1クラスにまとめない」ことで、
 * 無関係な変更が影響し合うことを防ぐ。
 */
package com.example.safe_coding;

import java.util.HashMap;
import java.util.Map;

public class TypeSafety {

    // ---------------------------------------------------------
    // ========== Before: String 定数でステータスを管理 ==========
    // ---------------------------------------------------------

    // [Before] String 定数でステータスを管理する方法
    // 定数を使えばタイポは減るが、「STATUS_ で始まる文字列なら何でも渡せてしまう」問題は残る
    private static final String STATUS_PENDING   = "PENDING";
    private static final String STATUS_ACTIVE    = "ACTIVE";
    @SuppressWarnings("unused")
    private static final String STATUS_CANCELLED = "CANCELLED";

    // ---------------------------------------------------------
    // ========== After: Enum でステータスを管理 ==========
    // ---------------------------------------------------------

    // [Java 7 動作差異] Enum 自体は Java 5 以降だが、switch 式の -> 構文は Java 14 以降
    // Java 7 では従来の switch 文（case:, break;）を使う
    private enum OrderStatus {
        PENDING, ACTIVE, CANCELLED
    }

    // ---------------------------------------------------------
    // ========== After: 専用クラスでコンテキストを表現 ==========
    // ---------------------------------------------------------

    // [Java 7 不可] Record は Java 16 以降。Java 7 では通常のクラス（コンストラクタ＋getter）で書く
    // 注文に関するコンテキスト情報をまとめた専用クラス
    private record OrderContext(String orderId, String userId, int quantity) {}

    // 在庫に関するコンテキスト情報をまとめた別の専用クラス
    // 「注文」と「在庫」は同じ商品を扱うが、変更の理由が異なるため別クラスにする
    private record InventoryContext(String productId, int availableStock) {}

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // セクション1: String 定数の問題（タイポが実行時まで気づけない）
        // ---------------------------------------------------------
        System.out.println("=== 1. Before: String 定数ではタイポが実行時まで気づけない ===");
        System.out.println();

        String status = STATUS_ACTIVE;
        System.out.println("  現在のステータス: " + status);

        // タイポ: ACTIVE → ACITVE（実は逆の文字）
        // コンパイラはこれをエラーにしない。ただの文字列比較のため false になるだけ
        if (status.equals("ACITVE")) { // ← タイポ! ACTIVE ではなく ACITVE
            System.out.println("  アクティブです");
        } else {
            System.out.println("  アクティブと判定されなかった（タイポによる不具合）");
        }
        System.out.println("  → コンパイラはこのタイポを検出できない。実行して初めて気づく。");

        // 定数を比較しても、誤った定数名ならコンパイルエラーになる（良い点）
        // ただし定数を使わず直接文字列リテラルを書けばタイポを防げない
        if (status.equals(STATUS_PENDING)) {
            System.out.println("  保留中です");
        } else if (status.equals(STATUS_ACTIVE)) {
            System.out.println("  アクティブです（定数を使えば比較はOK）");
        }

        // ---------------------------------------------------------
        // セクション2: Enum での解決（コンパイル時にタイポを検出）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 2. After: Enum で選択肢をコンパイル時に固定する ===");
        System.out.println();

        OrderStatus orderStatus = OrderStatus.ACTIVE;
        System.out.println("  orderStatus: " + orderStatus);

        // [Java 7 不可] switch 式の -> 構文は Java 14 以降。
        //   Java 7 では switch 文で書く:
        //   switch (orderStatus) {
        //       case PENDING:   System.out.println("  保留中"); break;
        //       case ACTIVE:    System.out.println("  処理中"); break;
        //       case CANCELLED: System.out.println("  キャンセル済み"); break;
        //   }
        String description = switch (orderStatus) {
            case PENDING   -> "保留中";
            case ACTIVE    -> "処理中";
            case CANCELLED -> "キャンセル済み";
        };
        System.out.println("  ステータス説明: " + description);
        System.out.println();
        System.out.println("  タイポ防止の効果:");
        System.out.println("    OrderStatus.ACITVE → コンパイルエラー");
        System.out.println("    （ACITVE という名前の定数は存在しないため、コンパイラが即座に指摘する）");
        System.out.println("  switch 式の網羅性チェック:");
        System.out.println("    CANCELLED の case を削除すると「網羅的でない」コンパイルエラーになる");
        System.out.println("    → 新しいステータスを Enum に追加したとき、switch の修正漏れをコンパイラが指摘する");

        // ---------------------------------------------------------
        // セクション3: Map 乱用の問題
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 3. Before: Map<String, Object> にコンテキストを詰め込む ===");
        System.out.println();

        Map<String, Object> context = new HashMap<>();
        context.put("orderId",  "ORDER-001");
        context.put("userId",   "USER-001");
        context.put("quantity", 5);

        System.out.println("  context に詰め込んだデータ: " + context);

        // タイポ: "orderId" → "orderid"（小文字の 'i'）
        // Map はキーが存在しなければ null を返すだけでエラーにならない
        String orderId = (String) context.get("orderid"); // ← タイポ！
        System.out.println("  orderId の取得（タイポあり）: " + orderId);
        System.out.println("  → null が返る。実行するまでタイポに気づけない。");
        System.out.println("  → (String) キャストが必要。型が違えば ClassCastException が発生する。");
        System.out.println("  → Map を見ても「何が何型で入っているか」がコードからわからない。");

        // ---------------------------------------------------------
        // セクション4: 専用クラスでの解決
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 4. After: 専用クラスでコンテキストを表現する ===");
        System.out.println();

        // [Java 7 不可] Record は Java 16 以降
        OrderContext order = new OrderContext("ORDER-001", "USER-001", 5);
        System.out.println("  order: " + order);

        // フィールド名はコンパイラがチェックする
        // order.orderid() と書けばコンパイルエラーになる（orderId() が正しい）
        System.out.println("  orderId: " + order.orderId());
        System.out.println("  userId:  " + order.userId());
        System.out.println("  quantity: " + order.quantity());
        System.out.println();

        // 在庫コンテキストは別クラス
        InventoryContext inventory = new InventoryContext("PROD-001", 100);
        System.out.println("  inventory: " + inventory);
        System.out.println("  productId: " + inventory.productId());
        System.out.println("  availableStock: " + inventory.availableStock());
        System.out.println();
        System.out.println("  OrderContext と InventoryContext を分ける理由:");
        System.out.println("    どちらも商品に関するデータを持つが、変更の理由が異なる。");
        System.out.println("    注文処理の変更（例: couponCode フィールドを追加）が");
        System.out.println("    在庫管理のコードに影響すべきでない。");
        System.out.println("    → 変更理由が異なるものは別クラスにする（単一責任の原則）。");

        // ---------------------------------------------------------
        // セクション5: まとめ
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 5. まとめ ===");
        System.out.println();
        System.out.println("  方法              タイポ検出       型エラー検出");
        System.out.println("  ─────────────────────────────────────────────────────────────");
        System.out.println("  String 定数       実行時のみ       実行時のみ（ClassCastException）");
        System.out.println("  Enum              コンパイル時     ―（型は固定）");
        System.out.println("  Map<String,Object> 実行時のみ      実行時のみ（ClassCastException）");
        System.out.println("  専用クラス/Record コンパイル時     コンパイル時");
        System.out.println();
        System.out.println("  → コンパイル時に検出できる = テストを書く前にバグを発見できる");
        System.out.println("  → 実行時まで気づけない = 本番環境で初めてバグが顕在化するリスクがある");
    }
}
