# 第06章 実装計画: oop_and_type_system（OOP・型システム）

## 概要

OOP の深化（インターフェース・ポリモーフィズム）と、関数型という新しいパラダイムの入口を同時に体験する章。
第01〜05章で体験してきた「手続き型・OOP」に加えて、「関数型」という第3の視点が登場する転換点となる。
Enum・ジェネリクス・ラムダ式・関数型インターフェース・高階関数・インターフェースによるポリモーフィズム・static の適切な使い方を、必ず「問題先行（Before）→解決策（After）」の対比で体験する。
第07章・第08章での Stream API・Optional の本格活用への基礎固めとなる章であり、3つのパラダイムが混在する Java の現実コードを初めて体験する章として位置づける。

---

## プログラミングパラダイムの地図

第01章からここまでで体験してきた書き方には名前があった。整理すると以下の3種類のパラダイム（考え方の流派）が存在する。

| パラダイム | 何を中心に考えるか | Java での書き方 | 向いている問題 |
| --- | --- | --- | --- |
| 手続き型 | 処理の手順を順番に書く | `for` ループ・`if` 文（第01〜03章） | 単純な順次処理 |
| オブジェクト指向 | データと振る舞いをひとまとめにする | クラス・インターフェース（第04〜06章） | 状態を持つ複雑なモデル |
| 関数型 | データの変換を副作用なく表現する | ラムダ式・Stream API（第06〜08章） | データ加工・並行処理 |

どれが正解かではなく、問題の性質・チームの習熟度・コードの読みやすさによって使い分ける。Java の実際のコードは3つが混在するのが現実であり、本章はその混在を初めて体験する章になる。

---

## 前章からの接続 / 次章への橋渡し

### 第05章から受け取ること

- `ArrayList<String>` の `<String>` に疑問を持ったまま終えている（本章の `GenericsBasics.java` で答える）
- `for` ループでリストを回す書き方の冗長さに違和感がある（本章の `LambdaBasics.java` で答える）
- `HashMap.get()` が `null` を返すことへの疑問がある（本章では深入りせず第08章への橋渡しに留める）
- `String` で定数管理するコードを書いたことで誤字バグの危険を知っている（本章の `EnumBasics.java` で答える）

### 第07章へ渡すこと

- Stream API（`.stream().filter().map().forEach()`）の仕組みは本章の `HigherOrderFunctions.java` と `LambdaBasics.java` で基礎を体験済みとして扱える
- `Comparator` をラムダ式で書けることを知っているため、第07章のソート操作で応用できる
- `interface Payment` による「呼び出し元を変えずに振る舞いを差し替える」設計思想は、第07章以降のコード設計の前提になる

---

## クラス関連用語の導入順序（第06章での応用）

第04章で「クラス・フィールド・コンストラクタ・インスタンス・メソッド」の基礎を習得済みのため、第06章では以下の順で概念を積み上げる。

1. **Enum** — クラスに似た仕組みで「定数の集合」を型安全に管理する（`EnumBasics.java`）
2. **ジェネリクス `<T>`** — クラス定義に「型パラメータ」を加える（`GenericsBasics.java`）
3. **ラムダ式・匿名クラス** — 「振る舞いを値として扱う」新しい視点（`LambdaBasics.java`）
4. **関数型インターフェース** — ラムダ式の「型」として機能するインターフェース（`FunctionalInterfaces.java`）
5. **高階関数** — ラムダ式を引数として受け取るメソッド（`HigherOrderFunctions.java`）
6. **インターフェース・ポリモーフィズム** — `implements` による振る舞いの差し替え（`InterfaceAndPolymorphism.java`）
7. **static の適切な使い方・アノテーション** — 「いつ static を使うか」の境界線（`StaticAndAnnotation.java`）

同時に複数の概念を出さない。7つのファイルを順番に体験する設計が現状の README に明示されており、この順序を維持する。

---

## 必須実装内容

### EnumBasics.java（実装済み）

