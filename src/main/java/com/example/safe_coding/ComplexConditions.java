/**
 * 【なぜこのコードを学ぶのか】
 * 企業設定とユーザー設定のどちらを使うかを決める複雑な条件分岐を、
 * ハンドラのインライン記述にすると「値がセットされないケース」に気づきにくい。
 * メソッドに切り出すと戻り値の型が固定され、未処理のケースが型検査で検出される。
 * [Java 8+] Optional を使って「値が存在しない可能性」を型で表現する。
 */
package com.example.safe_coding;

import java.util.Optional;

public class ComplexConditions {

    // ---------------------------------------------------------
    // 設定クラス（企業設定とユーザー設定）
    // ---------------------------------------------------------
    // [Java 7 不可] record は Java 16 以降
    private record CompanySetting(boolean enabled, String value) {}
    private record UserSetting(boolean enabled, String value) {}

    // ---------------------------------------------------------
    // Before: 複雑な条件分岐をインラインに書く
    // ---------------------------------------------------------
    // ★ 問題: result が String 型で宣言されているため、
    //   すべての if-else を通っても result に値が入らないケースがある。
    //   コンパイラが「初期化されていない可能性」を検出してくれる場合もあるが、
    //   複雑になると見落とされやすい。
    private static void handleRequestBad(String requestId,
                                         CompanySetting co, UserSetting user) {
        String result;  // 未初期化

        if (user.enabled() && user.value() != null && !user.value().isBlank()) {
            result = user.value();
        } else if (co.enabled() && co.value() != null && !co.value().isBlank()) {
            result = co.value();
        } else {
            // ↓ 両方とも無効なときのデフォルト処理を「うっかり書き忘れる」ことがある
            result = "(設定なし)";  // これを忘れるとコンパイルエラーまたは実行時 NPE
        }

        System.out.printf("  リクエスト %-12s → 設定値: %s%n", requestId, result);
    }

    // ---------------------------------------------------------
    // After: Optional<String> を返すメソッドに切り出す
    // ---------------------------------------------------------
    // ★ 改善: メソッドは「必ず何かを返す」ため、未処理ケースが発生しない。
    //   Optional.empty() が「値なし」を明示的に表す。
    //   呼び出し側は Optional を受け取るため「値がない可能性」を無視できない。
    private static Optional<String> resolveEffectiveSetting(CompanySetting co,
                                                             UserSetting user) {
        // ユーザー設定を優先する（有効かつ値がある場合）
        if (user.enabled() && user.value() != null && !user.value().isBlank()) {
            return Optional.of(user.value());
        }
        // 企業設定にフォールバック
        if (co.enabled() && co.value() != null && !co.value().isBlank()) {
            return Optional.of(co.value());
        }
        // どちらも有効でない
        return Optional.empty();
    }

    private static void handleRequestGood(String requestId,
                                          CompanySetting co, UserSetting user) {
        Optional<String> setting = resolveEffectiveSetting(co, user);

        // [Java 7 不可] ifPresentOrElse() は Java 9 以降
        // [Java 7 代替] if (setting.isPresent()) { ... } else { ... }
        setting.ifPresentOrElse(
            value -> System.out.printf("  リクエスト %-12s → 設定値: %s%n", requestId, value),
            ()    -> System.out.printf("  リクエスト %-12s → 設定値: (デフォルト値を使用)%n", requestId)
        );
    }

    // ---------------------------------------------------------
    // Switch 式の補足（Java 14+）
    // ---------------------------------------------------------
    private enum SettingPriority { USER, COMPANY, NONE }

    private static SettingPriority determinePriority(CompanySetting co, UserSetting user) {
        // [Java 7 不可] switch 式は Java 14 以降（プレビュー機能を含む）
        // switch 式では全 case を網羅しないとコンパイルエラーになる
        if (user.enabled() && user.value() != null && !user.value().isBlank()) {
            return SettingPriority.USER;
        }
        if (co.enabled() && co.value() != null && !co.value().isBlank()) {
            return SettingPriority.COMPANY;
        }
        return SettingPriority.NONE;
    }

