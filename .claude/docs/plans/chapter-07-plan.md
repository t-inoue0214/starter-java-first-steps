# 第07章 実装計画: collections_deep（データ構造を使いこなす）

## 概要

第06章でジェネリクス・ラムダ式・関数型インターフェース・高階関数を習得した学習者が、
それらの知識をコレクション操作に応用しながら「なぜそのデータ構造を選ぶのか」を体験する章。
パフォーマンス計測（`System.nanoTime()`）による裏付けと、`Comparator` を介したラムダ式の応用を通じて、
第06章で学んだ抽象概念を実践的なコードとして定着させる。

---

## 前章からの接続 / 次章への橋渡し

### 第06章から受け取ること

- `Comparator<T>` が関数型インターフェースであることを知っている
  - `list.sort(comparator)` が高階関数であることの理解が本章 `ComparatorDemo.java` の出発点になる
- ラムダ式で「振る舞いを引数として渡す」書き方を体験している
  - `forEach((k, v) -> ...)` の記法が自然に読める状態になっている
- `ArrayList<String>` の `<String>` がジェネリクスの型パラメータであることを理解している
  - 本章の `ListVsSet.java`・`LruCache.java` のジェネリクス記法がスムーズに読める

### 第08章へ渡すこと

- `for` ループでコレクションを走査する書き方の冗長さに改めて気づかせる
  - `ListVsSet.java`・`MapIterationPerf.java` での繰り返し処理が「もっと短く書けないか」という動機付けになり、第08章の Stream API へつながる
- `HashMap.get()` が `null` を返す問題（第05章で残した疑問）を再び意識させる
  - `LruCache.java` の `userCache.get(userId)` が `null` を返す可能性の確認箇所を第08章 `Optional` への橋渡しとして明示する
- コレクション操作のパフォーマンス差を体験済みとすることで、Stream API がどのような内部処理をしているかを問う動機が生まれる

---

## 章の順番入れ替え検討

### 現状の順序: 第06章（OOP・型システム）→ 第07章（collections_deep）→ 第08章（Modern API）

### 結論: 現状の順序を維持する

理由は以下のとおり。

**「第06章が先」の根拠（強い）:**

1. `ComparatorDemo.java` は「`Comparator` が関数型インターフェースである」理解を前提に設計されている。ラムダ式を知らない状態では `list.sort((a, b) -> a.getPrice() - b.getPrice())` の意味が伝わらない。
2. `MapIterationPerf.java` の `map.forEach((k, v) -> ...)` はラムダ式の知識が必要。
3. `LruCache.java` の `SimpleLruCache<K, V>` はジェネリクスの型パラメータを読める前提で書かれている。
4. 第06章 `chapter-06-plan.md` に「第07章へ渡すこと」として「`Comparator` をラムダ式で書けることを知っているため、第07章のソート操作で応用できる」と明記されており、設計の一貫性がある。

**「第07章が先の方がよい」ケースがないか（弱い）:**

- 「ArrayList を使ったことで HashSet の価値を知りたくなる」という動機は第05章でも醸成できる。第06章の前に第07章を置く必然性はない。
- 「コレクション操作の不便さ」が第06章の学習動機になる、という筋書きは成立しない。第06章の学習動機（`ArrayList<String>` の `<>` 疑問、ラムダへの動機）は第05章で十分に仕込まれている。

**第08章（Modern API）との重複・分担の整理:**

| テーマ | 第07章（collections_deep） | 第08章（modern_api） |
| --- | --- | --- |
| Stream API | 扱わない（動機付けの言及のみ） | 本格導入（`filter`・`map`・`collect`・`reduce`） |
| `Optional` | 扱わない（`null` の問題を残す） | 本格導入 |
| `Comparator` | ラムダ式の応用として扱う | 扱わない |
| パフォーマンス計測 | `System.nanoTime()` で実測 | 扱わない |
| LRU キャッシュ | 実装パターンとして扱う | 扱わない |

---

## 現状ファイルの評価

### 実装済みファイル（6本）

