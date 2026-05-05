# 第05章：便利な道具箱とミニゲーム

プログラムに「Javaの標準機能を活用する力」と「ユーザーと対話する力」を与えましょう。

## はじめに：なぜ「道具箱」が必要なのか？（Why）

「0〜99のランダムな整数を変数に入れたい」とします。
自分でゼロから書くなら、乱数のアルゴリズムを実装しなければいけません。

でも現実には、誰もそんなことはしません。

Javaが最初から提供している **Random** クラスを使えば、1行で済むからです。

```java
Random rand = new Random();
int answer = rand.nextInt(100); // 0〜99のランダムな整数
```

Javaには、こうした「あらかじめ用意された便利なクラス」が何千個もあります。
これらをまとめて **標準ライブラリ** と呼びます。

この章では、現場のコードで日常的に使われる3つの道具箱を体験します。

| 道具箱 | 役割 |
| :--- | :--- |
| **Scanner** | キーボードからの入力を受け取る |
| **ArrayList** | 後から自由に要素を追加・削除できるリスト |
| **HashMap** | 「名前 → 値段」のような対応表 |

ArrayList と HashMap の使い方は **CollectionDemo.java** で体験します。
そして最後に、Scanner と Random（乱数）を使った「**数当てゲーム**」を動かします。

---

## 1. import とは何か？

標準ライブラリを使うためには、コードの先頭に **import**（インポート）宣言を書く必要があります。

```java
import java.util.ArrayList;
import java.util.Scanner;
```

**なぜ import が必要なのか？**

`ArrayList` は `java.util` という「棚」に収納されています。
この「棚」のことを **パッケージ** といいます。
`import` を書くことで、Javaに「この棚から持ってきて」と伝えられます。

import なしでも `java.util.ArrayList` とフルネームで書けば使えますが、毎回フルネームを書くのは大変です。

```java
// import がない場合（フルネームで書く必要がある）
java.util.ArrayList<String> weapons = new java.util.ArrayList<>();

// import がある場合（短く書ける）
ArrayList<String> weapons = new ArrayList<>();
```

import を書き忘れると `cannot find symbol` というコンパイルエラーが出ます。
エラーが出たらまず「import を書き忘れていないか」を確認しましょう。

> **補足**: `String` や `System` は import なしで使えます。
> これは `java.lang` パッケージが自動的に読み込まれるためです。
> 「なぜ String だけ import しなくていいのか？」と疑問に思ったときの答えがここにあります。

---

## 2. 配列より便利な「List」と「Map」

実務の開発では、配列（`[]`）よりも、以下の「コレクション」と呼ばれるクラスをよく使います。

**CollectionDemo.java** で動きを確認してみましょう。

### Before：配列で書くと何が不便なのか？

第03章で学んだ配列は「最初に個数を決めなければいけない」という制約があります。

```java
String[] weapons = new String[3]; // 最初に「3本分」と決める
weapons[0] = "木の剣";
weapons[1] = "鉄の剣";
weapons[2] = "勇者の剣";
// 問題: 4本目を追加しようとするとエラーになる！
// weapons[3] = "炎の剣"; // → ArrayIndexOutOfBoundsException!
```

「何件来るかわからないデータ」を扱う実務では、この制約は非常に不便です。

### After：ArrayList を使うと何が解決するのか？

**ArrayList** はサイズが可変（あとから変えられる）です。

```java
ArrayList<String> weapons = new ArrayList<>();
weapons.add("木の剣");
weapons.add("鉄の剣");
weapons.add("勇者の剣");
weapons.add("炎の剣"); // 配列と違って後から追加できる！
```

> **補足**: `ArrayList<String>` の `<String>` は「この箱に入れるのは文字（String）だけ」という宣言です。
> 違う型を入れようとするとコンパイルエラーで教えてくれます。
> この仕組みを「ジェネリクス」といいます。第06章で詳しく学びます。

主なメソッド：

| メソッド | 動き |
| :--- | :--- |
| `add(データ)` | 末尾に追加 |
| `get(番号)` | 番号で取り出し（0始まり） |
| `size()` | 要素数を返す |

> **注意**: 配列は `names.length`（括弧なし）、ArrayList は `weapons.size()`（括弧あり）です。
> 混乱したら、実際に間違えてコンパイルエラーを体験してみましょう。

