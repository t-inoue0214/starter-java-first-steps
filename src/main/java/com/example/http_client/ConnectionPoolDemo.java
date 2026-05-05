/**
 * 【なぜこのコードを学ぶのか】
 * HttpClient はコネクションプールを内部で管理し、一度確立した TCP 接続を
 * 複数リクエストで再利用する（HTTP KeepAlive）。
 * 「毎回 new HttpClient()」するのは、毎回コネクションを張り直す無駄なアンチパターンだ。
 * この差を System.nanoTime() で実測し、Vert.x の WebClient.create(vertx) も
 * 同様に「1回だけ作って使い回す」設計になっている理由を体験する。
 *
 * 【注意】実習 API として https://httpbin.org（テスト専用）を使用する。
 * このURLを本番の API エンドポイントと間違えないこと。
 *
 * [Java 7 不可] java.net.http.HttpClient は Java 11 以降。
 *   Java 7/8 では Apache HttpClient 等の外部ライブラリを使う。
 */
package com.example.http_client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ConnectionPoolDemo {

    // 実習 API のベース URL（テスト専用。本番 API への誤接続に注意）
    private static final String BASE_URL = "https://httpbin.org";

    // 計測用リクエスト回数（多いほど差が出やすいが時間もかかる）
    private static final int REQUEST_COUNT = 5;

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("========================================");
        System.out.println("  ConnectionPoolDemo: コネクションプールと KeepAlive");
        System.out.println("========================================");
        System.out.println();
        System.out.println("リクエスト回数: " + REQUEST_COUNT + " 回（各セクション共通）");
        System.out.println();

        // ---------------------------------------------------------
        // section1: Before = 毎回 new HttpClient() するアンチパターン
        // ---------------------------------------------------------
        long beforeElapsed = section1AntiPattern();

        // ---------------------------------------------------------
        // section2: After = HttpClient を1回だけ作って再利用
        // ---------------------------------------------------------
        long afterElapsed = section2SharedClient();

        // ---------------------------------------------------------
        // section3: Before vs After の比較まとめ
        // ---------------------------------------------------------
        section3Comparison(beforeElapsed, afterElapsed);

        // ---------------------------------------------------------
        // section4: HTTP/1.1 vs HTTP/2 の設定と KeepAlive
        // ---------------------------------------------------------
        section4HttpVersionComparison();
    }

    // ==========================================================
    // section1: Before = 毎回 new HttpClient() するアンチパターン
    // ==========================================================
    private static long section1AntiPattern() throws IOException, InterruptedException {

        System.out.println("--- section1: Before = 毎回 new HttpClient()（アンチパターン） ---");
        System.out.println();
        System.out.println("問題点:");
        System.out.println("  1. 毎回 TCP 接続を新しく確立する → ハンドシェイクのコストがかかる");
        System.out.println();
        System.out.println("     ▼ ハンドシェイクとは？");
        System.out.println("     クライアントとサーバーが通信を始める前に行う「接続確立の手続き」。");
        System.out.println("     TCP では3回のメッセージ交換（SYN → SYN-ACK → ACK）が必要で、");
        System.out.println("     この往復がリクエストのたびに発生するとレイテンシが増える。");
        System.out.println("     HTTPS ではさらに TLS ハンドシェイク（証明書の検証と暗号鍵の交換）も加わる。");
        System.out.println();
        System.out.println("  2. スレッドプールも毎回作られ、GC 対象になる → リソースの無駄遣い");
        System.out.println("  3. HTTP/2 の多重化の恩恵を受けられない");
        System.out.println();

        // ========== Before: 毎回 new HttpClient() を生成する（アンチパターン） ==========
        long beforeStart = System.nanoTime();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            // ★ 問題のある書き方: ループの中で new HttpClient() を呼ぶ
            // 毎回新しいコネクションプールとスレッドプールが作られる
            // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
            HttpClient newClient = HttpClient.newHttpClient();  // ← 毎回生成（アンチパターン）

            // [Java 7 不可] HttpRequest は Java 11 以降
            // [Java 7 不可] java.time.Duration は Java 8 以降
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/get"))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            try {
                // [Java 7 不可] HttpResponse は Java 11 以降
                HttpResponse<String> resp = newClient.send(req, HttpResponse.BodyHandlers.ofString());
                System.out.println("  [Before] リクエスト " + (i + 1) + ": " + resp.statusCode());
            } catch (IOException e) {
                System.err.println("[エラー] リクエスト " + (i + 1) + " 失敗: " + e.getMessage());
            }
            // 注意: newClient をクローズしていないため、GC まで接続リソースが解放されない
        }

        long beforeElapsed = System.nanoTime() - beforeStart;
        System.out.println("Before（毎回 new）合計: " + (beforeElapsed / 1_000_000) + " ms");
        System.out.println();

        return beforeElapsed;
    }

    // ==========================================================
    // section2: After = HttpClient を1回だけ作って再利用
    // ==========================================================
    private static long section2SharedClient() throws IOException, InterruptedException {

        System.out.println("--- section2: After = HttpClient を1回だけ作って再利用 ---");
        System.out.println();

        // ========== After: HttpClient を1回だけ作ってフィールドまたはシングルトンに保持 ==========
        // HttpClient は内部でコネクションプールを持ち、同じホストへの接続を自動的に再利用する。
        // Vert.x の WebClient.create(vertx) も同様に1回だけ作るのが正しい使い方。
        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        // [Java 7 不可] java.time.Duration は Java 8 以降
        HttpClient sharedClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)  // HTTP/2 で多重化を活用する
                .build();

        long afterStart = System.nanoTime();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            // [Java 7 不可] HttpRequest は Java 11 以降
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/get"))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            try {
                // [Java 7 不可] HttpResponse は Java 11 以降
                HttpResponse<String> resp = sharedClient.send(req, HttpResponse.BodyHandlers.ofString());
                System.out.println("  [After] リクエスト " + (i + 1) + ": " + resp.statusCode()
                        + " (バージョン: " + resp.version() + ")");
            } catch (IOException e) {
                System.err.println("[エラー] リクエスト " + (i + 1) + " 失敗: " + e.getMessage());
            }
        }

        long afterElapsed = System.nanoTime() - afterStart;
        System.out.println("After（再利用）合計: " + (afterElapsed / 1_000_000) + " ms");
        System.out.println();

        return afterElapsed;
    }

    // ==========================================================
    // section3: Before vs After の比較まとめ
    // ==========================================================
    private static void section3Comparison(long beforeElapsed, long afterElapsed) {

        System.out.println("--- section3: Before vs After の比較まとめ ---");
        System.out.println();

        long diffMs = (beforeElapsed - afterElapsed) / 1_000_000;

        System.out.println("Before（毎回 new）: " + (beforeElapsed / 1_000_000) + " ms");
        System.out.println("After（再利用）  : " + (afterElapsed / 1_000_000) + " ms");

        if (diffMs > 0) {
            System.out.println("差: " + diffMs + " ms の短縮（After の方が速い）");
        } else {
            System.out.println("差: " + Math.abs(diffMs) + " ms（今回は差が出なかった or After の方が遅かった）");
            System.out.println("  ※ 短いリクエスト数やサーバーの応答時間により差が出ない場合がある。");
            System.out.println("    本番環境で数百・数千リクエスト/秒になると差は明確になる。");
        }

        System.out.println();
        System.out.println("コネクションプールの恩恵:");
        System.out.println("  After は2回目以降の TCP ハンドシェイク（接続確立の手続き）が不要。");
        System.out.println("  HTTP/2 では1つの接続で複数リクエストを多重化するためさらに効率的。");
        System.out.println("  Vert.x の WebClient も内部でコネクションプールを管理している。");
        System.out.println();
    }

    // ==========================================================
    // section4: HTTP/1.1 vs HTTP/2 の設定と KeepAlive
    // ==========================================================
    private static void section4HttpVersionComparison() throws IOException, InterruptedException {

        System.out.println("--- section4: HTTP/1.1 vs HTTP/2 の設定と KeepAlive ---");
        System.out.println();

        // HTTP/1.1 クライアント（KeepAlive で接続を維持するが多重化はできない）
        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        // [Java 7 不可] java.time.Duration は Java 8 以降
        HttpClient http1Client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // HTTP/2 クライアント（多重化で効率的に通信できる）
        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        HttpClient http2Client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/get"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        // HTTP/1.1 でリクエスト
        System.out.println("HTTP/1.1 クライアントでリクエスト:");
        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> http1Response = http1Client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("  ステータス: " + http1Response.statusCode()
                    + " / 実際のバージョン: " + http1Response.version());
        } catch (IOException e) {
            System.err.println("[エラー] HTTP/1.1 リクエスト失敗: " + e.getMessage());
        }

        // HTTP/2 でリクエスト
        System.out.println("HTTP/2 クライアントでリクエスト:");
        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> http2Response = http2Client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("  ステータス: " + http2Response.statusCode()
                    + " / 実際のバージョン: " + http2Response.version());
            System.out.println("  ※ HTTP/2 を指定しても、サーバー非対応なら HTTP_1_1 にフォールバックする");
        } catch (IOException e) {
            System.err.println("[エラー] HTTP/2 リクエスト失敗: " + e.getMessage());
        }

        System.out.println();
        System.out.println("HTTP バージョンの違い:");
        System.out.println("  HTTP/1.1: 1つの接続で1リクエストずつ処理。");
        System.out.println("    connection: keep-alive で接続を維持するが、リクエストは順番待ち。");
        System.out.println("    100リクエストあれば100回往復が必要（Head-of-Line Blocking 問題）。");
        System.out.println();
        System.out.println("  HTTP/2  : 1つの接続で複数リクエストを多重化（Multiplexing）。");
        System.out.println("    ストリームという仮想チャネルを使い、複数リクエストを同時に送受信できる。");
        System.out.println("    Head-of-Line Blocking が解消され、帯域効率が大幅に向上する。");
        System.out.println();
        System.out.println("  HttpClient.Version.HTTP_2 を設定するとサーバーが対応していれば HTTP/2 を使い、");
        System.out.println("  非対応なら HTTP/1.1 に自動的にフォールバックする（安全な設定）。");
        System.out.println("  Vert.x の WebClient も内部で同様のフォールバック機構を持つ。");
    }
}