| ファイル | 評価 | 主な問題点 |
| --- | --- | --- |
| `ArrayLimitation.java` | 概ね良好 | `var` 使用（コーディング規約違反）・Java 7 注釈なし |
| `ListVsSet.java` | 概ね良好 | `var` 使用・Java 7 注釈なし・`List.of()` 注釈なし |
| `MapComparison.java` | 概ね良好 | `var` 使用・Java 7 注釈なし・ラムダ式注釈なし |
| `ComparatorDemo.java` | 概ね良好 | `var` 使用・Java 7 注釈なし・`List.of()` 注釈なし |
| `MapIterationPerf.java` | 概ね良好 | `var` 使用・Java 7 注釈なし・ラムダ式注釈なし |
| `LruCache.java` | 概ね良好 | `var` 使用・switch 式注釈なし |

### README.md の評価

- 章の問い3つ（なぜ HashMap は順序不定か・Comparator とラムダの関係・contains() の遅さ）が冒頭に配置されており構成は適切。
- 各節の Before/After 対比と実行コマンドが揃っている。
- ナビゲーションバーの右セル（次章リンク）は「準備中」のテキストのみでリンクなし。第08章の作成後に更新する。
- `var` の使用がコーディング規約違反（`architecture.md` のルール「`var` はサンプルコードで使わない」に違反）。README 内のコードブロックに `var entry` が含まれており、修正が必要。

## Java SE 7 互換性の考慮

| 本章で使う Java 8+ の機能 | 導入バージョン | Java 7 での代替手段 | 注釈対象ファイル |
| --- | --- | --- | --- |
| ラムダ式 `(a, b) -> ...` | Java 8 | 匿名クラス（`new Comparator<Product>() { ... }`）で代替 | `ComparatorDemo.java`・`MapComparison.java`・`MapIterationPerf.java` |
| メソッド参照 `Product::getName` | Java 8 | ラムダ式または匿名クラスで代替 | `ComparatorDemo.java` |
| `Comparator.comparing()` | Java 8 | 匿名クラスで代替 | `ComparatorDemo.java` |
| `Map.forEach((k, v) -> ...)` | Java 8 | `for (Map.Entry<K, V> e : map.entrySet())` で代替 | `MapComparison.java`・`MapIterationPerf.java` |
| `List.forEach(e -> ...)` | Java 8 | 拡張 `for` ループで代替 | `ComparatorDemo.java` |
| `List.of()` | Java 9 | `Arrays.asList()` または `Collections.unmodifiableList()` で代替 | `ListVsSet.java`・`ComparatorDemo.java` |
| `switch` 式（`->` 構文） | Java 14 | 従来の `switch` 文で代替 | `LruCache.java`（`fetchFromDatabase` メソッド） |

### `.java` ファイルへの注釈追加（現状未記載の箇所一覧）

```java
// [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では匿名クラスで書く:
//   list.sort(new Comparator<Product>() {
//       @Override public int compare(Product a, Product b) { return a.getPrice() - b.getPrice(); }
//   });
list.sort((a, b) -> a.getPrice() - b.getPrice());  // ComparatorDemo.java

// [Java 7 不可] Comparator.comparing() は Java 8 以降。Java 7 では匿名クラスで書く:
//   list.sort(new Comparator<Product>() {
//       @Override public int compare(Product a, Product b) { return a.getName().compareTo(b.getName()); }
//   });
list.sort(Comparator.comparing(Product::getName));  // ComparatorDemo.java

// [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
//   List<String> withDuplicates = Arrays.asList("A", "B", "A", "C", "B", "D");
List<String> withDuplicates = List.of("A", "B", "A", "C", "B", "D");  // ListVsSet.java

// [Java 7 不可] Map.forEach() のラムダ版は Java 8 以降。Java 7 では entrySet ループで書く:
//   for (Map.Entry<String, String> entry : map.entrySet()) {
//       System.out.print(entry.getKey() + " ");
//   }
map.forEach((key, value) -> System.out.print(key + " "));  // MapComparison.java

// [Java 7 不可] switch 式（-> 構文）は Java 14 以降。Java 7 では switch 文で書く:
//   switch (userId) {
//       case 101: return "田中 太郎";
//       ...
//   }
return switch (userId) { ... };  // LruCache.java
```

### README.md への Java 7 注記（追加すべき箇所）

各節の末尾または説明箇所に以下の形式で追加する。

