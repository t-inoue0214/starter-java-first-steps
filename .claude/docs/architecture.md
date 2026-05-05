## ディレクトリ構成

README・ソースコードはすべて `src/main/java/com/example/` 配下の章フォルダにまとめて配置する。
フォルダ名 = Javaパッケージ名（Javaの仕様上、パッケージ名は数字で始められないため番号プレフィックスなし）。

```text
starter-java-first-steps/
└── src/main/java/com/example/
    ├── introduction/          # 第01章: Javaに触れてみよう（README.md + .java）
    ├── variables_and_types/   # 第02章: データと型
    ├── control_flow/          # 第03章: プログラムの流れを作る
    ├── class_and_objects/     # 第04章: クラスとオブジェクト
    ├── practical_java/        # 第05章: 便利な道具箱とミニゲーム
    ├── oop_and_type_system/   # 第06章: OOP・型システム
    ├── collections_deep/      # 第07章: データ構造を使いこなす
    ├── modern_api/            # 第08章: モダンAPIと堅牢なコーディング（追加予定）
    ├── algorithms/            # 第09章: アルゴリズムとソート（追加予定）
    ├── io_and_network/        # 第10章: I/OとWebの基礎（追加予定）
    ├── database_jdbc/         # 第11章: データベースアクセス JDBC（追加予定）
    ├── concurrency/           # 第12章: 並行処理・非同期処理の基礎（追加予定）
    ├── http_client/           # 第13章: HTTPクライアントと外部API連携（追加予定）
    └── architecture/          # 第14章: 設計とアーキテクチャ（追加予定）
```

## 全14章の構成

| 章 | ディレクトリ | タイトル | 状態 |
| --- | --- | --- | --- |
| 01 | `introduction/` | Javaに触れてみよう | 実装済み |
| 02 | `variables_and_types/` | データと型 | 実装済み |
| 03 | `control_flow/` | プログラムの流れを作る | 実装済み |
| 04 | `class_and_objects/` | クラスとオブジェクト | 実装済み |
| 05 | `practical_java/` | 便利な道具箱とミニゲーム | 実装済み |
| 06 | `oop_and_type_system/` | OOP・型システム | 実装済み |
| 07 | `collections_deep/` | データ構造を使いこなす | 実装済み |
| 08 | `modern_api/` | モダンAPIと堅牢なコーディング | 追加予定 |
| 09 | `algorithms/` | アルゴリズムとソート | 追加予定 |
| 10 | `io_and_network/` | I/OとWebの基礎 | 追加予定 |
| 11 | `database_jdbc/` | データベースアクセス（JDBC） | 追加予定 |
| 12 | `concurrency/` | 並行処理・非同期処理の基礎 | 追加予定 |
| 13 | `http_client/` | HTTPクライアントと外部API連携 | 追加予定 |
| 14 | `architecture/` | 設計とアーキテクチャ | 追加予定 |

## 章のレベル区分

| 章 | レベル | 対象読者 |
| --- | --- | --- |
| 第01〜05章 | 超入門 | プログラミング経験ゼロから。丁寧な説明・多めのコメント |
| 第06〜09章 | 基礎応用 | 基礎は理解した新卒向け。OOP・モダンAPI・データ構造・アルゴリズムの深掘り |
| 第10〜13章 | 実践 | 外部リソース（ファイル・DB・スレッド・HTTP）を扱う現場課題を体験 |
| 第14章 | 設計 | 全章の知識を統合。なぜその設計にするかを徹底的に問う |

## 重要概念の担当章

| 概念 | 初出・主担当章 | 備考 |
| --- | --- | --- |
| `static` の意味（main メソッドへの言及のみ） | 第01章 | 深い説明はしない |
| アクセス修飾子・スコープ・`static` vs インスタンス | **第04章** | `private`/`public` の違いをアンチパターンとともに体験 |
| `static` の誤用・危険性（乱用時の保守性低下） | **第06章** | `StaticAndAnnotation.java` で扱う |
| ラムダ式・関数型インターフェース | 第06章 | 従来の匿名クラス記法を先に示し、対比する |
| `var`（型推論）の紹介 | 第02章 README の補足のみ | サンプルコードには使わない |
| 例外処理の深掘り（カスタム例外・チェック例外） | **第08章** | 各章での `XxxException` 登場はOK。深い解説は第08章に集約 |
| ソートアルゴリズム・O記法・二分探索 | **第09章** | 標準ライブラリの `Arrays.sort()` との比較まで扱う |
| HTTPクライアント・コネクションプール・KeepAlive | **第13章** | `java.net.http.HttpClient`（Java 11+）を使う |

