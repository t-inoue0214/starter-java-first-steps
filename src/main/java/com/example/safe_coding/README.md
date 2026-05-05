# 第14章：安全なコーディング

> **この章から対象レベルが変わります。** 第14章からは「設計」レベルに入る。
>
> この章では「なぜその書き方が危険なのか」を体験で学ぶ。
> 各章のコードを書いてきた今、「動く」ことと「安全に動く」ことは別の話だとわかってきたはずだ。
> static の使い方・データの不変性・型の使い方・外部入力の扱い・クラスの責務——
> これらを間違えると本番で顧客情報が漏洩したり、バグが再現しなかったりする。
> この章でよくある罠を体験し、回避するテクニックを習得しよう。
>
> コードの動作を確認するだけでなく、「なぜそう設計するのか」「どこで判断が分かれるのか」というトレードオフを考えながら読むことが重要だ。

## この章の問い

1. **static フィールドを使ったクラスで Web リクエストを処理すると、なぜ顧客情報が混入するのか？**
2. **setter があるクラスはなぜ「いつ・どこで・誰が変えたか」が追跡しにくいのか？**
3. **`Map<String, Object>` にデータを詰め込むと、なぜバグが実行時まで気づけないのか？**
4. **外部から来たデータを「境界」でバリデーションするとなぜ内部コードが単純になるのか？**
5. **複雑な条件分岐をメソッドに切り出すと、なぜ「値がセットされないケース」を防げるのか？**
6. **「似ているから」でクラスを共有すると、なぜ無関係な変更が影響し合うのか？**

**この章でこれら6つの問いにすべて答える。**

## 学習の流れ

| ファイル | テーマ | 体験できる Why |
| :--- | :--- | :--- |
| `StaticPitfalls.java` | static の本番トラブル | なぜ static フィールドに状態を持たせると複数スレッドで情報が混入するのか |
| `ImmutableDesign.java` | イミュータブルな設計 | なぜ setter を持つクラスは変更の追跡が困難になるのか |
| `TypeSafety.java` | 型で意図を表す | なぜ String や Map に頼るとバグが実行時まで発覚しないのか |
| `InputValidation.java` | 外部入力の境界防衛 | なぜ境界でバリデーションを行うと内部コードが常に正常値として扱えるのか |
| `ComplexConditions.java` | 複雑条件はメソッド化する | なぜ条件分岐をメソッドに切り出すと未処理ケースを防げるのか |
| `ClassResponsibility.java` | クラスの責務を守る | なぜ「似ているから」でクラスを共有すると無関係な変更が波及するのか |

---

## 1. StaticPitfalls.java — static の本番トラブル

### Before: static フィールドにリクエストデータを格納する

```java
static class OrderHandlerBad {
    // JVM に1つだけ存在する。複数スレッドが同時に書き換える
    static String currentUserId;
    static String currentOrderId;

    static void handleRequest(String userId, String orderId) throws InterruptedException {
        currentUserId  = userId;   // スレッドAがセット
        currentOrderId = orderId;
        Thread.sleep(50);          // この間にスレッドBが上書きする
        // ここで読む値は「別スレッドに書き換えられた後の値」かもしれない
        System.out.printf("userId=%s の注文 %s を処理完了%n", currentUserId, currentOrderId);
    }
}
```

### After: インスタンスフィールドで保持する

```java
static class OrderHandlerGood {
    private final String userId;
    private final String orderId;

    OrderHandlerGood(String userId, String orderId) {
        this.userId  = userId;
        this.orderId = orderId;
    }

    void handleRequest() throws InterruptedException {
        Thread.sleep(50); // 待機しても、自分のフィールドは誰にも上書きされない
        System.out.printf("userId=%s の注文 %s を処理完了%n", userId, orderId);
    }
}
```

static フィールドは JVM に「1つだけ」存在する。Web サーバーは複数スレッドで同時にリクエストを処理するため、スレッドAがセットした値をスレッドBが上書きし、別ユーザーのデータが混入する。インスタンスフィールドにすれば、各リクエストが独自の状態を持つため、他スレッドの影響を受けない。`static final` の定数や引数のみを使うユーティリティメソッドは問題ないが、**リクエストデータを static フィールドに格納することは本番の情報漏洩に直結する**。