**体験させること:**

1. Before: `String` 定数でオーダー状態を管理する誤字バグ（`"ORERED"` がコンパイルを通る問題）を体験する。
2. After: `enum OrderStatus { ORDERED, SHIPPED, DELIVERED, CANCELLED }` に切り替えると存在しない定数はコンパイルエラーになる。
3. `switch` 式（`->` 構文）と組み合わせることで網羅性をコンパイラが保証する体験をする。

**現状の問題点と改善候補:**

- `switch` 式の `->` 構文は Java 14 以降であるため、`[Java 7 不可]` 注釈が必要（現状未記載）。
- `enum` 自体は Java 5 以降なので注釈不要だが、`switch` 式との組み合わせには注釈が必要。

### GenericsBasics.java（実装済み）

**体験させること:**

1. Before: `ObjectBox`（`Object` 型の箱）に `Integer` を入れて `String` として取り出すと `ClassCastException` が実行時に発生することを体験する。
2. After: `Box<String>` と宣言すると `Integer` を入れようとした瞬間、コンパイルエラーが発生する（問題発見が早い）。
3. 応用として `<T extends Number>` という境界型パラメータで `Integer` と `Double` を同じ計算メソッドで処理できることを見せる。

**現状の問題点と改善候補:**

- `var` を使用している行が複数あるが、`architecture.md` のコードスタイルルール（「var はサンプルコードで使わない」）に違反している。
  - 違反箇所: `var objectBox = new ObjectBox();`・`var stringBox = new Box<String>();`・`var intBox = new Box<Integer>();`・`var integers = List.of(1, 2, 3, 4, 5);`・`var doubles = List.of(1.1, 2.2, 3.3);`
  - 改善: `ObjectBox objectBox = new ObjectBox();` のように型を明示する。
  - 優先度: 高（コーディング規約違反）
- `List.of()` は Java 9 以降のため `[Java 7 不可]` 注釈が必要（現状未記載）。

### LambdaBasics.java（実装済み）

**体験させること:**

1. Step1: 通常の `for` ループ（古典的な書き方）。
2. Step2: 匿名クラスを使った `forEach`（中間段階・「こういう書き方もある」レベルでよい）。
3. Step3: ラムダ式 `name -> System.out.println(name)` で匿名クラスを短く書いた形を体験する。
4. Step4: メソッド参照 `System.out::println` でさらに短く書く。
5. 対比: 「偶数だけ表示する」処理を `for+if` 版と `stream().filter().forEach()` 版で並べる。
6. ラムダ式の4パターン（引数なし・1つ・2つ以上・複数行）を体験する。

**現状の問題点と改善候補:**

- `var names = List.of(...)` および `var numbers = List.of(...)` で `var` を使用しており、コーディング規約違反。
  - 改善: `List<String> names = List.of(...)` のように型を明示する。
  - 優先度: 高（コーディング規約違反）
- `var sorted = names.stream().sorted(byLength).toList();`・`var greeters = new Greeter[]{...}` も同様に `var` を使用している。
  - 改善: `List<String> sorted = ...`・`Greeter[] greeters = ...` と明示する。
- `List.of()` と `stream().toList()` は Java 9/16 以降のため `[Java 7 不可]` 注釈が必要（現状未記載）。
- ラムダ式・メソッド参照・`forEach` は Java 8 以降のため `[Java 7 不可]` 注釈が必要（現状未記載）。

### FunctionalInterfaces.java（実装済み）

**体験させること:**

1. `Predicate<T>`（`test()` で呼び出し）: 条件判定を変数に代入する体験。`and()` / `or()` / `negate()` で組み合わせる。
2. `Function<T, R>`（`apply()` で呼び出し）: 変換処理を変数に代入する体験。`andThen()` で連鎖する。
3. `Consumer<T>`（`accept()` で呼び出し）: 副作用専用の処理を変数に代入する体験。`andThen()` で連鎖する。
4. `Supplier<T>`（`get()` で呼び出し）: 遅延生成パターンを体験する。
5. 自作 `@FunctionalInterface`（`Greeter`）をラムダ式で実装する体験。

