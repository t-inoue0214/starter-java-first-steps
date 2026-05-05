/**
 * 【なぜこのコードを学ぶのか】
 * 「似ているから」でクラスを共有すると、関係のない変更が影響し合う。
 * 「同じメソッドを使いたいから」でユーティリティクラスを作ると、
 * 無関係なクラス間に見えにくい結合が生まれる。
 * 重複コードは常に悪ではない。DRY 原則は「知識の重複を避ける」であり
 * 「コードの重複を避ける」ではない。
 */
package com.example.safe_coding;

import java.util.ArrayList;
import java.util.List;

public class ClassResponsibility {

    // ==========================================================
    // Before: 「似ているから」まとめた継承設計
    // ==========================================================

    // [Before] 「どちらも商品リストを持つ」という理由でまとめた親クラス
    private static class ProductContainerBad {
        protected List<String> items;

        ProductContainerBad(List<String> items) {
            this.items = new ArrayList<>(items);
        }

        public List<String> getItems() { return items; }
        public int getTotalCount()     { return items.size(); }
    }

    // 注文データ: ProductContainerBad を継承
    private static class OrderDataBad extends ProductContainerBad {
        private final String orderId;

        OrderDataBad(String orderId, List<String> items) {
            super(items);
            this.orderId = orderId;
        }

        public String getOrderId() { return orderId; }

        @Override
        public String toString() {
            return "Order[" + orderId + "] items=" + items;
        }
    }

    // カートデータ: ProductContainerBad を継承（「似ているから」同じ親を使う）
    private static class CartDataBad extends ProductContainerBad {
        private final String sessionId;

        CartDataBad(String sessionId, List<String> items) {
            super(items);
            this.sessionId = sessionId;
        }

        public String getSessionId() { return sessionId; }

        @Override
        public String toString() {
            return "Cart[session=" + sessionId + "] items=" + items;
        }
    }

    // ==========================================================
    // After: 独立したクラス設計
    // ==========================================================

    // 注文データと在庫アイテム数を数える共通インターフェース（必要な場合のみ）
    private interface ItemCountable {
        int getItemCount();
    }

    // 注文データ: 独立したクラス（注文専用のフィールドだけ持つ）
    private static class OrderData implements ItemCountable {
        private final String orderId;
        private final List<String> items;

        OrderData(String orderId, List<String> items) {
            this.orderId = orderId;
            this.items   = List.copyOf(items); // [Java 10+] イミュータブルなコピー
        }

        public String       getOrderId()   { return orderId; }
        public List<String> getItems()     { return items; }
        @Override
        public int          getItemCount() { return items.size(); }

        @Override
        public String toString() {
            return "Order[" + orderId + "] items=" + items;
        }
    }

    // カートデータ: 独立したクラス（カート専用のフィールドだけ持つ）
    private static class CartData implements ItemCountable {
        private final String sessionId;
        private final List<String> items;

        CartData(String sessionId, List<String> items) {
            this.sessionId = sessionId;
            this.items     = List.copyOf(items);
        }

        public String       getSessionId() { return sessionId; }
        public List<String> getItems()     { return items; }
        @Override
        public int          getItemCount() { return items.size(); }

        @Override
        public String toString() {
            return "Cart[session=" + sessionId + "] items=" + items;
        }
    }

    // ==========================================================
    // ユーティリティクラスの乱用（Before / After）
    // ==========================================================

    // [Before] 「注文もカートも価格をフォーマットするから」という理由で共有
    private static class PriceUtils {
        // [危険] OrderData と CartData が同じユーティリティに依存する
        // → 請求書の表示形式を変えると注文一覧の表示も変わってしまう
        static String format(int price) {
            return String.format("%,d円", price);
        }
    }