---

## 2. ImmutableDesign.java — イミュータブルな設計

### Before: setter があるクラス、コレクションをそのまま返す getter

```java
static class MutableUserProfile {
    private String       name;
    private List<String> roles;

    public void setName(String name) { this.name = name; } // どこからでも変更できる

    public List<String> getRoles() { return roles; } // 内部リストをそのまま返す（危険）
}
```

### After: `final` フィールドと `List.copyOf()` で防衛的コピー

```java
static final class ImmutableUserProfile {
    private final String       name;
    private final List<String> roles;

    ImmutableUserProfile(String name, List<String> roles) {
        this.name  = name;
        this.roles = List.copyOf(roles); // 独立したコピーを作成
    }

    public List<String> getRoles() {
        return List.copyOf(roles); // 呼び出し元が変更しようとすると例外になる
    }
}
```

Record を使えばさらに簡潔に表現できる。

```java
// [Java 7 不可] Record は Java 16 以降
record UserProfileRecord(String name, int age, List<String> roles) {
    UserProfileRecord {
        roles = List.copyOf(roles); // コンパクトコンストラクタで防衛的コピー
    }
}
```

> **[Java 7 との違い]** `List.copyOf()` は Java 10 以降の機能だ。Java 7 では `Collections.unmodifiableList(new ArrayList<>(roles))` を使う。`Record` は Java 16 以降の機能だ。Java 7 では通常クラス（コンストラクタ + getter）で書く。

イミュータブルにすると「誰が変えたか」の追跡が不要になり、スレッド安全性も自動的に得られる。第15章（Onion Architecture）でも `record Product` を使ったが、この章ではその設計判断の根拠を掘り下げる。

---

## 3. TypeSafety.java — 型で意図を表す

### Before: String のタイポ / `Map<String, Object>` にデータを詰め込む

```java
// タイポが実行時まで気づけない
String status = "ACTIVE";
if (status.equals("ACITVE")) { ... } // ← タイポ！コンパイラは検出しない

// Map は「何が何型で入っているか」がコードからわからない
Map<String, Object> context = new HashMap<>();
context.put("orderId", "ORDER-001");
String orderId = (String) context.get("orderid"); // ← タイポで null が返る
```

### After: `enum OrderStatus` / 専用 Record クラス

```java
// [Java 7 動作差異] Enum は Java 5 以降だが、switch 式の -> は Java 14 以降
private enum OrderStatus { PENDING, ACTIVE, CANCELLED }

// OrderStatus.ACITVE → コンパイルエラー（即座に検出できる）
OrderStatus orderStatus = OrderStatus.ACTIVE;
String description = switch (orderStatus) {
    case PENDING   -> "保留中";
    case ACTIVE    -> "処理中";
    case CANCELLED -> "キャンセル済み";
};

// [Java 7 不可] Record は Java 16 以降
record OrderContext(String orderId, String userId, int quantity) {}

OrderContext order = new OrderContext("ORDER-001", "USER-001", 5);
order.orderid(); // ← コンパイルエラー（orderId() が正しい）
```

> **[Java 7 との違い]** switch 式の `->` 構文は Java 14 以降だ。Java 7 では従来の switch 文（`case:`, `break;`）を使う。`Record` は Java 16 以降だ。Java 7 では通常クラスで書く。

コンパイル時にバグを検出できる設計は、本番で初めてバグが顕在化するリスクを排除する。`Map<String, Object>` はデータの詰め合わせには便利だが、キーのタイポや型のミスが実行時まで発覚しない。Enum と専用クラスはコンパイラをバグ検出の「自動テスト」として活用する方法だ。

---

## 4. InputValidation.java — 外部入力の境界防衛

### Before: `processOrder(User user)` と User 全体を渡す

```java
// メソッドシグネチャから「User の何を使うか」がわからない
// テストに User・Address をすべて組み立てる必要がある
private static void processOrderBad(User user, int quantity) {
    String city        = user.getAddress().getCity(); // 深く掘り下げている
    int    creditLimit = user.getCreditLimit();
    // 処理...
}
```

### After: 必要な値だけを引数として渡す

```java
// シグネチャから「city と creditLimit と quantity を使う」とわかる
// テストは processOrderGood("東京", 50000, 3) と書くだけ
private static void processOrderGood(String city, int creditLimit, int quantity) {
    // 処理...
}
```

