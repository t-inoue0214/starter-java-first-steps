/**
 * 【なぜこのコードを学ぶのか】
 * Spring Boot などの Web フレームワークは、内部で HTTP リクエストを受け取り
 * レスポンスを返す仕組みを持っている。その仕組みをゼロから手書きして
 * 「Web サーバーとは何か」を体験する。
 * また、大きな main クラスに全ロジックを詰め込んだ状態がいかに保守しにくいかを
 * 体験する（→ HttpServerRefactored.java で責務の分離により解決する）。
 *
 * 【注意】com.sun.net.httpserver.HttpServer は内部 API・学習専用です。
 * このAPIは本番コードでは使わないこと。
 * フレームワーク（Spring Boot 等）の内部がどう動いているかを体験するための学習用 API です。
 */
package com.example.io_and_network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpServerBasics {

    // =====================================================================
    // 【注意】com.sun.net.httpserver.HttpServer は学習用の内部APIです。
    // このAPIは本番コードでは使わないこと。
    // フレームワーク（Spring Boot 等）の内部がどう動いているかを体験するための学習用APIです。
    // =====================================================================
    public static void main(String[] args) throws IOException, InterruptedException {

        // ---------------------------------------------------------
        // ========== Before: 全ロジックを main に詰め込んだアンチパターン ==========
        // ---------------------------------------------------------
        // ルーティング・ビジネスロジック・レスポンス生成がすべて main に混在している。
        // エンドポイントを1つ追加するたびに main が長くなり、
        // テストも再利用も困難になる典型的なアンチパターン。
        // → HttpServerRefactored.java で責務を分離して解決する。

        // ポート 8080 でサーバーを作成する（バックログ 0 = OSにデフォルトを任せる）
        // [Java 7 不可] com.sun.net.httpserver.HttpServer は Java 6 以降に存在するが、
        //   内部 API のため本番利用不可。学習専用として使う。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ---------------------------------------------------------
        // ハンドラー1: GET / → "Hello, World!" を返す
        // ---------------------------------------------------------
        // 問題: ハンドラーの処理が匿名クラス（またはラムダ）として main に埋め込まれる。
        //       ロジックが増えるほど、どこに何があるかわからなくなる。

        // [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では匿名クラスで書く:
        //   server.createContext("/", new com.sun.net.httpserver.HttpHandler() {
        //       @Override
        //       public void handle(HttpExchange exchange) throws IOException {
        //           byte[] body = "Hello, World! This is a Java HTTP Server."
        //               .getBytes(StandardCharsets.UTF_8);
        //           exchange.sendResponseHeaders(200, body.length);
        //           OutputStream os = exchange.getResponseBody();
        //           os.write(body);
        //           exchange.close();
        //       }
        //   });
        server.createContext("/", (HttpExchange exchange) -> {
            String responseBody = "Hello, World! This is a Java HTTP Server.";
            byte[] bodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);

            // ステータス 200 とレスポンスボディのバイト数を送信する
            exchange.sendResponseHeaders(200, bodyBytes.length);

            // レスポンスボディを書き込んで接続を閉じる
            OutputStream os = exchange.getResponseBody();
            os.write(bodyBytes);
            exchange.close();

            System.out.println("[アクセスあり] GET /");
        });

        // ---------------------------------------------------------
        // ハンドラー2: GET /time → 現在時刻を返す
        // ---------------------------------------------------------
        // 問題: ビジネスロジック（時刻フォーマット）がハンドラーに直書きされている。
        //       時刻フォーマットを変えたいとき、このコードを探して修正しなければならない。

        server.createContext("/time", (HttpExchange exchange) -> {
            // [Java 7 不可] java.time.LocalDateTime は Java 8 以降。
            //   Java 7 では java.util.Date / Calendar を使う:
            //   String time = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            //       .format(new java.util.Date());
            String currentTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            String responseBody = "現在時刻: " + currentTime;
            byte[] bodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(200, bodyBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bodyBytes);
            exchange.close();

            System.out.println("[アクセスあり] GET /time → " + currentTime);
        });

        // ---------------------------------------------------------
        // ハンドラー3: GET /echo → クエリパラメータ msg の値をそのまま返す
        // ---------------------------------------------------------
        // 問題: クエリパラメータの解析ロジックがハンドラーに直書きされている。
        //       他のエンドポイントで同じ処理が必要になっても再利用できない。
        //       例: /echo?msg=Hello → "Echo: Hello" を返す

        server.createContext("/echo", (HttpExchange exchange) -> {
            // クエリ文字列を取得する（例: "msg=Hello"）
            String query = exchange.getRequestURI().getQuery();
            String msg = "";

            // クエリ文字列を手作業でパースする（再利用できないアンチパターン）
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("msg")) {
                        msg = kv[1];
                    }
                }
            }

            String responseBody = "Echo: " + msg;
            byte[] bodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(200, bodyBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bodyBytes);
            exchange.close();

            System.out.println("[アクセスあり] GET /echo → msg=" + msg);
        });

        // ---------------------------------------------------------
        // サーバー起動
        // ---------------------------------------------------------
        // null を指定すると単一スレッドで動作する（リクエストが来るたびに順番に処理）
        server.setExecutor(null);
        server.start();

        System.out.println("サーバー起動: http://localhost:8080");
        System.out.println("エンドポイント:");
        System.out.println("  GET /          → Hello, World!");
        System.out.println("  GET /time      → 現在時刻");
        System.out.println("  GET /echo?msg= → Echo 返し");
        System.out.println("5秒後に自動終了します（テスト用）");
        System.out.println();

        // ---------------------------------------------------------
        // 問題点まとめ（このファイルの課題）
        // ---------------------------------------------------------
        // 1. ハンドラー追加のたびに main が長くなる
        // 2. ルーティング・ビジネスロジック・レスポンス生成が混在してテストも再利用も難しい
        // 3. レスポンス送信コード（sendResponseHeaders → write → close）が各ハンドラーに重複している
        // → HttpServerRefactored.java で Handler / Service / Helper に分割して解決する

        // 5秒待機後にサーバーを停止する（テスト用の自動終了）
        // [Java 7 不可] Thread.sleep() 自体は Java 1.0 からあるが、
        //   InterruptedException のハンドリングに注意が必要。
        Thread.sleep(5000);
        server.stop(0);
        System.out.println("サーバーを停止しました。");
    }
}
