# 第09章：アルゴリズムとソート

> この章は**基礎応用レベル**です。第08章まで終えた方を対象としています。

---

## この章の問い（第08章・第07章から持ち越した疑問）

第07章・第08章を通じて、次のような疑問が生まれませんでしたか？

1. **`ArrayList.contains()` が O(n) で遅い理由はわかった—では「ソート」の速さはどう決まるのか？**
2. **Stream API の `filter().map()` の内部では何が行われているのか？**
3. **100万件のデータから特定の値を探すとき、`contains()` より速い方法はないか？**

**この章でこの3つの問いにすべて答えます。**

---

## 計算量（O 記法）とは

計算量はデータ量 n が増えたとき「処理時間がどう増えるか」を表す指標だ。

```mermaid
graph LR
    A["O(1)\nどんなデータ量でも一定\nHashSet.contains()"] --> B["O(log n)\n2倍になるたびに1回増える\n二分探索"]
    B --> C["O(n)\nデータ量に比例\nList.contains()"]
    C --> D["O(n log n)\nほぼ線形\nArrays.sort() / マージソート"]
    D --> E["O(n²)\nデータ量の二乗\nバブルソート / 選択ソート"]
```

| 計算量 | 10件 | 1,000件 | 100,000件 | 代表例 |
| --- | --- | --- | --- | --- |
| O(1) | 1 | 1 | 1 | `HashSet.contains()` |
| O(log n) | 3 | 10 | 17 | 二分探索 |
| O(n) | 10 | 1,000 | 100,000 | `ArrayList.contains()` |
| O(n log n) | 33 | 10,000 | 1,700,000 | `Arrays.sort()` |
| O(n²) | 100 | 1,000,000 | 10,000,000,000 | バブルソート |

---

## 学習の流れ

| ファイル | テーマ | 体験できる Why |
| --- | --- | --- |
| `SortingAlgorithms.java` | 4種のソートの実装と計測 | O(n²) と O(n log n) の差が1000倍になる理由 |
| `BinarySearch.java` | 二分探索の実装と計測 | ソート済みであれば100万件でも20回で見つかる理由 |
| `StandardLibrarySort.java` | 標準ライブラリの使い方 | なぜソートを自分で実装してはいけないのか |

---

## 各節の説明

### 1. SortingAlgorithms.java — 4種のソートを実装して速度を計測する

**[アンチパターン]** O(n²) のアルゴリズムを10万件以上のデータに使うと、処理が数十秒かかることを実測で確認する。

**実装する4種のソート:**

| アルゴリズム | 計算量 | 特徴 |
| --- | --- | --- |
| バブルソート | O(n²) | 隣同士を比較して交換。最もシンプルだが最も遅い |
| 選択ソート | O(n²) | 未整列部分から最小値を探して先頭と交換。バブルより交換回数が少ない |
| マージソート | O(n log n) | 分割統治法。安定ソート。大量データでも使える |
| `Arrays.sort()` | O(n log n) | TimSort（現場で使う唯一の選択肢） |

**10万件での実測結果（目安）:**

```text
バブルソート: 約 21,000 ms（21 秒）
選択ソート  : 約  6,000 ms（6 秒）
マージソート: 約     18 ms
→ マージソートはバブルソートの約 1,000 倍速い
```

```bash
javac -d out/ src/main/java/com/example/algorithms/SortingAlgorithms.java
java -cp out/ com.example.algorithms.SortingAlgorithms
```

> ※ 10万件のバブルソート・選択ソートの計測には数十秒かかります。

---

### 2. BinarySearch.java — 二分探索で O(log n) を体験する

**Before（線形探索 O(n)）:**

先頭から1件ずつ調べる。ソートされていなくても使えるが、件数に比例して遅くなる。

**After（二分探索 O(log n)）:**

「配列がソート済み」であることが前提。中央の値と比較して探索範囲を毎回半分に絞る。

```text
100万件のデータから1つの値を探す比較回数:
  線形探索:  最大 1,000,000 回
  二分探索:  最大        20 回（log₂(1,000,000) ≈ 20）
```

探索の絞り込み過程をデバッグ出力で可視化する:

```text
探索範囲: [0, 99999]  中央値: 49999
探索範囲: [50000, 99999]  中央値: 74999
探索範囲: [75000, 99999]  中央値: 87499
...（毎回範囲が半分になる）
```

**注意: 未ソートの配列に二分探索を使うと誤った結果になる。** `Arrays.sort()` でソートしてから `Arrays.binarySearch()` を使うのが現場の流儀だ。

```bash
javac -d out/ src/main/java/com/example/algorithms/BinarySearch.java
java -cp out/ com.example.algorithms.BinarySearch
```

---