## Java SE 7 互換性注釈のルール

学習者の現場には Java SE 7 のコードが残存しており、Java SE 21 へアップグレードする機会がある。
Java 8 以降で追加された構文・API を扱うときは、以下のルールで注釈を付ける。

### 注釈ラベルの使い分け

| 注釈ラベル | 意味 | 主な対象 |
| --- | --- | --- |
| `[Java 7 不可]` | Java 7 では書けない。代替手段が必要 | ラムダ式・Stream API・`var`・テキストブロック・Records・`List.of()`・`HttpClient` 等 |
| `[Java 7 代替]` | Java 7 でも書けるが書き方が異なる | 匿名クラス・拡張 for ループ・`Arrays.asList()` 等、旧来の相当コード |
| `[Java 7 動作差異]` | 書けるが仕様・動作の一部が異なる | 例: switch 文の fall-through 挙動など。差異を具体的に明記 |

### 注釈が不要な機能（Java 7 で導入済み）

- `try-with-resources`（Java 7）
- ダイヤモンド演算子 `<>`（Java 7）
- `switch` 文での文字列比較（Java 7）
- マルチキャッチ `catch (A | B e)`（Java 7）

### `.java` ファイルでの記載フォーマット

```java
// [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では匿名クラスで書く:
//   list.sort(new Comparator<String>() {
//       @Override public int compare(String a, String b) { return a.compareTo(b); }
//   });
list.sort((a, b) -> a.compareTo(b));

// [Java 7 不可] Stream API は Java 8 以降。Java 7 では拡張 for ループで書く:
//   for (String s : list) { if (s.startsWith("A")) { result.add(s); } }
List<String> result = list.stream().filter(s -> s.startsWith("A")).toList();

// [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
//   List<String> names = Arrays.asList("Alice", "Bob");
List<String> names = List.of("Alice", "Bob");
```

### `README.md` での記載フォーマット

```markdown
> **[Java 7 との違い]** ラムダ式は Java 8 以降の機能です。Java 7 では匿名クラスを使います。
> ```java
> // Java 7 での書き方（匿名クラス）
> list.sort(new Comparator<String>() {
>     @Override public int compare(String a, String b) { return a.compareTo(b); }
> });
> ```
```

### Java 8 以降で追加された主な機能と代替手段

| 機能 | 導入バージョン | Java 7 での対応 |
| --- | --- | --- |
| ラムダ式 `(a, b) -> ...` | Java 8 | 匿名クラスで代替 |
| Stream API `stream().filter()...` | Java 8 | 拡張 `for` ループで代替 |
| メソッド参照 `System.out::println` | Java 8 | ラムダ式または匿名クラスで代替 |
| `default` / `static` インターフェースメソッド | Java 8 | 書けない（抽象クラスで代替） |
| `Optional<T>` | Java 8 | `null` チェックで代替 |
| `java.time`（`LocalDate` 等） | Java 8 | `java.util.Date` / `Calendar` で代替 |
| `List.of()` / `Map.of()` / `Set.of()` | Java 9 | `Arrays.asList()` / `Collections.unmodifiableList()` 等で代替 |
| `var`（型推論） | Java 10 | 型を明示して書く |
| `String.isBlank()` / `strip()` / `lines()` | Java 11 | `trim()` / `isEmpty()` 等で代替 |
| `java.net.http.HttpClient` | Java 11 | Apache HttpClient 等の外部ライブラリで代替 |
| テキストブロック `"""..."""` | Java 15 | 文字列連結 or `StringBuilder` で代替 |
| `instanceof` パターンマッチング | Java 16 | 明示的キャストで代替 |
| Records | Java 16 | 通常クラス（コンストラクタ＋getter）で代替 |
| `switch` 式（`->`） | Java 14 | 従来の `switch` 文で代替 |
| Sealed クラス | Java 17 | 書けない（継承制限の代替手段なし） |
| 仮想スレッド（Virtual Threads） | Java 21 | 通常のスレッドで代替 |

## コードスタイルのルール

