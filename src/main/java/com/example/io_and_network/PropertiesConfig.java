/**
 * 【なぜこのコードを学ぶのか】
 * DBの接続先・ポート番号・タイムアウト値などをソースコードに直書きすると、
 * 値を変えるたびに再コンパイルが必要になる。
 * .properties ファイルに設定を外出しすることで
 * 「コードを変えずに動作を変える」仕組みを体験する。
 * 「誰がファイルを作るのか」「どこに置くのか」という現場の疑問にも答える。
 */
package com.example.io_and_network;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;

public class PropertiesConfig {

    public static void main(String[] args) throws IOException {

        // ---------------------------------------------------------
        // 1. Before: ハードコードの問題
        // ---------------------------------------------------------
        System.out.println("=== 1. Before: 設定値をソースコードに直書きする問題 ===");
        System.out.println();

        // ========== Before: ハードコードされた設定値 ==========
        // 接続先・ポート・タイムアウトをソースコードに埋め込んでいる
        String host      = "localhost"; // DBホスト名
        int    port      = 5432;        // DBポート番号
        int    timeoutMs = 3000;        // タイムアウト（ミリ秒）

        System.out.println("[Before] ソースコードに直書きした設定値:");
        System.out.printf("  %-20s = %s%n", "host",      host);
        System.out.printf("  %-20s = %d%n", "port",      port);
        System.out.printf("  %-20s = %d ms%n", "timeoutMs", timeoutMs);
        System.out.println();

        // ハードコードの問題点を3つ示す
        System.out.println("問題点:");
        System.out.println("  → 問題1: 値を変えるたびにソースコードを修正して再コンパイルが必要");
        System.out.println("  → 問題2: 本番・開発・テストで設定が異なるとき、コードの書き換え忘れが起きやすい");
        System.out.println("  → 問題3: 接続先などの機密情報がソースコードに混入しリポジトリに残る");
        System.out.println();

        // ---------------------------------------------------------
        // 2. .propertiesファイルのフォーマット説明
        // ---------------------------------------------------------
        System.out.println("=== 2. .propertiesファイルのフォーマット ===");
        System.out.println();

        System.out.println("  # はコメント行（実行時に無視される）");
        System.out.println("  key=value 形式で設定を記述する");
        System.out.println("  空白行は無視される");
        System.out.println("  値に = や # が含まれる場合はバックスラッシュでエスケープする（例: url=jdbc\\:postgresql\\://...）");
        System.out.println();
        System.out.println("  [サンプル]");
        System.out.println("  # データベース設定");
        System.out.println("  db.host=localhost");
        System.out.println("  db.port=5432");
        System.out.println("  db.timeout.ms=3000");
        System.out.println();

        // ---------------------------------------------------------
        // 3. After: Properties.store() でファイルを生成する
        // ---------------------------------------------------------
        System.out.println("=== 3. After: Properties.store() で設定ファイルを書き出す ===");
        System.out.println();

        String propertiesFilePath = "tmp_app.properties";

        // Properties オブジェクトに設定値をセットする
        Properties props = new Properties();
        props.setProperty("db.host",       "localhost");
        props.setProperty("db.port",       "5432");
        props.setProperty("db.timeout.ms", "3000");
        props.setProperty("app.name",      "学習アプリ");
        props.setProperty("app.version",   "1.0.0");

        // ファイルに書き出す
        // [Java 7 不可] new FileWriter(path, charset) は Java 11 以降。
        //   Java 7 では new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8) を使う:
        //   try (OutputStreamWriter writer = new OutputStreamWriter(
        //           new FileOutputStream(propertiesFilePath), StandardCharsets.UTF_8)) {
        //       props.store(writer, "アプリケーション設定");
        //   }
        try (FileWriter writer = new FileWriter(propertiesFilePath, StandardCharsets.UTF_8)) {
            // store() の第2引数はファイル先頭に書き込まれるコメント（# 付きで出力される）
            // store() はコメントの後にタイムスタンプ行（#Mon May 04 ...）も自動で追加する
            props.store(writer, "アプリケーション設定");
        }

        System.out.println(".properties ファイルを書き出しました: " + propertiesFilePath);
        System.out.println();

        // 書き出したファイルの内容をそのまま表示する（ファイルの構造を確認するため）
        // [Java 7 不可] Files.readString() は Java 11 以降。Java 7 では BufferedReader を使う
        String rawContent = Files.readString(Path.of(propertiesFilePath), StandardCharsets.UTF_8);
        System.out.println("[ファイルの内容（生テキスト）]");
        System.out.println(rawContent);

        // ---------------------------------------------------------
        // 4. Properties.load() と getProperty() での読み込み
        // ---------------------------------------------------------
        System.out.println("=== 4. Properties.load() でファイルを読み込む ===");
        System.out.println();

        Properties loadedProps = new Properties();

        // [Java 7 不可] new FileReader(path, charset) は Java 11 以降。
        //   Java 7 では new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8) を使う:
        //   try (InputStreamReader reader = new InputStreamReader(
        //           new FileInputStream(propertiesFilePath), StandardCharsets.UTF_8)) {
        //       loadedProps.load(reader);
        //   }
        try (FileReader reader = new FileReader(propertiesFilePath, StandardCharsets.UTF_8)) {
            loadedProps.load(reader);
        }

        System.out.println("読み込んだ設定値:");
        System.out.printf("  %-20s = %s%n", "db.host",       loadedProps.getProperty("db.host"));
        System.out.printf("  %-20s = %s%n", "db.port",       loadedProps.getProperty("db.port"));
        System.out.printf("  %-20s = %s%n", "db.timeout.ms", loadedProps.getProperty("db.timeout.ms"));
        System.out.printf("  %-20s = %s%n", "app.name",      loadedProps.getProperty("app.name"));
        System.out.printf("  %-20s = %s%n", "app.version",   loadedProps.getProperty("app.version"));
        System.out.println();

        // getProperty(key, defaultValue): キーが存在しない場合はデフォルト値を返す
        // → ファイルに "db.password" を書いていないため "(未設定)" が返る
        String dbPassword = loadedProps.getProperty("db.password", "(未設定)");
        System.out.printf("  %-20s = %s%n", "db.password", dbPassword);
        System.out.println("  ↑ キーが存在しないときは getProperty(key, デフォルト値) でデフォルト値を返せる");
        System.out.println();

        // ---------------------------------------------------------
        // 5. setProperty() + store() で値を動的に追加・書き戻し
        // ---------------------------------------------------------
        System.out.println("=== 5. setProperty() + store() で値を追加して書き戻す ===");
        System.out.println();

        // 起動日時を動的に追加する（ログ用途・デバッグ用途で現場でよく使うパターン）
        // [Java 7 不可] LocalDateTime は Java 8 以降の java.time API。
        //   Java 7 では new java.util.Date().toString() を使う
        String launchedAt = LocalDateTime.now().toString();
        loadedProps.setProperty("app.launched.at", launchedAt);

        // 変更した Properties をファイルに書き戻す
        try (FileWriter writer = new FileWriter(propertiesFilePath, StandardCharsets.UTF_8)) {
            loadedProps.store(writer, "アプリケーション設定（起動日時を追加）");
        }

        // 書き戻したファイルを再読み込みして新しいキーが反映されたことを確認する
        Properties reloadedProps = new Properties();
        try (FileReader reader = new FileReader(propertiesFilePath, StandardCharsets.UTF_8)) {
            reloadedProps.load(reader);
        }

        System.out.println("追加したキー: app.launched.at");
        System.out.printf("  %-20s = %s%n", "app.launched.at", loadedProps.getProperty("app.launched.at"));
        System.out.println();
        System.out.println("→ setProperty() → store() → load() のサイクルで");
        System.out.println("  「コードを変えずに設定を更新する」読み書きの仕組みが完成する");
        System.out.println();

        // ---------------------------------------------------------
        // 6. まとめ
        // ---------------------------------------------------------
        System.out.println("=== 6. まとめ ===");
        System.out.println();

        System.out.println("  手法                    説明");
        System.out.println("  ----------------------  -------------------------------------------------------");
        System.out.printf("  %-22s  %s%n", "ハードコード",        "変更のたびに再コンパイルが必要");
        System.out.printf("  %-22s  %s%n", ".properties",        "key=value 形式でコードから設定を分離");
        System.out.printf("  %-22s  %s%n", "load(Reader)",       "ファイルから Properties に読み込む");
        System.out.printf("  %-22s  %s%n", "getProperty()",      "キーで値を取得、第2引数でデフォルト値");
        System.out.printf("  %-22s  %s%n", "store(Writer)",      "Properties の内容をファイルに書き出す");
        System.out.println();
        System.out.println("  [現場での管理方法]");
        System.out.println("  1. 開発者が app.properties.example（ダミー値入り）をリポジトリで管理する");
        System.out.println("  2. 本番チームや CI/CD が実際の値を記入した app.properties を作成する");
        System.out.println("  3. .properties ファイルは .gitignore に追加して機密情報をリポジトリに残さない");
        System.out.println();

        // ---------------------------------------------------------
        // 7. 後片付け: 一時ファイルを削除する
        // ---------------------------------------------------------
        System.out.println("=== 7. 後片付け ===");
        System.out.println();

        // [Java 7 不可] Files.deleteIfExists() は Java 7 の NIO.2 で追加された（Java 7 から使える）。
        //   Java 6 以前では new File(propertiesFilePath).delete() を使う。
        Files.deleteIfExists(Path.of(propertiesFilePath));
        System.out.println("一時ファイルを削除しました: " + propertiesFilePath);
    }
}