**現状の問題点と改善候補:**

- `var greeters = new Greeter[]{...}` で `var` を使用しており、コーディング規約違反。
  - 改善: `Greeter[] greeters = new Greeter[]{...}` と明示する。
  - 優先度: 高（コーディング規約違反）
- 4種の関数型インターフェース（`Predicate`/`Function`/`Consumer`/`Supplier`）は Java 8 以降のため `[Java 7 不可]` 注釈が必要（現状未記載）。

### HigherOrderFunctions.java（実装済み）

**体験させること:**

1. Before: `filterByContainsA()` と `filterByLengthAtLeast4()` というループ構造の同じメソッドが量産される問題を体験する。
2. After: `filter(List<T> list, Predicate<T> condition)` という1本の高階関数にまとめ、条件だけをラムダで差し込む設計を体験する。
3. 「この考え方が Stream API の `.filter().map().forEach()` の正体だ」という明示的な橋渡しを確認する。

**現状の問題点と改善候補:**

- ファイルの内容は確認済み（README には記載されているが実際のソースの `var` 使用状況は要確認）。
- `Predicate` 利用のため Java 8 以降の注釈が必要か確認が必要。

### InterfaceAndPolymorphism.java（実装済み）

#### 【設計意図】前の3ファイルからの視点切り替え

`LambdaBasics.java`・`FunctionalInterfaces.java`・`HigherOrderFunctions.java` の3ファイルでは「振る舞いをラムダとして引数に渡す」関数型の発想を学んだ。
`InterfaceAndPolymorphism.java` ではここで視点が切り替わり、「振る舞いをクラスとして定義してインターフェースで統一する」OOP の発想を学ぶ。

この切り替わりを学習者が意識できるよう、`InterfaceAndPolymorphism.java` の Why ヘッダー（`/** 【なぜこのコードを学ぶのか】 */`）へ「前の3ファイルとは視点が変わる：ラムダで振る舞いを渡す関数型の発想から、クラスで振る舞いを実装してインターフェースで統一する OOP の発想へ」という旨のコメントを必ず入れること。

**体験させること:**

1. Before: `processPaymentBefore(String type, int amount)` で文字列 `if-else` 分岐を体験する。`"creditcard"` という誤字でも「不明な支払い方法」として実行時に動いてしまうことを確認する。
2. After: `interface Payment { void pay(int amount); }` を定義し、実装クラスを `Payment` 型のリストに入れてループするだけで全種類を処理できることを体験する。
3. `ConveniencePayment` を追加したとき、`processPaymentAfter()` とループを変更せずに動作することを確認する（開閉原則の体験）。
4. `extends`（is-a 関係・単一継承）と `implements`（can-do 関係・多重実装）の使い分けを理解する。

**現状の問題点と改善候補:**

- `for (var payment : paymentMethods)` で `var` を使用しており、コーディング規約違反。
  - 改善: `for (Payment payment : paymentMethods)` と明示する。
  - 優先度: 高（コーディング規約違反）
- `ArrayList` を使っているが `List` インターフェース型の変数で受けることを示すと、インターフェース型で変数宣言する重要性（「実装型でなくインターフェース型で受ける」）が伝わる。ただし本章の学習目標外のため優先度は低。

### StaticAndAnnotation.java（実装済み）

**体験させること:**

1. Before: `static` なカウント変数を持つ `AppUtilBefore` は呼び出し順序によって結果が変わる（状態の混入）を体験する。
2. After: インスタンスに状態を切り出すと `utilA` と `utilB` が独立したカウントを持てることを確認する。
3. `MathHelper` のように純粋な計算（状態を持たない）なら `static` で問題ないという境界線を理解する。
4. `@Override`: 小文字の `tostring()` を書いてもコンパイルが通るバグを体験し、`@Override` 追加でコンパイルエラーになることを確認する。
5. `@FunctionalInterface`: 抽象メソッドを2つ書くとコンパイルエラーになることを確認する。

