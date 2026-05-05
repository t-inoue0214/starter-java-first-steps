# 第6章: OOP・型システム

> この章から対象レベルが「超入門」から「基礎応用」へ切り替わります。

第01〜05章では「動くコードを書けること」を目標にしてきました。  
第06章からは「**なぜ現場ではその書き方をするのか**」を問い続けます。

---

## この章の問い

第05章の末尾では、以下の3つの問いかけを残しました。この章で全部答えます。

- `ArrayList` を `for` 文でループするより、**ラムダ式** を使う書き方があるのはなぜ？  
  → [3. LambdaBasics.java](#3-lambdabasicsjava) で答えます
- 定数を `String` で管理するのではなく、**Enum（列挙型）** を使うのはなぜ？  
  → [1. EnumBasics.java](#1-enumbasicsjava) で答えます
- `Object` 型でなんでも受け取れるのに、なぜ **ジェネリクス（`<T>`）** が必要なの？  
  → [2. GenericsBasics.java](#2-genericsbasicsjava) で答えます

---

## 学習の流れ

| # | ファイル | テーマ | Before で体験する問題 |
|---|---|---|---|
| 1 | `EnumBasics.java` | Enum（列挙型） | 誤字があってもコンパイルが通るString定数管理 |
| 2 | `GenericsBasics.java` | ジェネリクス | 実行時にClassCastExceptionが発生するObject型の箱 |
| 3 | `LambdaBasics.java` | ラムダ式 | 同じ構造のforループを毎回書く冗長なコード |
| 4 | `FunctionalInterfaces.java` | 関数型インターフェース | Predicate/Function/Consumer/Supplierの4種を体験 |
| 5 | `HigherOrderFunctions.java` | 高階関数 | 条件ごとに別メソッドを量産する設計の限界 |
| 6 | `InterfaceAndPolymorphism.java` | インターフェース・ポリモーフィズム | 支払い方法が増えるたびにif文が増え続ける設計 |
| 7 | `StaticAndAnnotation.java` | static・アノテーション | 状態を持つstaticメソッドが呼び出し順で結果が変わる |

---

## 1. EnumBasics.java

**Before で体験する問題:**  
`"ORERED"` という誤字をしてもコンパイルが通り、実行時に「不明な状態」として処理される。

**After で得られる解決:**  
`enum OrderStatus { ORDERED, ... }` に切り替えると、存在しない定数はコンパイルエラーになる。  
さらに `switch` 式（`->` 構文）と組み合わせると、網羅性もコンパイラが保証する。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/EnumBasics.java
java -cp out/ com.example.oop_and_type_system.EnumBasics
```

---

## 2. GenericsBasics.java

**Before で体験する問題:**  
`Object` 型の箱に `Integer` を入れて `String` として取り出そうとすると、  
コンパイルは通るが実行時に `ClassCastException` が発生する。

**After で得られる解決:**  
`Box<String>` と宣言すると `Integer` を入れようとした瞬間にコンパイルエラーになる—問題の発見が早くなる。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/GenericsBasics.java
java -cp out/ com.example.oop_and_type_system.GenericsBasics
```

---

## 3. LambdaBasics.java

同じ処理を4段階で書き換えながら、ラムダ式がなぜ登場したかを体験する。

| 段階 | 書き方 |
|---|---|
| Step1 | 通常の `for` ループ |
| Step2 | 匿名クラスを使った `forEach` |
| Step3 | ラムダ式 `name -> System.out.println(name)` |
| Step4 | メソッド参照 `System.out::println` |

「偶数だけ表示する」処理を `for+if` 版とラムダ版で並べて対比する。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/LambdaBasics.java
java -cp out/ com.example.oop_and_type_system.LambdaBasics
```

---

## 4. FunctionalInterfaces.java

4つの標準関数型インターフェースを、それぞれの呼び出しメソッドとともに体験する。

| インターフェース | 役割 | 呼び出しメソッド |
|---|---|---|
| `Predicate<T>` | 条件判定（`boolean` を返す） | `test()` |
| `Function<T, R>` | 変換（T を受け取り R を返す） | `apply()` |
| `Consumer<T>` | 消費・副作用（戻り値なし） | `accept()` |
| `Supplier<T>` | 生成（引数なしで値を返す） | `get()` |

自作の `@FunctionalInterface` (`Greeter`) を定義し、ラムダで実装する例も示す。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/FunctionalInterfaces.java
java -cp out/ com.example.oop_and_type_system.FunctionalInterfaces
```

---

## 5. HigherOrderFunctions.java

**Before で体験する問題:**  
`filterByContainsA()` と `filterByLengthAtLeast4()` というループ構造が同じメソッドが量産される。

**After で得られる解決:**  
`filter(List<T> list, Predicate<T> condition)` という1本の高階関数にまとめ、  
条件だけをラムダで差し込む設計にする。

末尾では「この考え方が Stream API の `.filter().map().forEach()` の正体だ」と明記している。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/HigherOrderFunctions.java
java -cp out/ com.example.oop_and_type_system.HigherOrderFunctions
```

---

## 6. InterfaceAndPolymorphism.java

**Before で体験する問題:**  
`processPayment(String type, int amount)` は新しい支払い方法が増えるたびに `else if` を追加する必要がある。

**After で得られる解決:**  
`interface Payment { void pay(int amount); }` を定義し、呼び出し元は `Payment` 型のリストをループするだけ。  
`ConveniencePayment` を追加するとき、呼び出し元のコードは一切変更しない。

| キーワード | 意味 |
|---|---|
| `extends` | 1つのクラスからしか継承できない（単一継承） |
| `implements` | 複数のインターフェースを同時に実装できる |

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/InterfaceAndPolymorphism.java
java -cp out/ com.example.oop_and_type_system.InterfaceAndPolymorphism
```

---

## 7. StaticAndAnnotation.java

**Before で体験する問題:**  
`static` なカウント変数を持つ `AppUtilBefore` は、呼び出し順序によって結果が変わる（状態の混入）。

**After で得られる解決:**  
インスタンスに状態を切り出すと、`utilA` と `utilB` が独立したカウントを持てる。  
純粋な計算（`MathHelper.max()`）は `static` のままで問題ない—境界線を知ることが重要。

`@Override` と `@FunctionalInterface` の2つのアノテーションを実際に体験する:

| アノテーション | 付けると嬉しいこと |
|---|---|
| `@Override` | 誤字でメソッド名を間違えるとコンパイルエラーになる |
| `@FunctionalInterface` | 抽象メソッドを2つ書くとコンパイルエラーになる |

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/StaticAndAnnotation.java
java -cp out/ com.example.oop_and_type_system.StaticAndAnnotation
```

---

## まとめてコンパイル・実行する場合

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/*.java
java -cp out/ com.example.oop_and_type_system.EnumBasics
java -cp out/ com.example.oop_and_type_system.GenericsBasics
java -cp out/ com.example.oop_and_type_system.LambdaBasics
java -cp out/ com.example.oop_and_type_system.FunctionalInterfaces
java -cp out/ com.example.oop_and_type_system.HigherOrderFunctions
java -cp out/ com.example.oop_and_type_system.InterfaceAndPolymorphism
java -cp out/ com.example.oop_and_type_system.StaticAndAnnotation
```

---

## 第06章のまとめと次章への問いかけ

この章で学んだこと:

- **Enum**: 文字列定数の誤字バグをコンパイル時に防ぐ
- **ジェネリクス**: 実行時キャストエラーをコンパイル時エラーに変える
- **ラムダ式・メソッド参照**: 振る舞いを値として扱う
- **関数型インターフェース**: Predicate/Function/Consumer/Supplier という4つのパターン
- **高階関数**: 振る舞いを引数として受け取ることで重複コードをなくす
- **インターフェース・ポリモーフィズム**: 呼び出し元を変えずに振る舞いを差し替える
- **staticとアノテーション**: staticが適切な場所と不適切な場所の境界線

---

### 次は第07章へ：データ構造を使いこなす

**[→ 第07章: データ構造を使いこなす](../collections_deep/)**

第07章では、コレクションの「中身」を深堀りします。以下の問いに向き合いましょう:

- `HashMap` に入れた要素は、なぜ入れた順番どおりに出てくるとは限らないのか？
- `Comparator` を使うとき、ラムダ式で書けることに気づいたか？これは第06章で学んだどの概念と同じか？
- 要素数が数百万件の `List` に対して `contains()` を何度も呼ぶと遅い—なぜか？