「拒否」パターン（不正値を即座にブロック）と「デフォルト値」パターン（不正値を安全な値に変換）のコード例を示す。

```java
// 拒否パターン: 不正値は IllegalArgumentException でブロック
private static int parseAge(String input) {
    // [Java 7 不可] String.isBlank() は Java 11 以降。Java 7 では trim().isEmpty() を使う
    if (input == null || input.isBlank()) {
        throw new IllegalArgumentException("年齢は必須です");
    }
    int age = Integer.parseInt(input.trim()); // NumberFormatException は呼び出し元に伝播
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("年齢は 0〜150 の範囲で入力してください: " + age);
    }
    return age;
}

// デフォルト値パターン: 不正値はデフォルト値に変換して返す
private static int getTimeoutOrDefault(String configValue, int defaultTimeout) {
    if (configValue == null || configValue.isBlank()) { return defaultTimeout; }
    try {
        return Integer.parseInt(configValue.trim());
    } catch (NumberFormatException e) {
        return defaultTimeout; // 設定値が不正でもプログラムを止めない
    }
}
```

境界で1回バリデーションすれば、内部コードは「常に正常値が来る」という前提で書ける。内部の各メソッドで繰り返しチェックすると「どこが責任を持つか」が曖昧になり、チェック漏れが生じやすくなる。

---

## 5. ComplexConditions.java — 複雑条件はメソッド化する

### Before: 未初期化変数 + ハンドラ内 if-else チェーン

```java
private static void handleRequestBad(String requestId, CompanySetting co, UserSetting user) {
    String result; // 未初期化

    if (user.enabled() && user.value() != null && !user.value().isBlank()) {
        result = user.value();
    } else if (co.enabled() && co.value() != null && !co.value().isBlank()) {
        result = co.value();
    } else {
        // 両方が無効なときのデフォルト処理を「うっかり書き忘れる」ことがある
        result = "(設定なし)"; // これを忘れるとコンパイルエラーまたは実行時エラー
    }
}
```

### After: `Optional<String>` を返すメソッドに切り出す

```java
// [Java 7 不可] Optional は Java 8 以降
private static Optional<String> resolveEffectiveSetting(CompanySetting co, UserSetting user) {
    if (user.enabled() && user.value() != null && !user.value().isBlank()) {
        return Optional.of(user.value());
    }
    if (co.enabled() && co.value() != null && !co.value().isBlank()) {
        return Optional.of(co.value());
    }
    return Optional.empty(); // 「値なし」を型で明示
}

private static void handleRequestGood(String requestId, CompanySetting co, UserSetting user) {
    Optional<String> setting = resolveEffectiveSetting(co, user);
    // 呼び出し側は Optional を受け取るため「値がない可能性」を無視できない
    setting.ifPresentOrElse(
        value -> System.out.printf("設定値: %s%n", value),
        ()    -> System.out.printf("設定値: (デフォルト値を使用)%n")
    );
}
```

> **[Java 7 との違い]** `Optional` は Java 8 以降の機能だ。Java 7 では `null` チェックを使う。`ifPresentOrElse()` は Java 9 以降だ。Java 7 では `if (setting.isPresent()) { ... } else { ... }` と書く。

メソッドに切り出すと戻り値の型が固定され、未処理ケースがコンパイルで検出されやすくなる。インライン if-else では変数が未初期化のまま使われるリスクが残るが、メソッドは必ず値を返すことが保証される。

---

## 6. ClassResponsibility.java — クラスの責務を守る

### Before: `OrderData` と `CartData` が `ProductContainerBad` を継承

```java
// 「どちらも商品リストを持つ」という理由でまとめた親クラス
private static class ProductContainerBad {
    protected List<String> items;
    // ...
}

private static class OrderDataBad extends ProductContainerBad { /* 注文固有の処理 */ }
private static class CartDataBad  extends ProductContainerBad { /* カート固有の処理 */ }
// 問題: 注文の items を List<OrderItem> に変えるとカートにも影響する
```

### After: 独立したクラス、共通の振る舞いは `ItemCountable` インターフェース