### HashMap (マップ)

「キー」と「値」をセットで保存するクラスです。
Web開発でよく使う「JSON」というデータ形式と似た考え方です。

#### Before：変数で代替しようとすると…

「薬草は100ゴールド、毒消しは150ゴールド」を変数で管理しようとすると、品目が増えるたびに変数が増え続けます。

```java
int yakusouPrice  = 100;
int dokesinPrice  = 150;
int mannaPrice    = 300;
// アイテムが100種類あったら変数が100個必要！
// しかも名前でアイテムを指定する方法がない
```

#### After：HashMap を使えばキー（名前）で直接アクセスできる

```java
HashMap<String, Integer> items = new HashMap<>();
items.put("薬草", 100);
items.put("毒消し", 150);

int price = items.get("薬草"); // → 100
```

主なメソッド：

| メソッド | 動き |
| :--- | :--- |
| `put(キー, 値)` | 保存 |
| `get(キー)` | 値を取り出す |

---

## 3. 最終課題：数当てゲーム（Game.java）

これまでの学習の総決算です。
**Scanner**（入力を受け取る機能）と **Random**（サイコロ機能）を使って、簡単なゲームを作ります。

### 「意図的な無限ループ」という新しいパターン

第03章の while 文では `while (countdown > 0)` のように「終了条件を最初から決めて書く」形を学びました。

しかし今回のゲームには「何回で正解するか分からない」という問題があります。
最初から終了条件を書けないのです。

そこで現場でよく使われる「**意図的な無限ループ + break**」パターンを使います。

```java
while (true) { // 条件が常に true = ずっと繰り返し続ける
    // 毎回の処理

    if (正解したら) {
        break; // break でループを強制的に抜け出す
    }
}
```

「ループを終わらせる責任が `break` に移っている」のがポイントです。
`break` を書き忘れると、正解しても永遠にゲームが続きます。

第03章では「無限ループは危険」と学びましたが、**break で出口を用意した意図的な無限ループ**は現場の常套手段です。

### 使っている技術

| 技術 | 役割 |
| :--- | :--- |
| `import java.util.Scanner` | キーボード入力を読み込む機能を使う宣言 |
| `while (true)` | 正解が出るまで無限に繰り返す |
| `if - else if - else` | 数字の大小・一致を判定する |
| `break` | ループを強制終了してゲームを終わらせる |

### 実行方法

このプログラムは、あなたがキーボードで数字を入力しないと進みません。

1. 「Run」ボタンを押す。
2. 下のターミナルに「数字を入力してください >」と出る。
3. ターミナルをクリックして、数字を入力し Enter キーを押す。
4. 正解するまで頑張る！

> ゲームが終わると `scanner.close()` が呼ばれます。
> これはキーボードとの接続を「閉じる」後片付けの処理です。
> ファイルを開いたら閉じる・接続したら切る、という「使ったら片付ける」習慣は現場で重要なルールです。

---

## よくある間違い（アンチパターン）

### NG例1：import を書き忘れる、または位置を間違える

`import` はファイルの先頭（`package` 宣言の直後）に書くルールがあります。
メソッドの中に書いてもコンパイルエラーになります。

```java
// NG: import がないと "cannot find symbol" エラーになる
// （ファイルの中に import 宣言がない状態）
ArrayList<String> weapons = new ArrayList<>(); // エラー!

// OK: ファイルの先頭（package 宣言の直後）に書く
// package com.example.practical_java;  ← package宣言
// import java.util.ArrayList;          ← その直後に import
// ...
// public class CollectionDemo {
//     ArrayList<String> weapons = new ArrayList<>(); // OK
```

### NG例2：ArrayList の .size() と配列の .length を混同する

```java
// 配列: .length（括弧なし）
String[] names = {"Alice", "Bob"};
System.out.println(names.length); // OK

// ArrayList: .size()（括弧あり）
ArrayList<String> weapons = new ArrayList<>();
System.out.println(weapons.size());  // OK
System.out.println(weapons.length); // NG: コンパイルエラー!
```

### NG例3：while(true) から break を消してしまう

```java
while (true) {
    if (guess == answer) {
        // break; を消すと、正解しても「正解！！！」と表示しながらゲームが続いてしまう
    }
}
```

