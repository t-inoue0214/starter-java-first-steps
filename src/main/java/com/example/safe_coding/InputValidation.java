/**
 * 【なぜこのコードを学ぶのか】
 * 外部から来るデータ（ユーザー入力・APIレスポンス・ファイル）は壊れていることがある。
 * 境界でバリデーションして「拒否またはデフォルト値」に変換すれば、
 * 内部コードは常に安全な値を扱える。
 * また「引数には必要最小限のデータを渡す」ことで、
 * メソッドの責務が明確になり、テストも書きやすくなる。
 */
package com.example.safe_coding;

public class InputValidation {

    // ---------------------------------------------------------
    // 住所クラス（Before で使う）
    // ---------------------------------------------------------

    private static class Address {
        private final String city;
        private final String zipCode;

        Address(String city, String zipCode) {
            this.city    = city;
            this.zipCode = zipCode;
        }

        public String getCity()    { return city; }
        public String getZipCode() { return zipCode; }
    }

    // ---------------------------------------------------------
    // ユーザークラス（Before で使う）
    // ---------------------------------------------------------

    private static class User {
        private final String  userId;
        private final Address address;
        private final int     creditLimit;

        User(String userId, Address address, int creditLimit) {
            this.userId      = userId;
            this.address     = address;
            this.creditLimit = creditLimit;
        }

        public String  getUserId()     { return userId; }
        public Address getAddress()    { return address; }
        public int     getCreditLimit() { return creditLimit; }
    }

    // ---------------------------------------------------------
    // ========== Before: 引数に User 全体を渡す（知りすぎ問題）==========
    // ---------------------------------------------------------

    /**
     * 注文を処理する Bad メソッド。
     * 引数が User オブジェクト全体のため、
     * 「このメソッドが User の何を使うのか」がシグネチャからわからない。
     * テストのために User・Address・creditLimit をすべて組み立てる必要がある。
     */
    private static void processOrderBad(User user, int quantity) {
        // User の内部を深く掘り下げて値を取り出している（デメテルの法則違反）
        String city        = user.getAddress().getCity(); // Address オブジェクト経由
        int    creditLimit = user.getCreditLimit();

        System.out.printf("  [Before] city=%s, creditLimit=%d, quantity=%d で注文処理%n",
                city, creditLimit, quantity);
    }

    // ---------------------------------------------------------
    // ========== After: 必要な値だけを引数として受け取る ==========
    // ---------------------------------------------------------

    /**
     * 注文を処理する Good メソッド。
     * シグネチャを見れば「city と creditLimit と quantity を使う」ことが一目でわかる。
     * テストは String と int を渡すだけで書ける。
     */
    private static void processOrderGood(String city, int creditLimit, int quantity) {
        System.out.printf("  [After]  city=%s, creditLimit=%d, quantity=%d で注文処理%n",
                city, creditLimit, quantity);
    }

    // ---------------------------------------------------------
    // 「拒否」パターン: 不正値は境界で IllegalArgumentException をスロー
    // ---------------------------------------------------------

    /**
     * 外部入力（文字列）を年齢として解析するバリデーションメソッド。
     * 不正な値は IllegalArgumentException でブロックし、
     * 呼び出し元（内部コード）には正常な int 値だけが届く。
     *
     * @param input 外部から受け取った文字列（null や空文字も来うる）
     * @return 検証済みの年齢（0 以上 150 以下）
     * @throws IllegalArgumentException 不正な入力の場合
     */
    private static int parseAge(String input) {
        // ① null または空文字は拒否
        // [Java 7 不可] String.isBlank() は Java 11 以降。Java 7 では input.trim().isEmpty() を使う
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("年齢は必須です（null または空文字は不可）");
        }

        // ② 数値として解析できなければ拒否
        int age;
        try {
            age = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("年齢は数値で入力してください: " + input);
        }