```markdown
> **[Java 7 との違い]** `Comparator.comparing()` およびラムダ式での `list.sort()` は Java 8 以降の機能です。
> Java 7 では匿名クラスを使います。
> ```java
> // Java 7 での書き方（匿名クラス）
> list.sort(new Comparator<Product>() {
>     @Override public int compare(Product a, Product b) {
>         return a.getName().compareTo(b.getName());
>     }
> });
> ```
```

---

## アンチパターン（最低1つ以上含めること）

### アンチパターン1（掲載済み・継続）: HashMap に設定値を入れる誤用

`MapComparison.java` の「現場でよくある誤用」セクション。順序が必要な場面で `HashMap` を使うと処理順が毎回変わる。`LinkedHashMap` に変えるだけで解決することを対比して示す。既に実装済みのため変更不要。

### アンチパターン2（掲載済み・継続）: ArrayList の大量 contains

`ArrayLimitation.java` の注意点セクション。10万件の `ArrayList.contains()` が遅いことを実測で確認する。既に実装済みのため変更不要。

### アンチパターン3（追加推奨・優先度: 中）: keySet でループして get する誤用

`MapIterationPerf.java` の Before セクションで扱われているが、README に「アンチパターン」として明示的にラベリングされていない。README の MapIterationPerf 節の説明に以下を追記する。

```markdown
**[アンチパターン]** `keySet()` でループして `get(key)` する書き方は、ハッシュ計算が2回走り非効率です。
値も取得するなら `entrySet()` または `forEach()` を使います。
```

### アンチパターン4（追加推奨・優先度: 低）: HashSet の順序を前提にしたコード

`HashSet` の `toString()` 出力順序を前提にしたコードは、実行環境・JVM バージョンによって動作が変わりうる。`ListVsSet.java` の HashSet の表示部分にコメントで注意喚起する。

```java
// 注意: HashSet の表示順は毎回変わりうる—順序に依存するコードを書かないこと
// [アンチパターン] if (hashSet.toString().startsWith("[バナナ")) { ... } のような順序依存のコードはNG
System.out.println("HashSet の中身: " + hashSet);
```

---

## 実行コマンド

### 個別実行

```bash
# ArrayLimitation: 配列の限界と ArrayList の柔軟性を体験する
javac -d out/ src/main/java/com/example/collections_deep/ArrayLimitation.java
java -cp out/ com.example.collections_deep.ArrayLimitation

# ListVsSet: contains() の速度差を実測する
javac -d out/ src/main/java/com/example/collections_deep/ListVsSet.java
java -cp out/ com.example.collections_deep.ListVsSet

# MapComparison: Map 3種の順序の違いを可視化する
javac -d out/ src/main/java/com/example/collections_deep/MapComparison.java
java -cp out/ com.example.collections_deep.MapComparison

# ComparatorDemo: Comparator をラムダで渡す（第06章の応用）
javac -d out/ src/main/java/com/example/collections_deep/ComparatorDemo.java
java -cp out/ com.example.collections_deep.ComparatorDemo

# MapIterationPerf: keySet vs entrySet の速度差を実測する
javac -d out/ src/main/java/com/example/collections_deep/MapIterationPerf.java
java -cp out/ com.example.collections_deep.MapIterationPerf

# LruCache: LinkedHashMap で LRU キャッシュを作る（SimpleLruCache を含む）
javac -d out/ src/main/java/com/example/collections_deep/LruCache.java
java -cp out/ com.example.collections_deep.LruCache
```

### まとめてコンパイル・個別実行

```bash
# 全ファイルをまとめてコンパイルする
javac -d out/ src/main/java/com/example/collections_deep/*.java

# 各ファイルを順番に実行する
java -cp out/ com.example.collections_deep.ArrayLimitation
java -cp out/ com.example.collections_deep.ListVsSet
java -cp out/ com.example.collections_deep.MapComparison
java -cp out/ com.example.collections_deep.ComparatorDemo
java -cp out/ com.example.collections_deep.MapIterationPerf
java -cp out/ com.example.collections_deep.LruCache
```

---

## 修正の優先順位まとめ