`break` はループの「出口」です。消すと永遠に終わりません（Ctrl+C で停止できます）。

### NG例4：数字以外の文字を入力してしまう

`scanner.nextInt()` は「整数を読む」命令です。
数字以外（例: `abc`）を入力すると `InputMismatchException` というエラーが発生します。
数当てゲームでは整数だけ入力してください。

### NG例5：`HashMap.get()` の戻り値を `int` に直接代入する（存在しないキーの場合）

存在しないキーを指定すると `get()` は `null` を返します。`null` を `int`（プリミティブ型）に自動変換（オートアンボクシング）しようとした瞬間に `NullPointerException` が発生します。

```java
// NG: 存在しないキーの場合 get() は null を返す
// null を int に自動変換（オートアンボクシング）しようとして NullPointerException が発生する
int price = items.get("存在しないアイテム"); // NullPointerException!

// OK: まず Integer で受け取り、null チェックをしてから使う
Integer price = items.get("存在しないアイテム");
if (price != null) {
    System.out.println("値段: " + price);
}
// → null を安全に扱う Optional クラスは第08章で学びます
```

---

## 第05章のまとめ

- **標準ライブラリ**: Javaが最初から用意している便利なクラス群。`import` で使えるようにする
- **import**: `java.util.ArrayList` などの「棚の場所 + クラス名」を宣言する。書き忘れると `cannot find symbol` エラー
- **ArrayList**: サイズ可変のリスト。`add()` / `get()` / `size()` で操作する（配列の `.length` と混同しない）
- **HashMap**: キーと値の対応表。`put()` で保存、`get()` で取り出す
- **`while(true)` + `break`**: 終了条件が事前に決まらない場合へ使う「意図的な無限ループ + 出口」パターン
- **Scanner**: `scanner.nextInt()` でキーボードから整数を受け取る。使い終わったら `close()` する

---

## 確認してみよう

1. `CollectionDemo.java` を実行すると武器が何本表示されるか確認してみよう。`weapons.add("魔法の剣");` をさらに追加して実行すると「個数」はどう変わるか。
2. `items.get("存在しないキー")` を実行するとどうなるか確認してみよう。`null` という値が返ってくる。「null って何だろう？」という疑問は第08章で解決する。
3. `Game.java` の `break;` をコメントアウト（`//` を先頭につける）してゲームを動かすと何が起きるか確認してみよう。`break` の役割を実感したら元に戻しておこう（止まらなくなったら Ctrl+C で停止）。
4. 数当てゲームに「最大10回までしか挑戦できない」というルールを追加するには、どこをどう変えればよいか考えてみよう。ヒント：`count` 変数はすでにある。
5. `for (String w : weapons)` のループ部分を `weapons.forEach(w -> System.out.println("武器: " + w))` に書き換えてみよう。第06章で学ぶラムダ式の書き方である。今の時点では意味が分からなくても OK だ。

---

## 超入門編のまとめ：次へのステップ

お疲れ様でした！これでJavaの「超入門編（第01〜05章）」は完了です。

あなたが学んだこと：

1. **基本文法**: 変数、型、if文、for文
2. **オブジェクト指向**: クラス、メソッド、インスタンス、カプセル化
3. **標準機能**: ArrayList、HashMap、Scanner

---

### 次は第06章へ：OOP・型システム

第06章からは対象レベルが「超入門」から「基礎応用」へ切り替わります。

ここまでは「動くコードを書けること」を目標にしてきました。
第06章からは「**なぜ現場ではその書き方をするのか**」を問い続けます。

たとえば、こんな問いに向き合います：

- `ArrayList` を `for` 文でループするより、**ラムダ式** を使う書き方があるのはなぜ？
- 定数を `String` で管理するのではなく、**Enum（列挙型）** を使うのはなぜ？
- `Object` 型でなんでも受け取れるのに、なぜ **ジェネリクス（`<T>`）** が必要なの？

超入門編で身につけた基礎は、これらの問いに答えるための土台になっています。
ぜひ次の章へ進んでください！

---

| [← 第04章: クラスとオブジェクト](../class_and_objects/README.md) | [全章目次](../../../../../../README.md) | [第06章: OOP・型システム →](../oop_and_type_system/README.md) |
| :--- | :---: | ---: |
