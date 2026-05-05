/**
 * 【なぜこのコードを学ぶのか】
 * String定数で状態を管理すると、誤字があってもコンパイラはエラーを出さない。
 * "ORERED" と書いてしまっても実行時まで気づけない。
 * Enumを使うと「存在しない状態」をコンパイラが弾いてくれるため、
 * バグを実行前に発見できる。現場でEnumが好まれる理由はここにある。
 */
package com.example.oop_and_type_system;

public class EnumBasics {

    // ---------------------------------------------------------
    // ========== Before: String定数で状態管理（問題のある書き方） ==========
    // ---------------------------------------------------------

    // 注文状態をStringの定数で管理している（昔ながらの書き方）
    private static final String STATUS_ORDERED   = "ORDERED";
    private static final String STATUS_SHIPPED   = "SHIPPED";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    // String定数を受け取って状態を表示するメソッド（Before版）
    private static void showStatusBefore(String status) {
        // if-elseで文字列を比較している
        if (status.equals(STATUS_ORDERED)) {
            System.out.println("[Before] 注文受付済み");
        } else if (status.equals(STATUS_SHIPPED)) {
            System.out.println("[Before] 発送済み");
        } else if (status.equals(STATUS_DELIVERED)) {
            System.out.println("[Before] 配達完了");
        } else if (status.equals(STATUS_CANCELLED)) {
            System.out.println("[Before] キャンセル済み");
        } else {
            // 誤字があってもコンパイルは通り、ここに落ちてしまう
            System.out.println("[Before] 不明な状態: " + status);
        }
    }

    // ---------------------------------------------------------
    // ========== After: Enumで状態管理（安全な書き方） ==========
    // ---------------------------------------------------------

    // Enumを使うと「注文状態」として有効な値だけを型で表現できる
    // 選択肢が明確で、誤字の心配もない
    private enum OrderStatus {
        ORDERED,    // 注文受付済み
        SHIPPED,    // 発送済み
        DELIVERED,  // 配達完了
        CANCELLED   // キャンセル済み
    }

    // OrderStatus型を受け取るメソッド（After版）
    // Java 21のswitch式（-> 構文）でスッキリ書ける
    private static void showStatusAfter(OrderStatus status) {
        // switch式はすべてのケースを網羅しないとコンパイルエラーになる
        // 新しい状態をEnumに追加したとき、ここでの対応漏れをコンパイラが教えてくれる
        // [Java 7 不可] switch 式（-> 構文）は Java 14 以降。Java 7 では switch 文で書く:
        //   switch (status) { case ORDERED: message = "注文受付済み"; break; ... }
        String message = switch (status) {
            case ORDERED   -> "注文受付済み";
            case SHIPPED   -> "発送済み";
            case DELIVERED -> "配達完了";
            case CANCELLED -> "キャンセル済み";
        };
        System.out.println("[After] " + message);
    }

    // ---------------------------------------------------------
    // 応用例: Enumにフィールドとメソッドを持たせる
    // ---------------------------------------------------------

    // Enumはただの定数一覧ではなく、フィールドやメソッドも持てる
    private enum Season {
        SPRING("春"), SUMMER("夏"), AUTUMN("秋"), WINTER("冬");

        // 各定数が持つフィールド（日本語名）
        private final String japanese;

        // コンストラクタ（private が Enum の慣習）
        Season(String japanese) {
            this.japanese = japanese;
        }

        // 日本語名を返すメソッド
        public String japanese() {
            return japanese;
        }
    }

    public static void main(String[] args) {

        System.out.println("=== Before: String定数で状態管理 ===");

        // 正しい定数を使った場合
        showStatusBefore(STATUS_ORDERED);

        // 問題: 誤字しても コンパイルは通り、実行時に「不明な状態」として処理される
        // "ORERED" は "ORDERED" の誤字だが、コンパイラは何も言わない
        String typo = "ORERED"; // ← 誤字！ でもコンパイルエラーにならない
        showStatusBefore(typo);
        // 出力: [Before] 不明な状態: ORERED  ← バグが実行時まで気づけない

        System.out.println();
        System.out.println("=== After: Enumで状態管理 ===");

        // Enumを使えば存在しない値はコンパイル時点でエラーになる
        showStatusAfter(OrderStatus.ORDERED);
        showStatusAfter(OrderStatus.SHIPPED);
        showStatusAfter(OrderStatus.DELIVERED);
        showStatusAfter(OrderStatus.CANCELLED);

        // もし OrderStatus.ORERED と書いたら → コンパイルエラー！ 実行前に気づける
        // showStatusAfter(OrderStatus.ORERED); // コンパイルエラー: ORERED は存在しない

        System.out.println();
        System.out.println("=== 応用: フィールドを持つEnum (Season) ===");

        // すべての季節をループして日本語名を表示する
        for (Season season : Season.values()) {
            // name() は定数名（英語）を返す組み込みメソッド
            System.out.println(season.name() + " → " + season.japanese());
        }

        // switch式と組み合わせる応用例
        Season current = Season.SPRING;
        // [Java 7 不可] switch 式（-> 構文）は Java 14 以降。Java 7 では switch 文で書く:
        //   switch (current) { case SPRING: advice = "お花見シーズンです"; break; ... }
        String advice = switch (current) {
            case SPRING -> "お花見シーズンです";
            case SUMMER -> "熱中症に気をつけて";
            case AUTUMN -> "読書の秋です";
            case WINTER -> "暖かくしてください";
        };
        System.out.println(current.japanese() + ": " + advice);
    }
}