| 優先度 | 対象ファイル | 修正内容 |
| --- | --- | --- |
| 高 | 全 `.java` ファイル | `var` を型明示に修正（6ファイル・複数箇所） |
| 高 | 全 `.java` ファイル | Java 7 互換性注釈の追加（ラムダ式・`List.of()`・`switch` 式・`Comparator.comparing()`) |
| 高 | `README.md` | コードブロック内の `var entry` を型明示に修正 |
| 高 | `README.md` | 各節に `[Java 7 との違い]` 注釈ブロックを追加 |
| 中 | `LruCache.java` | `userCache.get(userId)` の `null` 箇所に第08章 `Optional` への橋渡しコメントを追加 |
| 中 | `ComparatorDemo.java` | `Product` クラスのメソッドのアクセス修飾子に `public` を追加 |
| 中 | `README.md` | アンチパターン3（keySet ループ）のラベリングを追加 |
| 低 | `README.md` | ナビゲーションバーの右セル（第08章リンク）を更新（第08章実装後） |
| 低 | `ListVsSet.java` | HashSet 順序依存アンチパターン4のコメントを追加 |

---

## 注意点

### 学習者への配慮

- 本章は「基礎応用」レベルであり、第06章のジェネリクス・ラムダ式・高階関数を習得した直後に読む想定。`Comparator` のラムダ応用が「難しい」と感じる学習者がいる場合は、第06章の `FunctionalInterfaces.java`（特に `Comparator` が関数型インターフェースである旨の記述）に戻るよう促す注記を `ComparatorDemo.java` の Why ヘッダーに加えることを検討する（優先度: 低）。
- `LruCache.java` の `SimpleLruCache` は `LinkedHashMap` の継承という高度な手法を使う。「この実装パターンを自分で書けるようになることが目標ではなく、標準ライブラリの活用方法を知ることが目標だ」という旨を Why ヘッダーと README の説明文に明記すること。現状 Why ヘッダーに「車輪の再発明を避けられることを体験する」と書かれており適切だが、より明示的にしてもよい。
- パフォーマンス計測結果（`System.nanoTime()` の値）はマシンスペックや JVM の状態によって毎回異なる。`ListVsSet.java` および `MapIterationPerf.java` の実行結果が「思ったほど差が出なかった」という場合への対処として、README に「JVM のウォームアップ・ガベージコレクション・実行環境によって数値は変わる。重要なのは傾向（O(1) vs O(n)）であり絶対値ではない」という注記を加えること（優先度: 中）。

### 既存コンテンツとの整合性

- `chapter-06-plan.md` の「第07章へ渡すこと」に記載された内容（`Comparator` をラムダ式で書ける、第06章の `HigherOrderFunctions.java` と Stream API の橋渡し）と本章の設計は整合している。
- `collections_deep/README.md` の冒頭「第06章から持ち越した疑問」3問と、`chapter-06-plan.md` の「第07章へ渡すこと」は対応関係にある。この対応を壊さないこと。
- 第08章との分担（Stream API・Optional は第08章。Comparator・パフォーマンス計測は第07章）を維持すること。本章で Stream API の `.filter()` 等を本格使用しない。動機付けの言及（「for ループをもっと短く書く方法が第08章で学べる」）にとどめること。

### コーディング規約との整合性

- `var` の使用がすべての `.java` ファイルに残っている。`architecture.md` のルール（「`var` はサンプルコードで使わない」「`var` の紹介は第02章 README の補足のみ」）に従い、実装時に型を明示した形へ修正する。この修正は学習者が「型が何であるか」を一目で確認できるという教育的効果もある。
- Why ヘッダーコメント（`/** 【なぜこのコードを学ぶのか】 */`）は全 6 ファイルに記載済みであり、この形式を維持する。
- Before/After の対比形式（`// ========== Before: 〜 ==========`）は全ファイルで使われており、この形式を維持する。

### 新規ファイル追加の必要性

現状の6ファイル構成は第07章として適切な範囲をカバーしている。以下の理由から新規ファイルの追加は不要と判断する。

- `ArrayDeque`（両端キュー）は第07章の範囲外。`LinkedList` の `offer()`/`poll()` で FIFO の概念を導入済みであり重複する。
- `PriorityQueue`（優先度付きキュー）はアルゴリズムとの関連が強く、第09章（アルゴリズム）での扱いが適切。
- `ConcurrentHashMap` は第12章（並行処理）の守備範囲。
