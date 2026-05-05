# 第13章 実装計画: http_client（HTTPクライアントと外部API連携）

## 概要

`java.net.http.HttpClient`（Java 11 以降の標準API）を使って外部 Web API を呼び出す。
第10章で作ったHTTPサーバーに対してクライアントを接続する体験も可能（README に補足を入れる）。

## 必須実装内容

- `HttpClient` / `HttpRequest` / `HttpResponse` の3クラスの役割を最初に整理する
- GET リクエスト（同期・非同期）
- POST リクエスト（JSON ボディの送信）
- レスポンスの受け取りとステータスコード確認
- 接続タイムアウト・読み取りタイムアウトの設定
- コネクションプール: `HttpClient` は再利用前提で作られており、**毎回 `new HttpClient()` するのはアンチパターン**。シングルトンまたはフィールドに持つ理由をコードで示す
- KeepAlive: HTTP/1.1 と HTTP/2 で接続を使い回す仕組みと `HttpClient.Version` の設定
- キャッシュ戦略: `ETag` / `Cache-Control` ヘッダーの読み方と条件付きリクエスト（`If-None-Match`）
- パフォーマンス計測: `System.nanoTime()` でコネクション再利用あり/なしの速度差を確認させる
- アンチパターン: 毎回 `new HttpClient()` を生成するコスト・スレッドプールの無駄遣い

## 実習用 API

実習用の外部 API は <https://httpbin.org>（テスト用公開 API）を使う。
本番相当の URL を誤って叩かないよう README に注意書きを入れること。

## Java SE 7 との差異ポイント

| 機能 | Java バージョン | Java 7 での代替 |
| --- | --- | --- |
| `java.net.http.HttpClient` | Java 11 | Apache HttpClient 等の外部ライブラリで代替 |
