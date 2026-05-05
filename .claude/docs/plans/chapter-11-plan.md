# 第11章 実装計画: database_jdbc（データベースアクセス JDBC）

## 概要

H2 データベースを使った JDBC の基礎。ビルドツールなしで手動クラスパス指定。

## 必須実装内容

- Maven/Gradle は使わない。`lib/h2.jar` をリポジトリに含めてクラスパス指定で実行
- `Connection` は `try-with-resources` で必ずクローズする
- `Statement` ではなく `PreparedStatement` を基本とする（SQLインジェクション対策）
- SQLインジェクションは「Statement で受ける側」と「PreparedStatement で防ぐ側」を実行して比較
- トランザクション（`commit` / `rollback`）の基礎

## 実行コマンド

```bash
javac -d out/ -cp lib/h2.jar src/main/java/com/example/database_jdbc/ConnectionBasics.java
java -cp out/:lib/h2.jar com.example.database_jdbc.ConnectionBasics
```

## 注意事項

- `lib/h2.jar` はリポジトリに含める（Gradle/Maven は使わない）
- H2 はインメモリモード（`jdbc:h2:mem:testdb`）で動作確認することを推奨
