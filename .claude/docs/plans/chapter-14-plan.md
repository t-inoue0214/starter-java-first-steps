# 第14章 実装計画: architecture（設計とアーキテクチャ）

## 概要

全章の知識を統合する最終章。「なぜその設計にするのか」を徹底的に問う。
他章では禁止している過度な抽象化・設計パターンを、この章では積極的に扱う。

## 必須実装内容

- クリーンアーキテクチャの考え方と必要性
- OnionArchitecture（クリーンアーキテクチャの簡易版）をサブパッケージで実装
- 各レイヤーの役割（Domain, Application, Infrastructure, Presentation）を体験
- 「なぜ大きな `main` クラスではいけないのか」を第10章との対比で示す
- 依存関係の方向（内側への依存のみ）をコードで体現する
- インターフェースを使った依存逆転の原則（DIP）を実践する

## 特記事項

- `OnionArchitecture/` サブパッケージは例外的に複数ファイル構成になる
- エントリーポイントの `Main.java` を必ず用意すること
- 過度な抽象化・設計パターンは**この章のみ**許可する

## 実行コマンド

```bash
javac -d out/ $(find src/main/java/com/example/architecture -name "*.java")
java -cp out/ com.example.architecture.Main
```
