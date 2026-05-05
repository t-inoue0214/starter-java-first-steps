/**
 * 【なぜこのコードを学ぶのか】
 * Web API の呼び出しコストを下げる最強の武器がキャッシュだ。
 * Cache-Control ヘッダーで「何秒間キャッシュを使っていいか」を伝え、
 * ETag で「内容が変わったかどうか」を確認する。
 * If-None-Match で 304 Not Modified を受け取れれば、
 * ボディの転送が省略され帯域・レイテンシを大幅削減できる。
 * Vert.x の WebClient も同じ HTTP ヘッダーの仕組みの上で動く。
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

public class CacheStrategyDemo {

    // 実習 API のベース URL（テスト専用。本番 API への誤接続に注意）
    private static final String BASE_URL = "https://httpbin.org";

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("========================================");
        System.out.println("  CacheStrategyDemo: キャッシュ戦略の基礎");
        System.out.println("========================================");
        System.out.println();

        // HttpClient は1つ作って使い回す（ConnectionPoolDemo の教訓を実践）
        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        // [Java 7 不可] java.time.Duration は Java 8 以降
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();

        // ---------------------------------------------------------
        // section1: Cache-Control ヘッダーを読む
        // ---------------------------------------------------------
        section1CacheControl(client);

        // ---------------------------------------------------------
        // section2: ETag の仕組みを理解する
        // ---------------------------------------------------------
        String etag = section2ETag(client);

        // ---------------------------------------------------------
        // section3: If-None-Match で条件付きリクエスト（304 Not Modified の体験）
        // ---------------------------------------------------------
        section3ConditionalRequest(client, etag);
    }

    // ==========================================================
    // section1: Cache-Control ヘッダーを読む
    // ==========================================================
    private static void section1CacheControl(HttpClient client) throws IOException, InterruptedException {

        System.out.println("--- section1: Cache-Control ヘッダーを読む ---");
        System.out.println();
        System.out.println("エンドポイント: GET " + BASE_URL + "/cache");
        System.out.println("このエンドポイントは Cache-Control・ETag・Last-Modified ヘッダーを返す。");
        System.out.println();

        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cache"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("ステータスコード: " + response.statusCode());

            // レスポンスヘッダーから各値を取り出す
            // [Java 7 不可] Optional.orElse() は Java 8 以降
            String cacheControl = response.headers().firstValue("cache-control").orElse("なし");
            String etag = response.headers().firstValue("etag").orElse("なし");
            String lastModified = response.headers().firstValue("last-modified").orElse("なし");

            System.out.println("Cache-Control : " + cacheControl);
            System.out.println("ETag          : " + etag);
            System.out.println("Last-Modified : " + lastModified);

        } catch (IOException e) {
            System.err.println("[エラー] Cache-Control 確認に失敗しました: " + e.getMessage());
            System.err.println("  httpbin.org に接続できるか確認してください。");
        }

        System.out.println();
        System.out.println("Cache-Control のディレクティブ（現場でよく使うもの）:");
        System.out.println("  max-age=N : N秒間はキャッシュを使ってよい（再リクエスト不要）");
        System.out.println("  no-cache  : キャッシュしていいが、毎回サーバーに検証を求める（ETag を使う）");
        System.out.println("  no-store  : キャッシュ禁止（認証情報などの機密データに使う）");
        System.out.println("  public    : 中継プロキシでもキャッシュしていい");
        System.out.println("  private   : ブラウザのみキャッシュ可（共有プロキシは不可）");
        System.out.println();
        System.out.println("max-age=3600 なら1時間はキャッシュを再利用できるため、");
        System.out.println("1時間以内に同じリソースを参照するユーザーはサーバーへの接続が不要になる。");
        System.out.println();
    }

    // ==========================================================
    // section2: ETag の仕組みを理解する
    // ==========================================================
    private static String section2ETag(HttpClient client) throws IOException, InterruptedException {

        System.out.println("--- section2: ETag の仕組みを理解する ---");
        System.out.println();

        System.out.println("ETag（Entity Tag）とは:");
        System.out.println("  レスポンスに付けられた「このリソースのバージョン識別子」。");
        System.out.println("  ファイルのハッシュ値や更新日時を元にサーバーが生成する。");
        System.out.println("  次のリクエスト時に If-None-Match ヘッダーで送り返すと、");
        System.out.println("  サーバーは「内容が変わっていないか」を確認できる。");
        System.out.println();

        // httpbin.org/etag/{etag} は指定した値を ETag ヘッダーとして返すエンドポイント
        String etagEndpoint = BASE_URL + "/etag/my-unique-etag-value";
        System.out.println("エンドポイント: GET " + etagEndpoint);

        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest firstRequest = HttpRequest.newBuilder()
                .uri(URI.create(etagEndpoint))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        // リクエスト1回目: サーバーから ETag を受け取る
        String etag = "なし";
        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> firstResp = client.send(firstRequest, HttpResponse.BodyHandlers.ofString());

            // [Java 7 不可] Optional.orElse() は Java 8 以降
            etag = firstResp.headers().firstValue("etag").orElse("なし");

            System.out.println("1回目: ステータス=" + firstResp.statusCode()
                    + " / ETag=" + etag
                    + " / ボディサイズ=" + firstResp.body().length() + " バイト");

        } catch (IOException e) {
            System.err.println("[エラー] ETag 取得に失敗しました: " + e.getMessage());
            return etag;
        }

        System.out.println();
        System.out.println("ETag を受け取ったら、次のリクエストで If-None-Match ヘッダーに付けて送る。");
        System.out.println("サーバーは ETag が一致すれば 304 Not Modified を返し、ボディの転送を省略する。");
        System.out.println();

        return etag;
    }

    // ==========================================================
    // section3: If-None-Match で条件付きリクエスト（304 Not Modified の体験）
    // ==========================================================
    private static void section3ConditionalRequest(HttpClient client, String etag)
            throws IOException, InterruptedException {

        System.out.println("--- section3: If-None-Match で条件付きリクエスト ---");
        System.out.println();

        if (etag.equals("なし")) {
            System.err.println("[スキップ] section2 で ETag を取得できなかったため、このセクションをスキップします。");
            return;
        }

        String etagEndpoint = BASE_URL + "/etag/my-unique-etag-value";

        // ========== Before: ETag なしで毎回フルレスポンスを受け取る ==========
        // → 内容が変わっていなくても毎回ボディを転送するので帯域・時間の無駄

        System.out.println("========== Before: ETag なしで毎回フルレスポンス ==========");
        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest noEtagRequest = HttpRequest.newBuilder()
                .uri(URI.create(etagEndpoint))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();  // ETag ヘッダーを付けない

        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> noEtagResp = client.send(noEtagRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("ステータス=" + noEtagResp.statusCode()
                    + " / ボディサイズ=" + noEtagResp.body().length() + " バイト");
            System.out.println("→ 内容が変わっていなくてもフルボディを受信している（帯域の無駄）");
        } catch (IOException e) {
            System.err.println("[エラー] Before リクエスト失敗: " + e.getMessage());
        }

        System.out.println();

        // ========== After: If-None-Match で条件付きリクエスト ==========
        // ETag が一致すれば 304 Not Modified が返り、ボディの転送がスキップされる
        System.out.println("========== After: If-None-Match で条件付きリクエスト ==========");
        System.out.println("前回受け取った ETag: " + etag);
        System.out.println("If-None-Match ヘッダーに付けて送ることで、");
        System.out.println("「このバージョン（ETag）から変わっていなければ 304 を返して」とサーバーに伝える。");
        System.out.println();

        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest conditionalRequest = HttpRequest.newBuilder()
                .uri(URI.create(etagEndpoint))
                .timeout(Duration.ofSeconds(10))
                // HTTP/2 では小文字が仕様（RFC 7540）。HTTP/1.1 でも現代の慣習は小文字
                .header("if-none-match", etag)  // 前回受け取った ETag を付ける
                .GET()
                .build();

        // リクエスト2回目: If-None-Match 付き（ETag が一致するので 304 が返るはず）
        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> secondResp = client.send(conditionalRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("2回目（If-None-Match 付き）: ステータス=" + secondResp.statusCode());

            if (secondResp.statusCode() == 304) {
                System.out.println("→ 304 Not Modified！ボディの転送がスキップされた（帯域節約）");
                System.out.println("  ボディサイズ: " + secondResp.body().length() + " バイト（0のはず）");
                System.out.println("  クライアントは手元にキャッシュしたレスポンスをそのまま使う。");
            } else {
                System.out.println("→ " + secondResp.statusCode()
                        + "（ETag が変わったかサーバーが条件付きリクエスト非対応）");
            }
        } catch (IOException e) {
            System.err.println("[エラー] 条件付きリクエスト失敗: " + e.getMessage());
        }

        System.out.println();

        // ---------------------------------------------------------
        // 異なる ETag を付けた場合（必ず 200 が返ることを確認）
        // ---------------------------------------------------------
        System.out.println("参考: 異なる ETag を送った場合（ETag が一致しないので 200 が返る）");

        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest mismatchRequest = HttpRequest.newBuilder()
                .uri(URI.create(etagEndpoint))
                .timeout(Duration.ofSeconds(10))
                .header("if-none-match", "\"wrong-etag\"")  // 意図的に間違った ETag を送る
                .GET()
                .build();

        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> mismatchResp = client.send(mismatchRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("3回目（異なる ETag）: ステータス=" + mismatchResp.statusCode()
                    + " / ボディサイズ=" + mismatchResp.body().length() + " バイト → フルレスポンス");
        } catch (IOException e) {
            System.err.println("[エラー] 不一致 ETag リクエスト失敗: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== まとめ: キャッシュ戦略のポイント ===");
        System.out.println("  Cache-Control: max-age=N → N秒間はサーバーへのリクエスト自体が不要");
        System.out.println("  Cache-Control: no-cache  → 毎回サーバーに確認するが ETag で帯域を節約");
        System.out.println("  ETag + If-None-Match     → ボディ転送をスキップして 304 を受け取る");
        System.out.println("  Last-Modified + If-Modified-Since → ETag の代替（精度が低い）");
        System.out.println();
        System.out.println("  Vert.x の WebClient も同じ HTTP ヘッダーを使う。");
        System.out.println("  高頻度で呼び出す API にはキャッシュ戦略を設計することで");
        System.out.println("  サーバー負荷・ネットワーク帯域・レイテンシを大幅に削減できる。");
    }
}
