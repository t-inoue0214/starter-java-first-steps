# 第08章 実装計画: modern_api（モダンAPIと堅牢なコーディング）

## 概要

Stream API・Optional・java.time に加え、**例外処理を深く扱う**。
各章で `XxxException` が登場するのはOKだが、深い解説はこの章に集約する。

## 必須実装内容

### 例外処理（この章の核心）

- チェック例外 vs 非チェック例外の違い（コンパイル強制 vs 任意）
- `Throwable` / `Exception` / `RuntimeException` の継承ツリーと使い分け
- 意図的に例外を発生させる理由と方法（`throw new XxxException("メッセージ")`）
- 自作例外クラスの作り方（`RuntimeException` を継承したカスタム例外）
- catch ブロックの責務: ログ出力だけでなく、デフォルト値の返却・ロールバック処理を示す
- `finally` / `try-with-resources` でファイル・ネットワーク接続を確実にクローズする理由と方法
- アンチパターン: 空の catch・`catch (Exception e) {}` で握りつぶす問題

### モダンAPI

- Stream API（`filter`, `map`, `collect`, `reduce`）
- `Optional<T>`（null を安全に扱う）
- `java.time`（`LocalDate`, `LocalDateTime`, `ZonedDateTime`）

### StreamAPI.java の実装方針

#### 【設計意図】同じ問題を3通りで解いてパラダイムの使い分けを体験する

「リストから条件に合う要素を取り出して変換する」という同一の問題を3通りで解く構成にする。

- **解法1（手続き型）**: `for` ループ + `if` 文 + 中間変数リスト
- **解法2（OOP）**: 処理をメソッドに切り出したクラス
- **解法3（関数型）**: `stream().filter().map().collect()`

3通りの実行結果が同じになることを確認させ、コメントで「どれが正解かではなく、状況で選ぶ。Java のコードは3つが混在するのが現実だ」という説明を添える。
これは第06章の「プログラミングパラダイムの地図」で整理した3つのパラダイムを、概念ではなく**コードとして体験**させる設計となる。第06章で「地図」を読んだ学習者が、本章で「3通りの実地体験」を通じてパラダイムの使い分けを身体感覚として記憶することを意図している。

### ImmutableDesign.java の実装計画

第04章で「`Dog` クラスに setter を書かなかった理由」の答えをここで明かす。

#### Before → Middle → After の3段階構成

**Before（JavaBeans スタイル）: setter を持つ mutable な `UserBean` クラス**

- `setAge(-1)` のように不正な値を setter 経由で設定できてしまう問題を体験させる。
- setter 後の状態変化がバグの温床になることをコードで確認させる。
- 「第04章の `Dog` クラスに setter を書かなかった理由」への答えがここで明かされることをコメントで明示する。

**Middle（手動イミュータブル）: setter を削除し `private final` フィールド + コンストラクタのみの設計**

- フィールドが `final` であれば、オブジェクトの状態は作った瞬間から変わらないという約束が生まれることを説明する。
- これが第04章の `Dog` クラスが目指していた設計と同じ形であることをコメントで明示する。

**After（Records）: `record User(String name, int age) {}` で Middle と同じクラスを1行で定義する**

- Records は Java 16 以降の機能であるため `[Java 7 不可]` 注釈を付ける。
- Java 7 での代替手段（`private final` フィールド + コンストラクタ + getter のみのクラス）をコメントで示す。

#### 第12章への橋渡し

`ImmutableDesign.java` の末尾に「setter を削除することで、複数のスレッドが同じオブジェクトを参照しても状態が壊れない（スレッドセーフ）」という一文を添える。「詳しくは第12章（並行処理）で体験する」という橋渡しコメントも合わせて記載すること。

#### Java SE 7 互換性

| 機能 | 導入バージョン | 注釈 |
| --- | --- | --- |
| Records（`record` キーワード） | Java 16 | `[Java 7 不可]` |
| `private final` フィールド・コンストラクタ・getter | Java 1 | 注釈不要 |

## Java SE 7 との差異ポイント

| 機能 | Java バージョン | Java 7 での代替 |
| --- | --- | --- |
| `Optional<T>` | Java 8 | `null` チェックで代替 |
| `java.time`（`LocalDate` 等） | Java 8 | `java.util.Date` / `Calendar` で代替 |
| Stream API | Java 8 | 拡張 `for` ループで代替 |
| テキストブロック | Java 15 | 文字列連結 or `StringBuilder` で代替 |