### 3. StandardLibrarySort.java — 標準ライブラリを使いこなす

**第07章 ComparatorDemo との接続:** `Comparator` を使ったカスタムソートは第07章で学んだ。本章では `Arrays.sort()` / `Collections.sort()` / `Arrays.binarySearch()` の実際の使い方を確認する。

**ソートの使い分け:**

```java
// プリミティブ配列
int[] numbers = {5, 3, 8, 1};
Arrays.sort(numbers);                          // 昇順: [1, 3, 5, 8]
Arrays.sort(numbers, 1, 3);                    // 部分ソート（index 1〜2のみ）

// リスト
ArrayList<String> names = new ArrayList<>();
Collections.sort(names);                       // 辞書順
names.sort(null);                              // 同上（等価）

// [Java 7 不可] Comparator.comparing() は Java 8 以降
// カスタムオブジェクト: 価格昇順 → カテゴリ順の複合ソート
products.sort(Comparator.comparing(Product::getCategory)
                        .thenComparing(Product::getPrice));
```

**`Arrays.binarySearch()` の使い方:**

```java
int[] sorted = {1, 3, 5, 8, 10};
int idx = Arrays.binarySearch(sorted, 5); // 返り値: 2（インデックス）
// ※ 必ずソート後に使うこと。未ソートに使うと結果は不定。
```

```bash
javac -d out/ src/main/java/com/example/algorithms/StandardLibrarySort.java
java -cp out/ com.example.algorithms.StandardLibrarySort
```

> **[Java 7 との違い]** ラムダ式・`Comparator.comparing()`・メソッド参照は Java 8 以降の機能です。`List.of()` は Java 9 以降です。Java 7 では匿名クラスを使います。`Arrays.sort()` 自体は Java 7 でも使えます。

---

## まとめて実行する

```bash
# 全ファイルをまとめてコンパイルする
javac -d out/ src/main/java/com/example/algorithms/*.java

# 各ファイルを順番に実行する（SortingAlgorithms は数十秒かかる）
java -cp out/ com.example.algorithms.SortingAlgorithms
java -cp out/ com.example.algorithms.BinarySearch
java -cp out/ com.example.algorithms.StandardLibrarySort
```

---

## 第09章のまとめ

* **ソートの計算量:** バブル/選択ソートは O(n²)、マージソート/Arrays.sort() は O(n log n)。10万件で約1000倍の速度差になる。
* **自前実装禁止:** ソートは `Arrays.sort()` / `list.sort()` に任せる。TimSort は現実のデータに最適化されており、自前実装が勝てる理由はない。
* **二分探索:** ソート済みであれば O(log n) で探索できる。100万件でも最大20回の比較で見つかる。ただしソートが前提条件。
* **データ構造との接続:** 第07章の `HashSet`（O(1)）、`ArrayList`（O(n)）、`TreeMap`（O(log n)）の速度差は、内部で使われているアルゴリズムの違いによるものだ。

---

## 確認してみよう

1. `SortingAlgorithms.java` のデータ件数を 1,000 件に変えて実行してみましょう。
   10万件のときと比べて各ソートの処理時間の比率はどう変わりましたか？O(n²) と O(n log n) の違いを計算で確かめましょう。

2. `BinarySearch.java` のデバッグ出力を読んで、対象値を見つけるまでに何回「探索範囲が半分になったか」を数えてみましょう。
   `log₂(100,000)` と一致するか確認しましょう。

3. `BinarySearch.java` の未ソート配列（`unsorted`）に `binarySearch()` を呼ぶと、なぜ誤った結果が返るのかをコードを追いながら説明してみましょう。

4. `StandardLibrarySort.java` に新しい `Product`（例: `"スマホスタンド", 2_000, "PC周辺機器"`）を追加して、カテゴリ → 価格の複合ソート結果を確認しましょう。

5. バブルソートの外側のループを「最後に交換が発生しなかったら終了する」という最適化（早期終了）に改善してみましょう。
   既にソート済みの配列を渡したときに比較回数がどう変わるかを確認しましょう。

---

## 次章（第10章）への問いかけ

第09章を終えて、次のような疑問を持ちましたか？

* `System.nanoTime()` で計測したが、ファイルの読み書きはなぜプログラムのコードより圧倒的に遅いのか？
* テキストファイルをJavaで読み書きするにはどうすればよいか？
* 「HTTP リクエストを送る」処理はソートのような同期的な処理と何が違うのか？

**これらの答えは第10章「I/O と Web の基礎」で学びます。**

---

| [← 第08章: モダンAPIと堅牢なコーディング](../modern_api/README.md) | [全章目次](../../../../../../README.md) | 第10章: I/OとWebの基礎（準備中） |
| :--- | :---: | ---: |
