# 第07章：データ構造を使いこなす

> この章は**基礎応用レベル**です。第06章まで終えた方を対象としています。

---

## この章の問い（第06章から持ち越した疑問）

第06章を終えたとき、次のような疑問を持ちませんでしたか？

1. **`HashMap` に入れた要素は、なぜ入れた順番どおりに出てくるとは限らないのか？**
2. **`Comparator` を使うとき、ラムダ式で書けることに気づいたか？これは第06章の高階関数と同じ概念ではないのか？**
3. **要素数が数百万件の `List` に対して `contains()` を何度も呼ぶと遅い—なぜか？**

**この章でこの3つの問いにすべて答えます。**

---

## 学習の流れ

| ファイル | テーマ | 体験できる Why |
| --- | --- | --- |
| `ArrayLimitation.java` | 配列の限界 | なぜ現場では配列より List を使うのか |
| `ListVsSet.java` | List vs Set | なぜ `contains()` には HashSet を使うのか |
| `MapComparison.java` | Map 3種比較 | なぜ順序が必要な場面で HashMap を使ってはいけないのか |
| `ComparatorDemo.java` | Comparator | なぜラムダで比較基準を渡せるのか（第06章の応用） |
| `MapIterationPerf.java` | Map 反復計測 | なぜ `entrySet` の方が `keySet` より速いのか |
| `LruCache.java` | LRU キャッシュ | LinkedHashMap を使いこなすと何ができるのか |

---

## 各節の説明

### 1. ArrayLimitation.java — 配列の限界を体験する

**Before（配列の問題）:**

- `int[] scores = new int[5]` に6個目を追加しようとすると `ArrayIndexOutOfBoundsException` が発生する
- 配列の途中への挿入は「後ろの要素を1つずつ手動でずらす」必要があり、コードが複雑になる

**After（ArrayList で解決）:**

- `add()` / `remove()` / `add(index, value)` が1行で書ける
- ただし `contains()` は O(n)（先頭から全件スキャン）—大量データでは遅い。詳しくは次のファイルへ。

```bash
javac -d out/ src/main/java/com/example/collections_deep/ArrayLimitation.java
java -cp out/ com.example.collections_deep.ArrayLimitation
```

---

### 2. ListVsSet.java — contains() の速度差を実測する

**計測体験:** 10万件のデータに対して `ArrayList.contains()` と `HashSet.contains()` の速度を `System.nanoTime()` で実測して比較する。

| コレクション | contains() の計算量 | 理由 |
| --- | --- | --- |
| ArrayList | O(n) | 先頭から1件ずつ比較する |
| HashSet | O(1) | ハッシュ関数で格納位置を直接計算する |

**LinkedList の使い所:** `offer()` / `poll()` でキュー（FIFO）として使う。

```bash
javac -d out/ src/main/java/com/example/collections_deep/ListVsSet.java
java -cp out/ com.example.collections_deep.ListVsSet
```

> **[Java 7 との違い]** `List.of()` は Java 9 以降の機能です。Java 7 では `Arrays.asList()` を使います。
> `HashSet.contains()` の O(1) 特性は Java 7 でも同じです。

---

### 3. MapComparison.java — Map 3種の「順序」の違いを可視化する

同じデータを3種の Map に入れて `forEach` で表示し、出力の違いを見せる。

| 種類 | 順序 | 速度（追加・検索） | 使いどころ |
| --- | --- | --- | --- |
| `HashMap` | 不定（ハッシュ値で決まる） | O(1) 最速 | 順序不要・とにかく速く検索したい |
| `TreeMap` | キーで自動ソート（赤黒木） | O(log n) | ソート済みで取り出したい・範囲検索をしたい |
| `LinkedHashMap` | 挿入順を保持 | O(1) ほぼ最速 | 設定値・ログ出力など順序が重要な場面 |

