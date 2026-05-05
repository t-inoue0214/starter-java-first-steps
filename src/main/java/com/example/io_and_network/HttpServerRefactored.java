/**
 * 【なぜこのコードを学ぶのか】
 * HttpServerBasics.java の「全部 main に詰め込む」コードを、
 * Handler / Service / Helper に分割してリファクタリングする。
 * 第04章・第06章で学んだ「責務の分離」と「インターフェースによるポリモーフィズム」が
 * 現場でどう活きるかを体験する。
 * 各クラスの役割が明確になることで、テスト・再利用・変更がいかに容易になるかを確認する。
 *
 * 【注意】com.sun.net.httpserver.HttpServer は内部 API・学習専用です。
 * このAPIは本番コードでは使わないこと。
 * フレームワーク（Spring Boot 等）の内部がどう動いているかを体験するための学習用 API です。
 *
 * 【クラス構成】
 *   HttpServerRefactored  ... main クラス（ルーター登録・起動・停止のみ）
 *   ├── HelloHandler      ... HttpHandler 実装: GET / のレスポンスを返す
 *   ├── TimeHandler       ... HttpHandler 実装: GET /time のレスポンスを返す
 *   ├── EchoHandler       ... HttpHandler 実装: GET /echo のレスポンスを返す
 *   ├── TimeService       ... ビジネスロジック（現在時刻の取得・フォーマット）
 *   ├── QueryParser       ... ユーティリティ（クエリ文字列から値を取得）
 *   └── ResponseHelper    ... 共通処理（レスポンス送信）
 */
package com.example.io_and_network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpServerRefactored {

    // =====================================================================
    // 【注意】com.sun.net.httpserver.HttpServer は学習用の内部APIです。
    // このAPIは本番コードでは使わないこと。
    // フレームワーク（Spring Boot 等）の内部がどう動いているかを体験するための学習用APIです。
    // =====================================================================
    public static void main(String[] args) throws IOException, InterruptedException {

        // ========== Before（HttpServerBasics.java）との違い ==========
        // Before: 全ルーティング・ロジック・レスポンス処理が main に混在
        //         → ルート追加のたびに main が肥大化し、テスト不可能
        // After:  責務を分離
        //         Handler  → HTTPリクエストの受け取りとレスポンスの返送
        //         Service  → ビジネスロジック（時刻取得など）
        //         Helper   → 共通処理（レスポンス送信）
        //         → ルート追加は新しい Handler クラスを追加するだけ
        // =================================================================

        // ポート 8080 でサーバーを作成する
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ---------------------------------------------------------
        // ルーティング登録: 各 Handler クラスをインスタンス化して割り当てるだけ
        // ---------------------------------------------------------
        // Before との比較: main はルーターの登録のみを担当する。
        //   ロジックはすべて各 Handler クラスに委譲されている。
        server.createContext("/", new HelloHandler());
        server.createContext("/time", new TimeHandler());
        server.createContext("/echo", new EchoHandler());

        // 単一スレッドで動作させる
        server.setExecutor(null);
        server.start();

        System.out.println("サーバー起動: http://localhost:8080");
        System.out.println("エンドポイント:");
        System.out.println("  GET /          → Hello, World! (Refactored)");
        System.out.println("  GET /time      → 現在時刻");
        System.out.println("  GET /echo?msg= → Echo 返し");
        System.out.println("5秒後に自動終了します（テスト用）");
        System.out.println();
        System.out.println("[リファクタリングの効果]");
        System.out.println("  新しいエンドポイントを追加するとき: 新しい Handler クラスを作って");
        System.out.println("  server.createContext() を1行追加するだけ。main は肥大化しない。");
        System.out.println("  TimeService を単体でテストしたいとき: main を起動せずに");
        System.out.println("  new TimeService().getCurrentTime() を呼ぶだけでテストできる。");

        Thread.sleep(5000);
        server.stop(0);
        System.out.println("サーバーを停止しました。");
    }

    // =========================================================
    // ビジネスロジック: 現在時刻の取得・フォーマット
    // =========================================================
    // Before との違い: HttpServerBasics では LocalDateTime.now().format() が
    //   ハンドラーの中に直書きされていた。
    //   After ではこのクラスを単体でテストでき、フォーマット変更もここだけ直せばよい。
    private static class TimeService {

        // 現在時刻を "yyyy/MM/dd HH:mm:ss" 形式の文字列で返す
        public String getCurrentTime() {
            // [Java 7 不可] java.time.LocalDateTime は Java 8 以降。
            //   Java 7 では java.util.Date / SimpleDateFormat を使う:
            //   return new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            //       .format(new java.util.Date());
            return LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        }
    }

    // =========================================================
    // 共通処理: レスポンス送信
    // =========================================================
    // Before との違い: HttpServerBasics では
    //   sendResponseHeaders → write → close の3行が各ハンドラーに重複していた。
    //   After ではこの共通処理を1か所にまとめ、修正が必要なときも1か所を直すだけでよい。
    private static class ResponseHelper {

        // ステータスコードとボディ文字列を指定してレスポンスを送信する
        // try-with-resources で OutputStream を確実にクローズする（Java 7 で導入）
        public static void sendResponse(HttpExchange exchange, int statusCode, String body)
                throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // =========================================================
    // ユーティリティ: クエリ文字列から指定キーの値を取得する
    // =========================================================
    // Before との違い: HttpServerBasics では for ループによるパース処理が
    //   EchoHandler の中に埋め込まれていた。
    //   After ではこのクラスを取り出してテスト・再利用が可能になっている。
    private static class QueryParser {

        // クエリ文字列（例: "msg=Hello&lang=ja"）から指定キーの値を返す。
        // クエリが null またはキーが見つからない場合は空文字列を返す。
        public static String getParam(String query, String key) {
            if (query == null) {
                return "";
            }
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && kv[0].equals(key)) {
                    return kv[1];
                }
            }
            return "";
        }
    }

    // =========================================================
    // Handler: GET / → "Hello, World! (Refactored)" を返す
    // =========================================================
    // HttpHandler インターフェースを実装することで、
    // server.createContext() に渡せる型として機能する（ポリモーフィズム）。
    // [Java 7 不可] HttpHandler は com.sun.net.httpserver パッケージの内部API。
    //   本番コードでは使わないこと。
    private static class HelloHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ResponseHelper.sendResponse(exchange, 200, "Hello, World! (Refactored)");
            System.out.println("[アクセスあり] GET /");
        }
    }

    // =========================================================
    // Handler: GET /time → 現在時刻を返す
    // =========================================================
    // TimeService を使うことでビジネスロジックがハンドラーから分離されている。
    // TimeService を差し替えれば（テスト用の固定時刻を返す実装など）、
    // ハンドラーのコードを変えずに動作を切り替えられる。
    private static class TimeHandler implements HttpHandler {

        // Handler はビジネスロジックを TimeService に委譲する
        private final TimeService timeService = new TimeService();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String time = timeService.getCurrentTime();
            ResponseHelper.sendResponse(exchange, 200, "現在時刻: " + time);
            System.out.println("[アクセスあり] GET /time → " + time);
        }
    }

    // =========================================================
    // Handler: GET /echo → クエリパラメータ msg の値を返す
    // =========================================================
    // クエリ解析を QueryParser に委譲することで、
    // 他の Handler から同じロジックを再利用できる。
    private static class EchoHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            // QueryParser に解析を委譲する（再利用可能）
            String msg = QueryParser.getParam(query, "msg");
            ResponseHelper.sendResponse(exchange, 200, "Echo: " + msg);
            System.out.println("[アクセスあり] GET /echo → msg=" + msg);
        }
    }
}