        // ③ 現実的な範囲でなければ拒否
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("年齢は 0〜150 の範囲で入力してください: " + age);
        }

        // ④ ここまで通れば正常値
        return age;
    }

    // ---------------------------------------------------------
    // 「デフォルト値」パターン: 不正値はデフォルト値に変換して返す
    // ---------------------------------------------------------

    /**
     * 設定ファイルの文字列値をタイムアウト（ミリ秒）として解析するメソッド。
     * null・空・非数値の場合はデフォルト値を返す（例外は投げない）。
     * 設定値は「なくても動く」場合に適した「デフォルト値」パターン。
     *
     * @param configValue    設定ファイルから読み取った文字列（null 可）
     * @param defaultTimeout 設定値が不正だった場合に使うデフォルト値
     * @return 解析したタイムアウト値、または defaultTimeout
     */
    private static int getTimeoutOrDefault(String configValue, int defaultTimeout) {
        // [Java 7 不可] String.isBlank() は Java 11 以降。Java 7 では trim().isEmpty() を使う
        if (configValue == null || configValue.isBlank()) {
            return defaultTimeout;
        }

        try {
            return Integer.parseInt(configValue.trim());
        } catch (NumberFormatException e) {
            // 数値でない設定値はデフォルト値で上書き（プログラムを止めない）
            System.out.println("    ※ タイムアウト設定が不正のためデフォルト値を使用: " + configValue);
            return defaultTimeout;
        }
    }

    public static void main(String[] args) {

        Address address = new Address("東京", "100-0001");
        User    user    = new User("USER-001", address, 50000);

        // address.getZipCode() を使って、住所情報を確認する（getZipCode() の使用例）
        System.out.println("  対象ユーザー: userId=" + user.getUserId()
                + ", 住所=" + address.getCity()
                + "（〒" + address.getZipCode() + "）"
                + ", 与信上限=" + user.getCreditLimit());
        System.out.println();

        // ---------------------------------------------------------
        // セクション1: 引数に必要最小限を渡す
        // ---------------------------------------------------------
        System.out.println("=== 1. Before: 引数に User 全体を渡す（知りすぎ問題）===");
        System.out.println();
        System.out.println("  メソッドシグネチャ: processOrderBad(User user, int quantity)");
        System.out.println("  → このシグネチャだけでは User の何を使うかわからない");
        System.out.println("  → テストするためには User・Address オブジェクトをすべて組み立てる必要がある");
        System.out.println("  → User・Address オブジェクトがフレームワークのクラスならテストがさらに面倒になる");
        System.out.println();
        processOrderBad(user, 3);

        System.out.println();
        System.out.println("=== 2. After: 必要な値だけを引数として受け取る ===");
        System.out.println();
        System.out.println("  メソッドシグネチャ: processOrderGood(String city, int creditLimit, int quantity)");
        System.out.println("  → シグネチャから「city と creditLimit と quantity を使う」と一目でわかる");
        System.out.println("  → テストは processOrderGood(\"東京\", 50000, 3) と書くだけ");
        System.out.println();
        processOrderGood(user.getAddress().getCity(), user.getCreditLimit(), 3);

        // ---------------------------------------------------------
        // セクション2: バリデーションは境界で1回だけ
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 3. 外部入力は境界でバリデーションして内部では信頼する ===");
        System.out.println();

        System.out.println("  ── 「拒否」パターン: parseAge() ──");
        System.out.println();

        // 正常値
        try {
            int age = parseAge("25");
            System.out.println("  parseAge(\"25\")   → " + age + " （正常）");
        } catch (IllegalArgumentException e) {
            System.out.println("  parseAge(\"25\")   → 例外: " + e.getMessage());
        }

        // null
        try {
            int age = parseAge(null);
            System.out.println("  parseAge(null)  → " + age);
        } catch (IllegalArgumentException e) {
            System.out.println("  parseAge(null)  → 拒否: " + e.getMessage());
        }

        // 文字列
        try {
            int age = parseAge("二十五");
            System.out.println("  parseAge(\"二十五\") → " + age);
        } catch (IllegalArgumentException e) {
            System.out.println("  parseAge(\"二十五\") → 拒否: " + e.getMessage());
        }

        // 範囲外
        try {
            int age = parseAge("200");
            System.out.println("  parseAge(\"200\")  → " + age);
        } catch (IllegalArgumentException e) {
            System.out.println("  parseAge(\"200\")  → 拒否: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  ── 「デフォルト値」パターン: getTimeoutOrDefault() ──");
        System.out.println();

        System.out.println("  getTimeoutOrDefault(\"3000\", 5000)    → "
                + getTimeoutOrDefault("3000", 5000));
        System.out.println("  getTimeoutOrDefault(null, 5000)       → "
                + getTimeoutOrDefault(null, 5000));
        System.out.println("  getTimeoutOrDefault(\"abc\", 5000)    → "
                + getTimeoutOrDefault("abc", 5000));
        System.out.println("  getTimeoutOrDefault(\"  \", 5000)     → "
                + getTimeoutOrDefault("  ", 5000));

        // ---------------------------------------------------------
        // セクション3: まとめ
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 4. まとめ ===");
        System.out.println();
        System.out.println("  引数の最小化:");
        System.out.println("    必要な値だけを渡す → 責務が明確・テストしやすい");
        System.out.println("    ハンドル処理なら最小化が難しいことが多いが、ドメインロジックなら最小化できることが多い");
        System.out.println();
        System.out.println("  「拒否」パターン:");
        System.out.println("    不正値は即 IllegalArgumentException → 内部コードに絶対に入れない");
        System.out.println("    「なにがあっても内部では正常な値が保証されている」状態を作る");
        System.out.println();
        System.out.println("  「デフォルト値」パターン:");
        System.out.println("    null や不正値はデフォルト値に変換して内部に渡す");
        System.out.println("    設定値のように「なくても動く」場合に適している");
        System.out.println();
        System.out.println("  バリデーションは境界で1回だけ:");
        System.out.println("    入口（境界）でチェックすれば、内部では常に正常値として扱える");
        System.out.println("    内部の各メソッドでチェックを繰り返すと「どこが責任を持つか」が曖昧になる");
    }
}