    // ==========================================================
    // メインメソッド
    // ==========================================================
    public static void main(String[] args) {

        // ----------------------------------------------------------
        // 1. Before: 「似ているから」まとめた継承のアンチパターン
        // ----------------------------------------------------------
        System.out.println("=== 1. Before: 「似ているから」で継承してまとめる ===");
        System.out.println();

        OrderDataBad badOrder = new OrderDataBad("ORDER-001", List.of("ノートPC", "マウス"));
        CartDataBad  badCart  = new CartDataBad("SESSION-XYZ", List.of("キーボード"));

        System.out.println("  注文: " + badOrder + " 件数=" + badOrder.getTotalCount());
        System.out.println("  カート: " + badCart  + " 件数=" + badCart.getTotalCount());
        System.out.println();
        System.out.println("  問題: この設計で「注文の items に数量フィールドを追加したい」となったとき、");
        System.out.println("    親クラス ProductContainerBad の List<String> を");
        System.out.println("    List<OrderItem> に変えると CartDataBad も影響を受ける。");
        System.out.println("    「注文とカートは似ているだけで、変わる理由が異なる」");
        System.out.println("    → 同じ親クラスを持つべきではなかった。");
        System.out.println();

        // ----------------------------------------------------------
        // 2. After: 独立したクラス + 必要なら Interface
        // ----------------------------------------------------------
        System.out.println("=== 2. After: 独立したクラスを持ち、共通の振る舞いは Interface で表現する ===");
        System.out.println();

        OrderData goodOrder = new OrderData("ORDER-001", List.of("ノートPC", "マウス"));
        CartData  goodCart  = new CartData("SESSION-XYZ", List.of("キーボード"));

        System.out.println("  注文: " + goodOrder + " 件数=" + goodOrder.getItemCount());
        System.out.println("  カート: " + goodCart  + " 件数=" + goodCart.getItemCount());
        System.out.println();
        System.out.println("  改善: OrderData と CartData は独立しているため、");
        System.out.println("    注文の内部構造を変えてもカートに影響しない。");
        System.out.println("    「アイテム数を数える」という共通の振る舞いだけ");
        System.out.println("    ItemCountable インターフェースで表現している。");
        System.out.println();

        // ItemCountable を使う共通処理の例
        System.out.println("  ItemCountable を使った共通処理（どちらの型でも動く）:");
        printItemCount(goodOrder);
        printItemCount(goodCart);
        System.out.println();

        // ----------------------------------------------------------
        // 3. ユーティリティクラスの乱用と重複コードの許容
        // ----------------------------------------------------------
        System.out.println("=== 3. ユーティリティクラスの乱用と重複コードの許容 ===");
        System.out.println();

        System.out.println("  [Before] PriceUtils を OrderData と InvoiceData が共有する:");
        System.out.println("    OrderData の注文一覧 → PriceUtils.format(price)");
        System.out.println("    InvoiceData の請求書 → PriceUtils.format(price)");
        System.out.println("    → 請求書の表示を「¥150,000」形式に変えたくても、");
        System.out.println("      PriceUtils を変えると注文一覧の表示も変わってしまう。");
        System.out.println();

        System.out.println("  [After] それぞれのクラスに個別に formatPrice() を持つ:");
        System.out.println("    → 重複コードに見えるが「変わる理由が異なる」ため分けておく。");
        System.out.println("    → 請求書の形式変更が注文一覧に影響しない。");
        System.out.println();

        // 「たまたま同じ」コードの例
        System.out.println("  注文一覧のフォーマット: " + formatPriceForOrder(150000));
        System.out.println("  請求書のフォーマット:   " + formatPriceForInvoice(150000));
        System.out.println("  （今は同じ結果だが、変わる理由が違うため別メソッドにしている）");
        System.out.println();

        // ----------------------------------------------------------
        // 4. DRY 原則の正しい理解
        // ----------------------------------------------------------
        System.out.println("=== 4. DRY 原則の正しい理解 ===");
        System.out.println();
        System.out.println("  DRY = Don't Repeat Yourself（同じ知識を繰り返すな）");
        System.out.println();
        System.out.printf("  %-32s  %s%n", "状況", "判断");
        System.out.println("  " + "-".repeat(70));
        System.out.printf("  %-32s  %s%n",
            "同じビジネスルールが2箇所にある",
            "→ まとめるべき（知識の重複）");
        System.out.printf("  %-32s  %s%n",
            "同じ計算式が2箇所にある",
            "→ まとめるべき（知識の重複）");
        System.out.printf("  %-32s  %s%n",
            "偶然同じコードが2箇所にある",
            "→ そのままでよい（変わる理由が異なる）");
        System.out.printf("  %-32s  %s%n",
            "3回以上同じパターンが出現した",
            "→ 抽象化を検討（Rule of Three）");
        System.out.println();
        System.out.println("  → 「コードの見た目の重複」を避けることが DRY ではない。");
        System.out.println("    「同じ知識・同じルールを1箇所で管理する」ことが DRY の本質だ。");

        // ----------------------------------------------------------
        // 5. まとめ
        // ----------------------------------------------------------
        System.out.println();
        System.out.println("=== 5. まとめ ===");
        System.out.println();
        System.out.printf("  %-30s  %s%n", "アンチパターン", "問題");
        System.out.println("  " + "-".repeat(70));
        System.out.printf("  %-30s  %s%n",
            "「似ているから」継承",
            "変わる理由が異なるクラスが結合する");
        System.out.printf("  %-30s  %s%n",
            "ユーティリティクラスの乱用",
            "無関係なクラス間に見えにくい結合が生まれる");
        System.out.printf("  %-30s  %s%n",
            "重複コードの撲滅",
            "変わる理由が異なるコードまで共通化してしまう");
        System.out.println();
        System.out.printf("  %-30s  %s%n", "正しいアプローチ", "効果");
        System.out.println("  " + "-".repeat(70));
        System.out.printf("  %-30s  %s%n",
            "独立したクラスを持つ",
            "変更が局所化される");
        System.out.printf("  %-30s  %s%n",
            "共通の振る舞いは Interface",
            "実装の共有なしに多態性を使える");
        System.out.printf("  %-30s  %s%n",
            "重複コードの許容",
            "変更が他クラスに波及しない");
    }

    // ItemCountable を受け取る共通処理
    private static void printItemCount(ItemCountable countable) {
        System.out.printf("    %s のアイテム数: %d%n",
            countable.getClass().getSimpleName(), countable.getItemCount());
    }

    // 注文一覧用の価格フォーマット（重複コードだが「変わる理由」が異なる）
    private static String formatPriceForOrder(int price) {
        return String.format("%,d円", price);
    }

    // 請求書用の価格フォーマット（将来的に "¥150,000" 形式に変わるかもしれない）
    private static String formatPriceForInvoice(int price) {
        return String.format("%,d円", price);
    }
}
