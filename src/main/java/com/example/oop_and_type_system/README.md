# 第06章：OOP・型システム

> この章から対象レベルが「超入門」から「基礎応用」へ切り替わります。

超入門編では「書いたら動いた」という体験を積みました。
基礎応用編からは「なぜその書き方をするのか」を自分で説明できることを目標にします。
コードの量よりも、「この設計の何が問題で、どう解決したか」を言葉にする練習をしてください。

第01〜05章では「動くコードを書けること」を目標にしてきました。  
第06章からは「**なぜ現場ではその書き方をするのか**」を問い続けます。

---

## この章の問い

第05章の末尾では、以下の3つの問いかけを残しました。この章で全部答えます。

- `ArrayList` を `for` 文でループするより、**ラムダ式** を使う書き方があるのはなぜか？  
  → [3. LambdaBasics.java](#3-lambdabasicsjava) で答える
- 定数を `String` で管理するのではなく、**Enum（列挙型）** を使うのはなぜか？  
  → [1. EnumBasics.java](#1-enumbasicsjava) で答える
- `Object` 型でなんでも受け取れるのに、なぜ **ジェネリクス（`<T>`）** が必要なのか？  
  → [2. GenericsBasics.java](#2-genericsbasicsjava) で答える

---

## プログラミングパラダイムの地図

Java は1つの言語で複数の書き方（パラダイム）を使い分けられる。この章はその転換点となる。

| パラダイム | 何を中心に考えるか | Java での書き方 | 主に扱う章 |
| --- | --- | --- | --- |
| 手続き型 | 処理の手順を順番に書く | `for` / `if`（第01〜03章） | 第01〜03章 |
| オブジェクト指向 | データと振る舞いをひとまとめにする | クラス・インターフェース（第04〜06章） | 第04〜06章 |
| 関数型 | データの変換を副作用なく表現する | ラムダ式・Stream API（第06〜08章） | 第06〜08章 |

「どれが正解か」ではなく「状況に応じて使い分ける」のが現場のスタイルだ。

---

## 学習の流れ

| # | ファイル | テーマ | Before で体験する問題 |
| --- | --- | --- | --- |
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

> 「不明な状態」と「コンパイルエラー」の違いを確認したら、次は `GenericsBasics.java` で同じ「型安全」の考え方がどう使われるか見てみましょう。

---

## 2. GenericsBasics.java

**Before で体験する問題:**  
`Object` 型の箱に `Integer` を入れて `String` として取り出そうとすると、  
コンパイルは通るが実行時に `ClassCastException` が発生する。

**After で得られる解決:**  
`Box<String>` と宣言すると、`Integer` を入れようとした瞬間、コンパイルエラーが発生する—実行よりも早い段階で問題が見つかる。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/GenericsBasics.java
java -cp out/ com.example.oop_and_type_system.GenericsBasics
```

> 「実行時エラー」を「コンパイル時エラー」に変える感覚をつかんだら、次は `LambdaBasics.java` で「振る舞いを変数に入れる」という新しい発想を体験しましょう。

---

## 3. LambdaBasics.java

同じ処理を4段階で書き換えながら、ラムダ式がなぜ登場したかを体験する。

| 段階 | 書き方 |
| --- | --- |
| Step1 | 通常の `for` ループ |
| Step2 | 匿名クラスを使った `forEach` |
| Step3 | ラムダ式 `name -> System.out.println(name)` |
| Step4 | メソッド参照 `System.out::println` |

「偶数だけ表示する」処理を `for+if` 版とラムダ版で並べて対比する。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/LambdaBasics.java
java -cp out/ com.example.oop_and_type_system.LambdaBasics
```

> **[Java 7 との違い]** ラムダ式・メソッド参照・`List.of()`・Stream API はすべて Java 8 以降の機能です。`List.forEach()` も Java 8 以降のため、匿名クラスを渡す書き方であっても Java 7 では動きません。Java 7 では拡張 `for` ループで代替します。

Step1→Step4 の変化を確認したら、次は `FunctionalInterfaces.java` でJavaが用意している標準の「振る舞いの型」を学びましょう。

---

## 4. FunctionalInterfaces.java

4つの標準関数型インターフェースを、それぞれの呼び出しメソッドとともに体験する。

| インターフェース | 役割 | 呼び出しメソッド |
| --- | --- | --- |
| `Predicate<T>` | 条件判定（`boolean` を返す） | `test()` |
| `Function<T, R>` | 変換（T を受け取り R を返す） | `apply()` |
| `Consumer<T>` | 消費・副作用（戻り値なし） | `accept()` |
| `Supplier<T>` | 生成（引数なしで値を返す） | `get()` |

自作の `@FunctionalInterface` (`Greeter`) を定義し、ラムダで実装する例も示す。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/FunctionalInterfaces.java
java -cp out/ com.example.oop_and_type_system.FunctionalInterfaces
```

> 4種のインターフェースを動かしたら、次は `HigherOrderFunctions.java` で「振る舞いを引数に渡す」ことで重複コードがどう消えるかを体験しましょう。

---

## 5. HigherOrderFunctions.java

**Before で体験する問題:**  
`filterByContainsA()` と `filterByLengthAtLeast4()` はループ構造が同じメソッドとして量産される。

**After で得られる解決:**  
`filter(List<T> list, Predicate<T> condition)` という1本の高階関数にまとめ、  
条件だけをラムダで差し込む設計にする。

末尾では「この考え方が Stream API の `.filter().map().forEach()` の正体だ」と明記している。

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/HigherOrderFunctions.java
java -cp out/ com.example.oop_and_type_system.HigherOrderFunctions
```

> 「振る舞いを引数で渡す」感覚をつかんだら、次は `InterfaceAndPolymorphism.java` でその考え方がクラス設計にどう応用されるか見てみましょう。

---

## 6. InterfaceAndPolymorphism.java

**Before で体験する問題:**  
`processPayment(String type, int amount)` は新しい支払い方法が増えるたびに `else if` を追加する必要がある。

**After で得られる解決:**  
`interface Payment { void pay(int amount); }` を定義し、呼び出し元は `Payment` 型のリストをループするだけ。  
`ConveniencePayment` を追加するとき、呼び出し元のコードは一切変更しない。

| キーワード | 意味 |
| --- | --- |
| `extends` | 1つのクラスからしか継承できない（単一継承） |
| `implements` | 複数のインターフェースを同時に実装できる |

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/InterfaceAndPolymorphism.java
java -cp out/ com.example.oop_and_type_system.InterfaceAndPolymorphism
```

> 新しい支払い方法を追加したとき呼び出し元が無変更だった理由を言葉で説明できたら、最後の `StaticAndAnnotation.java` でよくあるミス（static 乱用）を体験しましょう。

---

## 7. StaticAndAnnotation.java

**Before で体験する問題:**  
`static` なカウント変数を持つ `AppUtilBefore` は、呼び出し順序によって結果が変わる（状態の混入）。

**After で得られる解決:**  
インスタンスに状態を切り出すと、`utilA` と `utilB` が独立したカウントを持てる。  
純粋な計算（`MathHelper.max()`）は `static` のままで問題ない—境界線を知ることが重要。

`@Override` と `@FunctionalInterface` の2つのアノテーションを実際に体験する:

| アノテーション | 付けると嬉しいこと |
| --- | --- |
| `@Override` | 誤字でメソッド名を間違えるとコンパイルエラーになる |
| `@FunctionalInterface` | 抽象メソッドを2つ書くとコンパイルエラーになる |

```bash
javac -d out/ src/main/java/com/example/oop_and_type_system/StaticAndAnnotation.java
java -cp out/ com.example.oop_and_type_system.StaticAndAnnotation
```

> 全7ファイルを終えたら、下の「まとめと次章への問いかけ」を読んで第07章へ進んでください。

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

## 確認してみよう

1. `EnumBasics.java` の `switch` 式から `default ->` の行をコメントアウトして実行してみましょう。
   コンパイルがどうなるかを確かめよう。`default` があることで「どんな安全性」が生まれているかを言葉で説明してみましょう。

2. `GenericsBasics.java` の `Box<String>` を `Box<Integer>` に変えた場合、エラーはどのタイミングで発生しますか？
   「実行時エラー」と「コンパイルエラー」のどちらになるかを実際に試して確かめましょう。

3. `LambdaBasics.java` の Step3（ラムダ式）を Step2（匿名クラス）の書き方に書き直してみましょう。
   コード量がどれだけ増えるかを確認して、ラムダ式が生まれた理由を実感しましょう。

4. `Predicate`・`Function`・`Consumer`・`Supplier` の4種を「引数の数と戻り値の有無」で整理してみましょう。
   それぞれの呼び出しメソッド（`test`・`apply`・`accept`・`get`）も一緒に書き出してみましょう。

5. `StaticAndAnnotation.java` の `@Override` を `@Overide`（スペルミス）に変えてコンパイルするとどうなりますか？
   確認したら元に戻しておきましょう。

6. `InterfaceAndPolymorphism.java` に新しい支払い方法 `ConveniencePayment`（コンビニ払い）を追加してみましょう。
   このとき `processPaymentBefore()` と `payments` リストのどちらを変更しますか？変更しなくていい理由も答えましょう。

---

### 次は第07章へ：データ構造を使いこなす

第07章では、コレクションの「中身」を深堀りします。以下の問いに向き合いましょう:

- `HashMap` に入れた要素は、なぜ入れた順番どおりに出てくるとは限らないのか？
- `Comparator` を使うとき、ラムダ式で書けることに気づいたか？これは第06章で学んだどの概念と同じか？
- 要素数が数百万件の `List` に対して `contains()` を何度も呼ぶと遅い—なぜか？

---

| [← 第05章: 便利な道具箱とミニゲーム](../practical_java/README.md) | [全章目次](../../../../../../README.md) | [第07章: データ構造を使いこなす →](../collections_deep/README.md) |
| :--- | :---: | ---: |