```java
private interface ItemCountable {
    int getItemCount(); // 共通の振る舞いだけをインターフェースで定義
}

private static class OrderData implements ItemCountable {
    private final String orderId;
    private final List<String> items;
    @Override public int getItemCount() { return items.size(); }
}

private static class CartData implements ItemCountable {
    private final String sessionId;
    private final List<String> items;
    @Override public int getItemCount() { return items.size(); }
}
// 改善: 注文の内部構造を変えてもカートには影響しない
```

DRY 原則（Don't Repeat Yourself）の本質は「**知識の重複を避ける**」ことであり、「コードの見た目の重複を避けること」とは区別する必要がある。`OrderData` と `CartData` の価格フォーマットメソッドが今同じコードでも、変わる理由が異なれば別々に持つべきだ。「似ているから」でクラスを共有すると、無関係な変更が波及し、修正コストが増大する。

---

## まとめてコンパイル・実行する

```bash
javac -d out/ $(find src/main/java/com/example/safe_coding -name "*.java")
java -cp out/ com.example.safe_coding.StaticPitfalls
java -cp out/ com.example.safe_coding.ImmutableDesign
java -cp out/ com.example.safe_coding.TypeSafety
java -cp out/ com.example.safe_coding.InputValidation
java -cp out/ com.example.safe_coding.ComplexConditions
java -cp out/ com.example.safe_coding.ClassResponsibility
```

---

## 第14章のまとめ

- **static フィールドの危険性**: static フィールドは JVM に1つしか存在しない。リクエスト処理クラスで使うと複数スレッドがデータを上書きし合い、顧客情報の混入（情報漏洩）が起きる。
- **イミュータブル設計**: `final` フィールドと防衛的コピーにより、作成後に状態が変わらないクラスを設計できる。「誰が変えたか」の追跡が不要になり、スレッド安全性も自動的に得られる。
- **型安全性**: `String` の代わりに `Enum` を、`Map<String, Object>` の代わりに専用クラスを使うことで、コンパイル時にバグを検出できる。実行時まで気づけないバグは本番での障害に直結する。
- **入力バリデーション**: 外部入力は「境界」で1回バリデーションし、拒否するかデフォルト値に変換する。内部コードは常に正常値として扱えるようになり、責務が明確になる。
- **複雑条件のメソッド化**: 複雑な条件分岐はメソッドに切り出す。戻り値の型が固定され、未処理ケースをコンパイラが検出しやすくなる。`Optional` で「値がない可能性」を型で表現すると、呼び出し側がその可能性を無視できなくなる。
- **クラスの責務**: 「似ているから」でクラスを共有すると、変わる理由の異なる変更が互いに影響し合う。DRY 原則は「知識の重複を避けること」であり、「コードの重複を避けること」とは区別する必要がある。

---

## 確認してみよう

1. `StaticPitfalls.java` を実行して Before のセクションで `userId` の表示が混入するケースを確認してみよう。混入が起きないケースと起きるケースの違いを説明してみよう。
2. `ImmutableDesign.java` の `MutableUserProfile` に setter を追加した場合と削除した場合で、テストを書く難易度がどう変わるか考えてみよう。
3. `TypeSafety.java` で `OrderStatus` に新しい値（例: `SHIPPED`）を追加し、switch 式のケースを1つ削除したときにコンパイルエラーが出ることを確認してみよう。
4. `InputValidation.java` の `parseAge()` に新しいバリデーション条件（例: 成人のみ許可）を追加してみよう。境界以外の内部コードを変更せずに対応できることを確認してみよう。
5. `ComplexConditions.java` の `handleRequestBad()` で `result = "(設定なし)"` の行を削除したときのコンパイラの動作を確認してみよう。同様の変更を `resolveEffectiveSetting()` に対して行うとどうなるか比較してみよう。
6. `ClassResponsibility.java` の `OrderData` と `CartData` に共通する処理をユーティリティクラスにまとめた場合と、それぞれのクラスに個別のメソッドを持たせた場合で、「将来の変更のしやすさ」がどう違うか考えてみよう。

---

| [← 第13章: HTTPクライアントと外部API連携](../http_client/README.md) | [全章目次](../../../../../../README.md) | [第15章: 設計とアーキテクチャ →](../architecture/README.md) |
| :--- | :---: | ---: |