**よくあるバグ:** 設定値を `HashMap` に入れると処理順が毎回変わる。`LinkedHashMap` に変えるだけで解決する。

```bash
javac -d out/ src/main/java/com/example/collections_deep/MapComparison.java
java -cp out/ com.example.collections_deep.MapComparison
```

> **[Java 7 との違い]** `Map.forEach((k, v) -> ...)` のラムダ版は Java 8 以降の機能です。
> Java 7 では拡張 `for` ループで代替します。
>
> ```java
> // Java 7 での書き方
> for (Map.Entry<String, String> entry : hashMap.entrySet()) {
>     System.out.print(entry.getKey() + " ");
> }
> ```

---

### 4. ComparatorDemo.java — ラムダで比較基準を外から渡す

**Before（Comparable の問題）:**

`Product implements Comparable<Product>` で `compareTo` を実装すると、ソート基準が「価格順」に固定されてしまう。「名前順でもソートしたい」という要求に応えられない。

**After（Comparator をラムダで渡す）:**

```java
list.sort((a, b) -> a.getPrice() - b.getPrice());                    // 価格昇順
list.sort(Comparator.comparing(Product::getName));                   // 名前順
list.sort(Comparator.comparing(Product::getPrice).reversed());       // 価格降順
list.sort(Comparator.comparing(Product::getCategory)
                    .thenComparing(Product::getPrice));              // カテゴリ→価格の2段階
```

**第06章との接続:** `Comparator` は関数型インターフェース。`list.sort(comparator)` は「比較の振る舞い」をラムダとして受け取る高階関数—第06章の `Predicate` / `Function` と同じ考え方だ。

```bash
javac -d out/ src/main/java/com/example/collections_deep/ComparatorDemo.java
java -cp out/ com.example.collections_deep.ComparatorDemo
```

> **[Java 7 との違い]** ラムダ式・メソッド参照・`Comparator.comparing()` はすべて Java 8 以降の機能です。`List.of()` は Java 9 以降です。
> Java 7 では匿名クラスを使います。
>
> ```java
> // Java 7 での書き方（匿名クラス）
> list.sort(new Comparator<Product>() {
>     @Override
>     public int compare(Product a, Product b) {
>         return a.getName().compareTo(b.getName());
>     }
> });
> ```

---

### 5. MapIterationPerf.java — keySet vs entrySet の速度差を実測する

**[アンチパターン]** `keySet()` でループして `get(key)` する書き方は、ハッシュ計算が2回走り非効率です。値も取得するなら `entrySet()` または `forEach()` を使います。

**Before（よく見るが非効率なコード）:**

```java
for (String key : map.keySet()) {
    String value = map.get(key); // ← キーのハッシュ計算が2回走る
}
```

**After（entrySet で一度に取得）:**

```java
for (Map.Entry<String, String> entry : map.entrySet()) {
    String key   = entry.getKey();   // ハッシュ計算は反復時の1回だけ
    String value = entry.getValue();
}
```

10万件の HashMap で3通りを計測し、`keySet + get` / `entrySet` / `forEach` の速度差を数値で確認する。

```bash
javac -d out/ src/main/java/com/example/collections_deep/MapIterationPerf.java
java -cp out/ com.example.collections_deep.MapIterationPerf
```

> **[Java 7 との違い]** `Map.forEach((k, v) -> ...)` のラムダ版は Java 8 以降の機能です。
> Java 7 では `entrySet()` の拡張 `for` ループで代替します。`entrySet()` 自体は Java 7 でも使えます。

---

### 6. LruCache.java — LinkedHashMap で LRU キャッシュを作る

**LRU（Least Recently Used）とは:** キャッシュが満杯になったとき、最近最も使われていないデータを捨てるポリシー。DBやAPIへのアクセス結果を直近N件だけメモリに保持して応答を高速化する用途で使われる。

`LinkedHashMap` を継承して `removeEldestEntry` をオーバーライドするだけで実現できる。