#### 【設計意図】`MathHelper` の static メソッドを純粋関数の伏線として扱う

`MathHelper` の `static` メソッド（状態を持たない純粋な計算）は、関数型プログラミングにおける「純粋関数（同じ入力には常に同じ出力を返し、副作用を持たない関数）」の概念そのものとなる。

この概念を「静的メソッドの適切な使い方」として第06章で体験させておくことで、以下の章への伏線となる。

- **第08章（Stream API）**: `.map()` 等に渡す関数が副作用を持つべきでない理由が腑に落ちる。
- **第12章（並行処理）**: 副作用のない処理がスレッドセーフである理由を体感できる。

`StaticAndAnnotation.java` の Why ヘッダーおよびコード内コメントに「`MathHelper` の pure function 性は第08章・第12章への伏線である」旨を明記すること。

**現状の問題点と改善候補:**

- `var utilA = new AppUtil();`・`var utilB = new AppUtil();`・`var dogWithout = ...`・`var dogWith = ...` で `var` を使用しており、コーディング規約違反。
  - 改善: `AppUtil utilA = new AppUtil();` 等と明示する。
  - 優先度: 高（コーディング規約違反）

---

## Java SE 7 互換性の考慮

| 本章で使う Java 8+ の機能 | 導入バージョン | Java 7 での代替手段 | 注釈対象ファイル |
| --- | --- | --- | --- |
| ラムダ式 `(a, b) -> ...` | Java 8 | 匿名クラスで代替 | `LambdaBasics.java`・`FunctionalInterfaces.java`・`InterfaceAndPolymorphism.java`・`StaticAndAnnotation.java` |
| メソッド参照 `System.out::println` | Java 8 | ラムダ式または匿名クラスで代替 | `LambdaBasics.java`・`FunctionalInterfaces.java` |
| `Predicate`/`Function`/`Consumer`/`Supplier` | Java 8 | 匿名クラスで代替 | `FunctionalInterfaces.java`・`HigherOrderFunctions.java` |
| `List.forEach()` | Java 8 | 拡張 `for` ループで代替 | `LambdaBasics.java` |
| `Stream API` | Java 8 | 拡張 `for` ループで代替 | `LambdaBasics.java`・`HigherOrderFunctions.java` |
| `List.of()` | Java 9 | `Arrays.asList()` で代替 | `LambdaBasics.java`・`GenericsBasics.java`・`FunctionalInterfaces.java` |
| `stream().toList()` | Java 16 | `stream().collect(Collectors.toList())` で代替 | `LambdaBasics.java` |
| `switch` 式（`->` 構文） | Java 14 | 従来の `switch` 文で代替 | `EnumBasics.java` |
| `default` インターフェースメソッド | Java 8 | 書けない（抽象クラスで代替） | `InterfaceAndPolymorphism.java`（`Predicate.and()` 等が内部で使用） |

### `.java` ファイルへの注釈追加（現状未記載の箇所）

各ファイルの該当箇所に以下の形式で注釈を追加する必要がある。

```java
// [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では匿名クラスで書く:
//   names.forEach(new Consumer<String>() {
//       @Override public void accept(String name) { System.out.println(name); }
//   });
names.forEach(name -> System.out.println(name));
```

### README への Java 7 注記

`LambdaBasics.java` の Step2 説明箇所（匿名クラス）に以下を追加する。

> **[Java 7 との違い]** ラムダ式は Java 8 以降の機能です。Java 7 では Step2 の匿名クラスの書き方しか使えません。

---

## アンチパターン（必ず1つ以上含めること）

### 現状で掲載済みのアンチパターン（各ソースファイル内）

