# 第05章 実装計画: practical_java（便利な道具箱とミニゲーム）

## 概要

第04章で「自分でクラスを作る」体験を終えた学習者が、Java が提供する標準クラス（道具箱）を「使う」体験に集中する章。
Scanner・ArrayList・HashMap・Random を組み合わせた数当てゲームを最終課題として、前章までの知識（クラス・メソッド・制御構文）を統合する。
第06章では「ArrayList の <String> はなぜ必要か」「ラムダ式はなぜ登場したか」という問いに発展するため、本章はその問いかけで締める設計にする。

---

## 前章からの接続 / 次章への橋渡し

### 第04章から受け取ること

- クラスとは「設計図」であり `new` でインスタンスを作ることを知っている
- `Scanner`・`ArrayList` のように `new クラス名()` と書けば使えることが腑に落ちている
- アクセス修飾子（`public`/`private`）とメソッド呼び出しの基本が身についている

### 第06章へ渡すこと

- `ArrayList<String>` の `<String>` への疑問（「なぜ型を書くの？」）を意図的に残す
  - 第06章の `GenericsBasics.java` への橋渡しになる
- `for` ループでリストを回す冗長さへの違和感（「もっと短く書けないの？」）を意図的に残す
  - 第06章の `LambdaBasics.java` への橋渡しになる
- `HashMap` の `get()` が `null` を返す現象への疑問（「null って何？」）を意図的に残す
  - 第08章の `Optional` への橋渡しになる

## Java SE 7 互換性の考慮

| 本章で使う Java 8+ の機能 | 導入バージョン | Java 7 での代替手段 |
| --- | --- | --- |
| `<>` ダイヤモンド演算子 | Java 7 | Java 7 でも使用可能（注釈不要） |
| `ArrayList`・`HashMap`・`Scanner`・`Random` | Java 2〜 | 全て Java 7 で使用可能（注釈不要） |
| `for-each`（拡張 for 文） | Java 5 | Java 7 で使用可能（注釈不要） |

本章（第05章）は Java 8+ の新機能をほぼ使わない。`[Java 7 不可]` 注釈が必要な箇所は原則なし。
ただし将来的に `List.of()` に書き換える場合は `[Java 7 不可]` 注釈が必要になる。

### README への Java 7 注記

CollectionDemo.java の `ArrayList<String>` の説明箇所に補足として以下を追加する（優先度：低）。

> **[Java 7 との違い]** `List.of("木の剣", "鉄の剣")` という不変リストを作る方法は Java 9 以降でのみ利用できます。Java 7・8 では `Arrays.asList()` を使います。

---

## アンチパターン（必ず1つ以上含めること）

### 現状で掲載済みのアンチパターン（README）

1. `import` を書き忘れる / 位置を間違える（NG例1）
2. `ArrayList.size()` と配列の `.length` を混同する（NG例2）
3. `while(true)` から `break` を消す（NG例3）
4. 数字以外の入力で `InputMismatchException` が発生する（NG例4）

### 追加推奨のアンチパターン

**NG: `HashMap.get()` の戻り値を `int` に直接代入する（存在しないキー）**

```java
// NG: 存在しないキーの場合 get() は null を返す
// int への null 代入はオートアンボクシング時に NullPointerException が発生する
int price = items.get("存在しないアイテム"); // NullPointerException!

// OK: null チェックを先にする（第08章の Optional で改善する方法を学ぶ）
Integer price = items.get("存在しないアイテム");
if (price != null) {
    System.out.println("値段: " + price);
}
```

このアンチパターンは「確認してみよう」問2 で「試してみよう」という形で誘導することで、第08章の `Optional` への動機付けになる。

---

## 実行コマンド

```bash
# CollectionDemo を実行する
javac -d out/ src/main/java/com/example/practical_java/CollectionDemo.java
java -cp out/ com.example.practical_java.CollectionDemo

# 数当てゲームを実行する（キーボード入力が必要）
javac -d out/ src/main/java/com/example/practical_java/Game.java
java -cp out/ com.example.practical_java.Game
```

---

## 注意点

### 学習者への配慮

- 本章は「超入門」レベルの最終章であり、クラスを「使う」体験に集中させる。
  自分でクラスを設計する（継承・インターフェース）は第06章に委ねる。
- `ArrayList<String>` の `<String>` の意味は「詳しくは第06章で学ぶ」という一行注記にとどめ、深入りしない。
- ゲームの「面白さ」が学習の入り口になるため、Game.java のコメントは親しみやすいトーンを維持する。

### 既存コンテンツとの整合性

- 第03章（制御フロー）で学んだ `while` / `break` / `if-else` を Game.java で再利用する構成は意図的。
- 第04章で学んだ「インスタンスを作って使う」（`new Scanner()`・`new Random()` 等）が本章の前提になっている。

### README の現状評価

- 現状の README は完成度が高く、構成上の大きな改善点はない。
- 「超入門編のまとめ」セクションが第01〜05章の締めくくりとして機能しており、第06章への問いかけ（3問）が橋渡しとして適切に配置されている。
- ナビゲーションバー（前章・目次・次章）は設置済みで書式も正しい。