- 各 `.java` ファイルは単独で `main` メソッドから実行できる形式（第14章の OnionArchitecture サブパッケージを除く）
- コメントは**日本語**で、初心者が読んでわかる説明を添える
- Java 21 のモダン構文（switch式の `->` 、テキストブロック等）を積極的に使う
- **`var` はサンプルコードで使わない**。型を明示して書く（`var` の紹介は第02章 README の補足のみ）
- 学習用コードのため、過度な抽象化・設計パターンは不要（第14章を除く）
- 各ファイルの先頭に `/** 【なぜこのコードを学ぶのか】... */` 形式の Why ヘッダーを必ず付ける
- **アクセス修飾子は常に明示する**（`public`・`private`・`protected`・パッケージプライベート）
- `static` は最初から多用しない。第04章以降で段階的に深める

## Why体験型コンテンツの設計原則

- **問題先行**: まず「悪い/不便な書き方」「問題が起きる状態」を体験させ、その後に解決策を示す
- **対比掲載**: 旧来の書き方と現代の書き方を同一ファイル内に並べて差を可視化する（`// ========== Before: 〜 ==========` 形式）
- **全記述方法の提示**: 書き方が複数ある場合はすべて示し、同じ結果になることを実行で確認させ、使い分けの理由を説明する
- **アンチパターンの掲載**: 各章に「やってはいけない書き方とその理由」を必ず含める
- **計測体験**: パフォーマンス差は `System.nanoTime()` で計測して標準出力に出す
- **非決定的な動作の明示**: 第12章のスレッド系ファイルは `main` メソッドの直上に「実行するたびに結果が変わりうる」旨のコメントを必ず記載する
- **可読性重視**: ソースコードは読みやすさを重視することを推奨する（記述が楽だからはNG）

## 各章 README のナビゲーションバー

すべての章の `README.md` 末尾（本文の後、最終行）に以下のテーブル形式のナビゲーションバーを置く。

```markdown
| [← 第NN章: タイトル](../前章フォルダ名/README.md) | [全章目次](../../../../../../README.md) | [第NN章: タイトル →](../次章フォルダ名/README.md) |
|:---|:---:|---:|
```

- ルート `README.md` へのパスは **`../../../../../../README.md`**（章フォルダから6階層上）
- 前章がない場合（第01章）: 左セルを空欄にする
- 次章がない場合（第14章）: 右セルをリンクなしのテキスト `第NN章（準備中）` にする
- ナビゲーションバーの直前に `---` 区切り線を入れる

### 各章の対応表

| 章 | フォルダ名 | 章タイトル |
| :--- | :--- | :--- |
| 01 | `introduction` | Javaに触れてみよう |
| 02 | `variables_and_types` | データと型 |
| 03 | `control_flow` | プログラムの流れを作る |
| 04 | `class_and_objects` | クラスとオブジェクト |
| 05 | `practical_java` | 便利な道具箱とミニゲーム |
| 06 | `oop_and_type_system` | OOP・型システム |
| 07 | `collections_deep` | データ構造を使いこなす |
| 08 | `modern_api` | モダンAPIと堅牢なコーディング |
| 09 | `algorithms` | アルゴリズムとソート |
| 10 | `io_and_network` | I/OとWebの基礎 |
| 11 | `database_jdbc` | データベースアクセス（JDBC） |
| 12 | `concurrency` | 並行処理・非同期処理の基礎 |
| 13 | `http_client` | HTTPクライアントと外部API連携 |
| 14 | `architecture` | 設計とアーキテクチャ |

## 確認してみよう

各章の最後に「確認してみよう」を追加して、理解度チェックする問いを３問〜６問程度用意する。

## 実行方法

### 第01〜10章・第12〜13章（外部ライブラリ不要）

```bash
javac -d out/ src/main/java/com/example/introduction/Hello.java
java -cp out/ com.example.introduction.Hello
```

### 第11章（JDBC）

```bash
javac -d out/ -cp lib/h2.jar src/main/java/com/example/database_jdbc/ConnectionBasics.java
java -cp out/:lib/h2.jar com.example.database_jdbc.ConnectionBasics
```

### 第14章（OnionArchitecture）

```bash
javac -d out/ $(find src/main/java/com/example/architecture -name "*.java")
java -cp out/ com.example.architecture.Main
```

## 注意事項

- 一次情報は [Oracle公式ドキュメント(dev.java)](https://dev.java/learn/) を参照すること
- 学習者が混乱しないよう、説明コメントを削除・簡略化しない
- `com.sun.net.httpserver.HttpServer`（第10章）は内部APIのため「本番コードでは使わない」旨を README に必ず記載する
- 第11章の H2 依存 JAR は `lib/h2.jar` に配置し、Gradle/Maven は使わない
- 第13章の実習 API（httpbin.org）はテスト専用。本番 API への誤接続を防ぐ注意書きを README に必ず入れる