1. `EnumBasics.java`: `"ORERED"` という誤字がコンパイルを通る（Before）
2. `GenericsBasics.java`: `Object` 型への `ClassCastException`（Before）
3. `LambdaBasics.java`: `for+if` の冗長なループ（Step1 Before として示すが「悪い」とは明示しない設計）
4. `HigherOrderFunctions.java`: 同じループ構造のメソッド量産（Before）
5. `InterfaceAndPolymorphism.java`: 文字列 `if-else` 分岐で支払い種別を管理（Before）
6. `StaticAndAnnotation.java`: `static` 乱用による状態混入・`@Override` なしのタイポ（Before）

### 追加推奨のアンチパターン（`var` の文脈で）

`GenericsBasics.java` への追加例（コメントのみ追記）:

```java
// [アンチパターン] var を使うと「この箱が何を入れる箱か」が一目でわからなくなる
// var box = new Box<>(); // ← 何の Box? 型が読めない
// Box<String> box = new Box<>(); // OK: 型が明示されている
```

---

## 実行コマンド

各ファイルは個別に実行できる。

```bash
# 個別実行の例
javac -d out/ src/main/java/com/example/oop_and_type_system/EnumBasics.java
java -cp out/ com.example.oop_and_type_system.EnumBasics

# まとめてコンパイルして個別実行する
javac -d out/ src/main/java/com/example/oop_and_type_system/*.java
java -cp out/ com.example.oop_and_type_system.GenericsBasics
java -cp out/ com.example.oop_and_type_system.LambdaBasics
java -cp out/ com.example.oop_and_type_system.FunctionalInterfaces
java -cp out/ com.example.oop_and_type_system.HigherOrderFunctions
java -cp out/ com.example.oop_and_type_system.InterfaceAndPolymorphism
java -cp out/ com.example.oop_and_type_system.StaticAndAnnotation
```

---

## 注意点

### 学習者への配慮

- 本章から「超入門」→「基礎応用」にレベルが切り替わるため、README 冒頭の「このレベル切り替え」を明示した注記は維持すること（現状の README に記載済み）。
- `HigherOrderFunctions.java` → `InterfaceAndPolymorphism.java` の順序は「振る舞いを引数で渡す」から「振る舞いを実装クラスで差し替える」という自然な発展のため変更しない。
- `StaticAndAnnotation.java` を最後に置く理由: `static` の乱用問題は「第04章で何気なく書いた `static main`」の深堀りとして位置づけられており、他の全ファイルを理解した後で改めて見ると腑に落ちやすい。

### コーディング規約違反の優先修正箇所

複数のファイルで `var` をサンプルコードに使用しているが、`architecture.md` のルール（「`var` はサンプルコードで使わない」）に違反している。以下のファイルで型を明示する修正が必要。

- `GenericsBasics.java`: `var objectBox`・`var stringBox`・`var intBox`・`var integers`・`var doubles`
- `LambdaBasics.java`: `var names`・`var numbers`・`var sorted`・`var greeters`
- `FunctionalInterfaces.java`: `var greeters`
- `InterfaceAndPolymorphism.java`: `for (var payment : ...)`
- `StaticAndAnnotation.java`: `var utilA`・`var utilB`・`var dogWithout`・`var dogWith`

### 既存コンテンツとの整合性

- README のファイル学習順序テーブルは現状のまま維持する（順序変更は学習者の混乱につながる）。
- README の「第06章のまとめと次章への問いかけ」セクションにある第07章への3問は、`collections_deep/README.md` の冒頭で「第06章で残した問い」として参照されている前提で維持する。
- 第04章で「アクセス修飾子・スコープ・`static` vs インスタンス」を学ぶと `architecture.md` に記載されており、`StaticAndAnnotation.java` の `static` 乱用の問題は第04章の知識の上に積み上がる設計として整合している。

### README タイトルの修正

現状 README の先頭が `# 第6章: OOP・型システム` となっており、他章の形式（`# 第06章：クラスとオブジェクト`）と異なる。以下に統一する。

- 修正前: `# 第6章: OOP・型システム`
- 修正後: `# 第06章：OOP・型システム`