    // ---------------------------------------------------------
    // メインメソッド
    // ---------------------------------------------------------
    public static void main(String[] args) {

        // ---------------------------------------------------------
        // 1. Before: インラインの条件分岐
        // ---------------------------------------------------------
        System.out.println("=== 1. Before: 複雑な条件分岐をインラインに書く ===");
        System.out.println();
        System.out.println("  問題: 条件が増えるにつれて「どのケースで何がセットされるか」が");
        System.out.println("        把握しにくくなる。値が未セットのまま使われるバグに気づきにくい。");
        System.out.println();

        handleRequestBad("REQ-001",
            new CompanySetting(true, "企業設定値"),
            new UserSetting(false, ""));
        handleRequestBad("REQ-002",
            new CompanySetting(false, ""),
            new UserSetting(true, "ユーザー設定値"));
        handleRequestBad("REQ-003",
            new CompanySetting(true, "企業設定値"),
            new UserSetting(true, "ユーザー設定値"));  // ユーザー優先
        handleRequestBad("REQ-004",
            new CompanySetting(false, ""),
            new UserSetting(false, ""));               // 両方無効
        System.out.println();

        // ---------------------------------------------------------
        // 2. After: Optional<String> を返すメソッドに切り出す
        // ---------------------------------------------------------
        System.out.println("=== 2. After: resolveEffectiveSetting() に切り出して Optional を返す ===");
        System.out.println();
        System.out.println("  改善: メソッドは必ず値を返すため「未処理ケース」が発生しない。");
        System.out.println("        Optional を受け取る呼び出し側は「値がない可能性」を無視できない。");
        System.out.println();

        handleRequestGood("REQ-001",
            new CompanySetting(true, "企業設定値"),
            new UserSetting(false, ""));
        handleRequestGood("REQ-002",
            new CompanySetting(false, ""),
            new UserSetting(true, "ユーザー設定値"));
        handleRequestGood("REQ-003",
            new CompanySetting(true, "企業設定値"),
            new UserSetting(true, "ユーザー設定値"));
        handleRequestGood("REQ-004",
            new CompanySetting(false, ""),
            new UserSetting(false, ""));
        System.out.println();

        // ---------------------------------------------------------
        // 3. 補足: Switch 式で網羅性をさらに強制する
        // ---------------------------------------------------------
        System.out.println("=== 3. 補足: Enum + switch 式で優先度を明示する ===");
        System.out.println();
        System.out.println("  resolveEffectiveSetting() をさらに発展させ、");
        System.out.println("  「どの設定が使われたか」を Enum で返すパターン:");
        System.out.println();

        CompanySetting co   = new CompanySetting(true, "企業設定値");
        UserSetting    user = new UserSetting(true, "ユーザー設定値");

        // [Java 7 不可] switch 式は Java 14 以降。Java 7 では switch 文を使う
        SettingPriority priority = determinePriority(co, user);
        String result = switch (priority) {
            case USER    -> user.value();
            case COMPANY -> co.value();
            case NONE    -> "(デフォルト値)";
            // ← case を1つでも書き忘れると コンパイルエラーになる（Java 14+ の switch 式の恩恵）
        };
        System.out.printf("  優先度: %s → 設定値: %s%n", priority, result);
        System.out.println();

        // ---------------------------------------------------------
        // 4. まとめ
        // ---------------------------------------------------------
        System.out.println("=== 4. まとめ ===");
        System.out.println();
        System.out.printf("  %-30s  %s%n", "手法", "効果");
        System.out.println("  " + "-".repeat(65));
        System.out.printf("  %-30s  %s%n", "インライン if-else",
            "変数の初期化忘れに気づきにくい");
        System.out.printf("  %-30s  %s%n", "メソッド化（Optional 返し）",
            "未処理ケースが発生しない。空の可能性を型で表現");
        System.out.printf("  %-30s  %s%n", "Enum + switch 式（Java 14+）",
            "選択肢の網羅性をコンパイル時に強制できる");
        System.out.println();
        System.out.println("  → 「値がセットされないケース」は複雑な条件分岐で発生しやすい。");
        System.out.println("    メソッド化することで戻り値の型が固定され、");
        System.out.println("    コンパイラが未処理ケースを検出してくれる。");
    }
}
