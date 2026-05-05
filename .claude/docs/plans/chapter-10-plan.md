# 第10章 実装計画: io_and_network（I/OとWebの基礎）

## 概要

CSV/JSON/XML の読み書き・HTTPサーバースクラッチ開発（サーバー側）。
第08章の例外処理（try-with-resources）を実践で使う章として位置づける。

## 必須実装内容

- ファイルの読み書き（`BufferedReader`, `BufferedWriter`, `Files`）
- ファイル・ストリームは必ず try-with-resources でクローズする
- CSV/JSON/XML の読み書きサンプル
- `com.sun.net.httpserver.HttpServer` でHTTPサーバーをスクラッチ実装
- 大きな `main` クラスからHandler/Service/Repositoryへの段階的リファクタリングを体験させる

## 特記事項

`com.sun.net.httpserver.HttpServer` を使う箇所には必ず以下の注意書きを入れること:

> このAPIは本番コードでは使わないこと。フレームワークの内部がどう動いているかを体験するための学習用APIです。

## Java SE 7 との差異ポイント

| 機能 | Java バージョン | Java 7 での代替 |
| --- | --- | --- |
| `Files.readString()` | Java 11 | `BufferedReader` で代替 |
| `Files.writeString()` | Java 11 | `BufferedWriter` で代替 |