```java
class SimpleLruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    SimpleLruCache(int capacity) {
        super(capacity + 1, 0.75f, true); // accessOrder=true でアクセス順を保持
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity; // 上限超えたら最も古いエントリを削除
    }
}
```

**動作確認のステップ:**

1. 容量3のキャッシュに A, B, C を追加 → `{A, B, C}`
2. A にアクセス（get）して「最近使った」に更新
3. D を追加 → B（最も古い）が自動で削除されて `{C, A, D}`

```bash
javac -d out/ src/main/java/com/example/collections_deep/LruCache.java
java -cp out/ com.example.collections_deep.LruCache
```

> **[Java 7 との違い]** `fetchFromDatabase` メソッド内の `switch` 式（`->` 構文）は Java 14 以降の機能です。Java 7 では従来の `switch` 文で代替します。
> `LinkedHashMap` 自体と `removeEldestEntry` は Java 7 でも使えます。

---

## まとめて実行する

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

## 第07章のまとめ

この章で答えた3つの問い:

1. **HashMap が順序不定な理由:** キーのハッシュ値で格納位置が決まるため、挿入順とは無関係な順番になる。順序が必要なら `LinkedHashMap`（挿入順）か `TreeMap`（キーのソート順）を選ぶ。

2. **Comparator とラムダ式の関係:** `Comparator<T>` は `compare(T o1, T o2)` という抽象メソッドを1つ持つ関数型インターフェース。`list.sort(comparator)` は「比較の振る舞い」をラムダで受け取る高階関数—第06章の `Predicate` / `Function` と同じ設計思想だ。

3. **List.contains() が遅い理由:** ArrayList は先頭から1件ずつ比較する O(n) 操作。HashSet はハッシュ関数で格納位置を直接計算する O(1) 操作—データ量によらず一定速度。

---

## 確認してみよう

1. `ListVsSet.java` の `dataSize` を `1_000_000`（100万件）に変えて実行してみましょう。
   10万件のときと比べて速度差はどう変わりましたか？O(n) と O(1) の意味と結びつけて考えてみましょう。

2. `HashMap`・`TreeMap`・`LinkedHashMap` の3種を「順序の保証」と「検索速度」の2軸で整理してみましょう。
   「設定ファイルを読み込んで順番どおりに処理する」用途にはどれが適切か、理由とともに答えましょう。

3. `MapIterationPerf.java` の Before セクション（`keySet + get` のループ）を `entrySet` ループに書き直してみましょう。
   ハッシュ計算が1回で済む理由をコードを見ながら説明してみましょう。

4. `ComparatorDemo.java` で使っている `(a, b) -> a.getPrice() - b.getPrice()` は、第06章で学んだどのインターフェースを実装していますか？
   `list.sort()` が「高階関数」と言える理由を「引数に何を受け取るか」という観点で説明しましょう。

5. `LruCache.java` のキャパシティを `3` から `2` に変えて実行してみましょう。
   ステップ3（D を追加）のとき削除されるのは B ではなく何になりますか？実行前に予測してから確認しましょう。

---

## 次章（第08章）への問いかけ

第07章を終えて、次のような疑問を持ちましたか？

- コレクションを「for 文でループして条件分岐」するコードをもっと短く書く方法はないか？
- `null` を返すメソッドがあると呼び出し元で必ず `if (x != null)` チェックが必要になる—もっと安全に表現する方法は？
- 「日付を文字列でフォーマットする」処理は `SimpleDateFormat` でいいのか？なぜ現場では `DateTimeFormatter` が推奨されるのか？

**これらの答えは第08章「モダン API と堅牢なコーディング」で学びます。**

---

| [← 第06章: OOP・型システム](../oop_and_type_system/README.md) | [全章目次](../../../../../../README.md) | 第08章: モダンAPIと堅牢なコーディング（準備中） |
| :--- | :---: | ---: |
