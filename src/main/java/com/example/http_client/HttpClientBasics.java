/**
 * 【なぜこのコードを学ぶのか】
 * Java 11 で標準化された java.net.http.HttpClient は、Apache HttpClient 等の
 * 外部ライブラリなしに HTTP リクエストを送れる標準 API だ。
 * REST API との連携は現場での日常業務であり、
 * HttpClient・HttpRequest・HttpResponse の3クラスの役割分担を理解することで、
 * Vert.x の WebClient がなぜそう設計されているかの原点が見えてくる。
 *
 * 【注意】実習 API として https://httpbin.org（テスト専用）を使用する。
 * このURLを本番の API エンドポイントと間違えないこと。
 * httpbin.org はリクエスト内容をそのまま JSON で返してくれるテスト用サービスだ。
 *
 * [Java 7 不可] java.net.http.HttpClient は Java 11 以降。
 *   Java 7/8 では Apache HttpClient や OkHttp など外部ライブラリを使う必要がある。
 */
package com.example.http_client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HttpClientBasics {

    // 実習 API のベース URL（テスト専用。本番 API への誤接続に注意）
    private static final String BASE_URL = "https://httpbin.org";

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("========================================");
        System.out.println("  HttpClientBasics: HTTP通信の基本");
        System.out.println("========================================");
        System.out.println();

        // ---------------------------------------------------------
        // section1: HttpClient・HttpRequest・HttpResponse の3クラスの役割
        // ---------------------------------------------------------
        section1ThreeClasses();

        // ---------------------------------------------------------
        // section2: 同期 GET リクエスト（結果が返るまで待つ）
        // ---------------------------------------------------------
        section2SynchronousGet();

        // ---------------------------------------------------------
        // section3: 非同期 GET リクエスト（結果を待たずに次の処理へ）
        // ---------------------------------------------------------
        section3AsynchronousGet();

        // ---------------------------------------------------------
        // section4: POST リクエスト（JSON ボディを送る）
        // ---------------------------------------------------------
        section4PostWithJson();

        // ---------------------------------------------------------
        // section5: タイムアウトの設定と HttpTimeoutException の体験
        // ---------------------------------------------------------
        section5Timeout();
    }

    // ==========================================================
    // section1: 3クラスの役割説明
    // ==========================================================
    private static void section1ThreeClasses() {

        System.out.println("--- section1: 3クラスの役割 ---");
        System.out.println();
        System.out.println("java.net.http パッケージには3つの主要クラスがある:");
        System.out.println();

        System.out.println("【1】HttpClient");
        System.out.println("  HTTP 接続を管理する。コネクションプール・プロトコル・タイムアウトを設定する。");
        System.out.println("  ★ 一度作ったら使い回す（毎回 new するのはアンチパターン）。");
        System.out.println("  Vert.x の WebClient.create(vertx) も「1回だけ作る」のが正しい使い方。");
        System.out.println();

        System.out.println("【2】HttpRequest");
        System.out.println("  「どこに」「どのメソッドで」「どんなヘッダー・ボディで」送るかを定義する。");
        System.out.println("  ビルダーパターン（HttpRequest.newBuilder()）で構築する。");
        System.out.println("  Vert.x の WebClient.get(port, host, path) に対応する。");
        System.out.println();

        System.out.println("【3】HttpResponse<T>");
        System.out.println("  レスポンスのステータスコード・ヘッダー・ボディを持つ。");
        System.out.println("  BodyHandlers.ofString() で文字列として受け取れる。");
        System.out.println("  <T> はボディをどんな型で受け取るかを表す型パラメータ。");
        System.out.println();

        // ---------------------------------------------------------
        // HttpClient の作成（プログラム全体で1つだけ作るのが原則）
        // ---------------------------------------------------------
        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降。
        //   Java 7/8 では HttpURLConnection または Apache HttpClient を使う:
        //   HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        // [Java 7 不可] java.time.Duration は Java 8 以降。
        //   Java 7 では数値（ミリ秒）で直接指定する。
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))  // 接続タイムアウト（サーバーへの接続確立まで）
                .version(HttpClient.Version.HTTP_2)       // HTTP/2 を優先（サーバー非対応なら HTTP/1.1 へ自動フォールバック）
                .build();

        System.out.println("HttpClient 作成完了: " + client.version());
        System.out.println("  → version()はデフォルト設定（実際の通信時に HTTP/2 または HTTP/1.1 が選択される）");
        System.out.println();

        // ---------------------------------------------------------
        // HTTP メソッドの種類と冪等性（べきとうせい）
        // ---------------------------------------------------------
        System.out.println("▼ HTTP メソッドの種類と冪等性（べきとうせい）");
        System.out.println();
        System.out.println("  「冪等（べきとう）」とは: 同じ操作を何度繰り返しても結果が変わらない性質。");
        System.out.println("  例: 電気スイッチを「ON にする」操作は冪等。すでに ON でも何度押しても ON のまま。");
        System.out.println("      一方「トグル（ON/OFF 切り替え）」は冪等ではない。");
        System.out.println();
        System.out.println("  メソッド  | 用途                        | 冪等 | 安全（読み取り専用）");
        System.out.println("  --------- | --------------------------- | ---- | --------------------");
        System.out.println("  GET       | リソースの取得              | ○   | ○（サーバー状態を変えない）");
        System.out.println("  HEAD      | GET と同じだがボディなし    | ○   | ○（ヘッダーだけ確認したいとき）");
        System.out.println("  OPTIONS   | 利用できるメソッドを確認    | ○   | ○（CORS プリフライトで使われる）");
        System.out.println("  POST      | リソースの新規作成          | ✗   | ✗（呼ぶたびに新しいリソースが増える）");
        System.out.println("  PUT       | リソースの全体更新（上書き）| ○   | ✗（同じ内容で何度送っても結果は同じ）");
        System.out.println("  PATCH     | リソースの部分更新          | ✗   | ✗（実装によっては冪等にもなる）");
        System.out.println("  DELETE    | リソースの削除              | ○   | ✗（2回目以降は 404 だが状態は変わらない）");
        System.out.println();
        System.out.println("  なぜ冪等性が重要か？");
        System.out.println("  → ネットワーク障害でレスポンスが届かなかったとき、");
        System.out.println("    冪等なメソッド（GET/PUT/DELETE）は安全にリトライできる。");
        System.out.println("    POST をリトライすると同じデータが2重登録される恐れがある。");
        System.out.println("    REST API 設計で「更新は PUT か PATCH か」を選ぶ判断基準になる。");
        System.out.println();
    }

    // ==========================================================
    // section2: 同期 GET リクエスト
    // ==========================================================
    private static void section2SynchronousGet() throws IOException, InterruptedException {

        System.out.println("--- section2: 同期 GET リクエスト ---");

        // HttpClient は1つ作って使い回す（section1 の説明を実践する）
        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        // [Java 7 不可] java.time.Duration は Java 8 以降
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();

        // GET リクエストの構築（ビルダーパターン）
        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/get?name=Java"))  // クエリパラメータ付き URL
                .timeout(Duration.ofSeconds(10))               // このリクエスト単体のタイムアウト
                .GET()                                         // HTTP メソッドを GET に指定
                .build();

        System.out.println("リクエスト先: " + request.uri());

        try {
            // send() は結果が返るまでスレッドをブロックする（同期）
            // BodyHandlers.ofString() でレスポンスボディを String として受け取る
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("ステータスコード: " + response.statusCode());
            System.out.println("HTTP バージョン: " + response.version());

            // レスポンスボディは長いので最初の100文字だけ表示する
            String body = response.body();
            System.out.println("レスポンスボディ（先頭100文字）: " + body.substring(0, Math.min(body.length(), 100)) + "...");

            // ヘッダーの取得（ない場合は "なし" を返す）
            // [Java 7 不可] Optional は Java 8 以降。orElse() は値がない場合のデフォルト値を返す
            String contentType = response.headers().firstValue("content-type").orElse("なし");
            System.out.println("Content-Type: " + contentType);

            // ---------------------------------------------------------
            // ステータスコードによる分岐
            // ---------------------------------------------------------
            // 200系 = 成功 / 400系 = クライアントエラー / 500系 = サーバーエラー
            if (response.statusCode() == 200) {
                System.out.println("→ 成功: " + response.statusCode() + " OK");
            } else if (response.statusCode() >= 400 && response.statusCode() < 500) {
                System.out.println("→ クライアントエラー: " + response.statusCode());
            } else if (response.statusCode() >= 500) {
                System.out.println("→ サーバーエラー: " + response.statusCode());
            } else {
                System.out.println("→ その他のステータス: " + response.statusCode());
            }

            System.out.println();
            // ---------------------------------------------------------
            // HTTPステータスコード全カテゴリのリファレンス
            // ---------------------------------------------------------
            System.out.println("▼ HTTP ステータスコードの全カテゴリ（現場で頻出するものを中心に）");
            System.out.println();
            System.out.println("  【1xx: 情報（Informational）】処理が続行中であることを示す中間レスポンス");
            System.out.println("    100 Continue        : ボディを送り始めてよい（大きなファイル送信の前確認）");
            System.out.println("    101 Switching Proto : プロトコル切替（HTTP → WebSocket へのアップグレードで登場）");
            System.out.println();
            System.out.println("  【2xx: 成功（Success）】リクエストが正常に処理された");
            System.out.println("    200 OK              : 最も基本的な成功。GET・PUT のレスポンスで使う");
            System.out.println("    201 Created         : リソース作成成功。POST 後に返すのが REST の慣習");
            System.out.println("    204 No Content      : 成功したがボディなし。DELETE 後によく使う");
            System.out.println();
            System.out.println("  【3xx: リダイレクト（Redirection）】別の場所を参照するよう指示");
            System.out.println("    301 Moved Permanently : URL が永久に移動（検索エンジンも新 URL を記録）");
            System.out.println("    302 Found             : 一時的なリダイレクト。ログイン後の画面遷移など");
            System.out.println("    304 Not Modified      : キャッシュが有効（ETag 一致）。ボディなしで帯域節約");
            System.out.println();
            System.out.println("  【4xx: クライアントエラー（Client Error）】リクエスト側に問題がある");
            System.out.println("    400 Bad Request       : リクエスト形式が不正（JSON の構文エラーなど）");
            System.out.println("    401 Unauthorized      : 認証が必要（名前に反して「未認証」の意味）");
            System.out.println("    403 Forbidden         : 認証済みだが権限なし（「認可」の失敗）");
            System.out.println("    404 Not Found         : リソースが存在しない");
            System.out.println("    409 Conflict          : 状態が競合（同名ユーザーの重複登録など）");
            System.out.println("    415 Unsupported Media : Content-Type が未対応（JSON 必須なのに text/plain など）");
            System.out.println("    429 Too Many Requests : レートリミット超過。Retry-After ヘッダーで待ち時間を示す");
            System.out.println();
            System.out.println("  【5xx: サーバーエラー（Server Error）】サーバー側に問題がある");
            System.out.println("    500 Internal Server Error : サーバーの予期しないエラー（バグや例外）");
            System.out.println("    502 Bad Gateway           : 上位サーバー（プロキシ等）からの不正レスポンス");
            System.out.println("    503 Service Unavailable   : サーバー過負荷・メンテナンス中。一時的な障害");
            System.out.println("    504 Gateway Timeout       : 上位サーバーがタイムアウト（カスケード障害の兆候）");
            System.out.println();
            System.out.println("  ポイント: 401 vs 403 の違いを間違えやすい。");
            System.out.println("    401 = ログインしていない（誰ですか？）");
            System.out.println("    403 = ログイン済みだが権限がない（あなたには見せられません）");

        } catch (IOException e) {
            // ネットワーク接続失敗時（サーバーが落ちている・ネットワーク断など）
            System.err.println("[エラー] ネットワーク接続に失敗しました: " + e.getMessage());
            System.err.println("  httpbin.org に接続できるか確認してください。");
            return;
        }

        System.out.println();
    }

    // ==========================================================
    // section3: 非同期 GET リクエスト
    // ==========================================================
    private static void section3AsynchronousGet() throws InterruptedException {

        System.out.println("--- section3: 非同期 GET リクエスト ---");
        System.out.println("sendAsync() はすぐに返る（ノンブロッキング）。Vert.x の WebClient.get() も同じ思想。");
        System.out.println();

        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        // [Java 7 不可] java.time.Duration は Java 8 以降
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();

        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/get?name=Async"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        System.out.println("リクエスト先: " + request.uri());
        System.out.println("sendAsync() を呼んだ直後（リクエスト完了を待たずにここが実行される）");

        // sendAsync() はすぐに CompletableFuture を返す（ノンブロッキング）
        // [Java 7 不可] CompletableFuture は Java 8 以降
        // [Java 7 不可] HttpClient.sendAsync() は Java 11 以降
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString());

        // ---------------------------------------------------------
        // 非同期処理のチェーン（コールバックのネストを避けられる）
        // ---------------------------------------------------------
        // thenApply() でレスポンスを受け取って加工する（別スレッドで実行される）
        // [Java 7 不可] CompletableFuture.thenApply() はラムダ式（Java 8）以降
        CompletableFuture<String> resultFuture = future.thenApply(
                resp -> "非同期受信: ステータス=" + resp.statusCode()
                        + " / バージョン=" + resp.version());

        // join() で結果が揃うまでメインスレッドをここで待機させる
        // （Vert.x では join() は使わず、ハンドラーのコールバックで次の処理を書く）
        try {
            String result = resultFuture.join();
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("[エラー] 非同期リクエストに失敗しました: " + e.getMessage());
            return;
        }

        System.out.println();
        System.out.println("同期 vs 非同期の使い分け:");
        System.out.println("  send()      → 結果が出るまで待つ。シンプルだが待ち時間に CPU を使えない。");
        System.out.println("  sendAsync() → すぐに返る。待ち時間に別の処理を並行できる（高スループット）。");
        System.out.println("  Vert.x の WebClient はすべて非同期（sendAsync と同じ思想）。");
        System.out.println();
    }

    // ==========================================================
    // section4: POST リクエスト（JSON ボディを送る）
    // ==========================================================
    private static void section4PostWithJson() throws IOException, InterruptedException {

        System.out.println("--- section4: POST リクエスト（JSON ボディ） ---");

        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        // [Java 7 不可] java.time.Duration は Java 8 以降
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();

        // JSON ボディを文字列で作成（外部ライブラリなし）
        // 現場では Jackson や Gson 等の JSON ライブラリを使うのが一般的
        // [Java 7 不可] テキストブロック """...""" は Java 15 以降。
        //   Java 7 では文字列連結 or StringBuilder を使う:
        //   String jsonBody = "{\"name\": \"Java\", \"version\": 21}";
        String jsonBody = """
                {"name": "Java", "version": 21}
                """.strip();  // テキストブロックの末尾改行を除去する

        // [Java 7 不可] HttpRequest は Java 11 以降
        // HTTP/2 ではヘッダー名を小文字で送ることが仕様（RFC 7540）で定められている。
        // HTTP/1.1 は大文字小文字を区別しないが、現代の慣習として小文字を使う。
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/post"))
                .timeout(Duration.ofSeconds(10))
                .header("content-type", "application/json")  // MIME タイプで送信ボディの形式を宣言
                .header("accept", "application/json")         // レスポンスで受け取りたい MIME タイプを指定
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))  // POST ボディを設定
                .build();

        System.out.println("リクエスト先: POST " + postRequest.uri());
        System.out.println("送信ボディ: " + jsonBody);

        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("ステータスコード: " + response.statusCode());

            // httpbin.org はリクエスト内容をそのまま JSON で返してくれる
            // レスポンスの "data" フィールドに送ったボディが echo されているはず
            String body = response.body();
            System.out.println("レスポンス（先頭200文字）: " + body.substring(0, Math.min(body.length(), 200)) + "...");
            System.out.println();
            System.out.println("↑ レスポンスの \"data\" フィールドに送ったJSONが含まれていることを確認できる。");
            System.out.println("  現場では Jackson の ObjectMapper.readTree() で JSON をパースして使う。");

        } catch (IOException e) {
            System.err.println("[エラー] POST リクエストに失敗しました: " + e.getMessage());
            return;
        }

        System.out.println();
        System.out.println("▼ MIME タイプ（メディアタイプ）とは？");
        System.out.println("  content-type / accept ヘッダーに書く「データ形式の識別子」。");
        System.out.println("  形式: タイプ/サブタイプ  例: application/json, text/html");
        System.out.println();
        System.out.println("  よく使う MIME タイプ一覧:");
        System.out.println("    application/json        : JSON データ（REST API の標準）");
        System.out.println("    application/xml         : XML データ");
        System.out.println("    text/plain              : プレーンテキスト");
        System.out.println("    text/html               : HTML ドキュメント");
        System.out.println("    application/octet-stream: バイナリデータ（ファイルダウンロード等）");
        System.out.println("    multipart/form-data     : ファイルアップロード付きフォーム送信");
        System.out.println("    image/png, image/jpeg   : 画像ファイル");
        System.out.println();
        System.out.println("  content-type: サーバーへ「このボディは○○形式です」と伝える（送信側）");
        System.out.println("  accept      : サーバーへ「○○形式で返してください」と伝える（受信側）");

        System.out.println();
    }

    // ==========================================================
    // section5: タイムアウトの設定と HttpTimeoutException の体験
    // ==========================================================
    private static void section5Timeout() throws InterruptedException {

        System.out.println("--- section5: タイムアウトの体験 ---");
        System.out.println("httpbin.org/delay/3 は3秒間レスポンスを遅延させるエンドポイント。");
        System.out.println("1秒のタイムアウトを設定して HttpTimeoutException を体験する。");
        System.out.println();

        // タイムアウト専用の HttpClient（接続タイムアウトは長めに設定）
        // [Java 7 不可] java.net.http.HttpClient は Java 11 以降
        // [Java 7 不可] java.time.Duration は Java 8 以降
        HttpClient timeoutClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))  // TCP 接続確立のタイムアウト
                .build();

        // 3秒遅延するエンドポイントに 1秒タイムアウトを設定する
        // → 必ず HttpTimeoutException が発生する
        // [Java 7 不可] HttpRequest は Java 11 以降
        HttpRequest slowRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/delay/3"))  // 3秒遅延エンドポイント
                .timeout(Duration.ofSeconds(1))           // 1秒でタイムアウト
                .GET()
                .build();

        System.out.println("リクエスト先: " + slowRequest.uri());
        System.out.println("タイムアウト設定: 1秒（サーバーは3秒後に返す → 必ずタイムアウト）");

        try {
            // [Java 7 不可] HttpResponse は Java 11 以降
            HttpResponse<String> response = timeoutClient.send(
                    slowRequest,
                    HttpResponse.BodyHandlers.ofString());

            // ここには到達しない（タイムアウトするため）
            System.out.println("予期しない成功: " + response.statusCode());

        } catch (HttpTimeoutException e) {
            // HttpTimeoutException は IOException のサブクラス
            // タイムアウト発生時に送出される
            System.out.println("タイムアウト発生！（想定通り）: " + e.getMessage());
            System.out.println("→ 現場では適切なタイムアウト設定が必須。");
            System.out.println("  タイムアウトが長すぎると、障害時にスレッドがずっと待ち続ける。");
            System.out.println("  タイムアウトが短すぎると、正常なレスポンスを捨ててしまう。");

        } catch (IOException e) {
            // タイムアウト以外のネットワークエラー
            System.err.println("[エラー] ネットワークエラー: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== まとめ: HttpClient の重要ポイント ===");
        System.out.println("  1. HttpClient は1回だけ作って使い回す（コネクションプールを有効活用）");
        System.out.println("  2. HttpRequest は不変（イミュータブル）なので複数スレッドで共有できる");
        System.out.println("  3. send() = 同期（シンプル）/ sendAsync() = 非同期（高スループット）");
        System.out.println("  4. タイムアウトは connectTimeout（接続）と timeout（リクエスト）の2段階");
        System.out.println("  5. Vert.x の WebClient はこれと同じ概念をリアクティブに拡張した設計");
    }
}
