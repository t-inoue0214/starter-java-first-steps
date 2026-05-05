/**
 * 【なぜこのコードを学ぶのか】
 * System.out.println() はログではない。レベルで絞り込めず、ファイルに書けず、
 * タイムスタンプもスレッド名も付かない。本番では「何が起きたか」を後から追える
 * ロギングが必須だ。java.util.logging（JUL）を使って標準ライブラリだけで
 * ログレベル・ファイル出力・フォーマット制御を体験する。
 */
package com.example.io_and_network;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggingBasics {

    // ---------------------------------------------------------
    // OrderService: Logger を使うサービスクラス（内部クラス）
    // ---------------------------------------------------------
    // 実際の現場では、ビジネスロジックのクラスが Logger を持つ。
    // クラス名を Logger 名に使う（Logger.getLogger(getClass().getName())）のが慣習。
    private static class OrderService {

        private final Logger logger;

        OrderService(Logger logger) {
            this.logger = logger;
        }

        /** 商品を在庫に追加する */
        void addProduct(String name, int quantity) {
            // FINE: デバッグ詳細（本番では非表示、開発時に確認する）
            logger.fine("在庫チェック開始: 商品=" + name + ", 追加数=" + quantity);

            if (quantity <= 0) {
                // WARNING: 問題はあるが処理を継続できる場合
                logger.warning("追加数が不正: " + name + " quantity=" + quantity);
                return;
            }

            // INFO: 通常の業務イベント（本番でも表示する）
            logger.info("商品を追加しました: " + name + "（在庫: " + quantity + "個）");
        }

        /** 注文を処理する */
        void processOrder(String name, int requested, int stock) {
            // FINE: デバッグ詳細
            logger.fine("注文処理開始: 商品=" + name + ", 要求=" + requested + ", 在庫=" + stock);

            if (stock < 0) {
                // SEVERE: アプリを止めるようなバグ・異常状態
                logger.severe("バグ検出: 在庫数が負の値です（在庫=" + stock + "）。在庫管理コードを確認してください");
                return;
            }
            if (stock < requested) {
                // WARNING: 在庫不足（回復可能な問題）
                logger.warning("在庫不足: " + name + " 要求=" + requested + ", 在庫=" + stock);
                return;
            }

            // INFO: 注文受付成功
            logger.info("注文を受け付けました: " + name + " × " + requested + "（在庫残: " + (stock - requested) + "個）");
        }
    }

    // ---------------------------------------------------------
    // Logger セットアップメソッド（ハンドラを設定する）
    // ---------------------------------------------------------
    private static FileHandler setupLogger(Logger logger, String logFilePath,
                                           Level consoleLevel) throws IOException {

        // ルートロガーへの重複出力を防ぐ
        // java.util.logging はデフォルトでルートロガーにも出力を渡す（親ロガーへの伝播）
        // false にすることでこの Logger だけの設定で動くようにする
        logger.setUseParentHandlers(false);

        // Logger 自体はすべてのレベルを受け取る（フィルタリングはハンドラに任せる）
        logger.setLevel(Level.ALL);

        // ConsoleHandler: 指定レベル以上をコンソールに出力する
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(consoleLevel);
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);

        // FileHandler: すべてのレベル（FINE以上）をファイルに記録する
        // → コンソールでは見えないデバッグ情報（FINE）もファイルには残る
        FileHandler fileHandler = new FileHandler(logFilePath);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        return fileHandler; // close() を呼ぶために返す
    }

    // ---------------------------------------------------------
    // メインメソッド
    // ---------------------------------------------------------
    public static void main(String[] args) throws IOException {

        String logFilePath = "tmp_app.log";

        // ---------------------------------------------------------
        // 1. Before: System.out.println のアンチパターン
        // ---------------------------------------------------------
        System.out.println("=== 1. Before: System.out.println によるデバッグ出力（アンチパターン） ===");
        System.out.println();

        // [アンチパターン] System.out.println でログを代替する
        System.out.println("[情報] 商品を追加しました: ノートPC");
        System.out.println("[デバッグ] 在庫チェック実行: 現在の在庫=10");
        System.out.println("[エラー] 在庫不足: 要求=15, 在庫=10");
        System.out.println();
        System.out.println("問題点:");
        System.out.println("  → 問題1: レベルで絞り込めない（デバッグ行も本番に混ざる）");
        System.out.println("  → 問題2: ファイルに書くにはリダイレクト（> app.log）に頼るしかない");
        System.out.println("  → 問題3: タイムスタンプ・スレッド名が自動では付かない");
        System.out.println("  → 問題4: 本番でデバッグ出力を消すにはコードを修正して再コンパイルが必要");
        System.out.println();

        // ---------------------------------------------------------
        // 2. ログレベルの一覧（概念説明）
        // ---------------------------------------------------------
        System.out.println("=== 2. ログレベル（重大度の高い順）===");
        System.out.println();
        System.out.printf("  %-10s %s%n", "SEVERE",  ": アプリを止めるエラー（DB接続不可、予期しない例外）");
        System.out.printf("  %-10s %s%n", "WARNING", ": 問題はあるが回復できる（リトライ成功、在庫不足など）");
        System.out.printf("  %-10s %s%n", "INFO",    ": 通常の業務イベント（注文受付、ログインなど）★本番で表示");
        System.out.printf("  %-10s %s%n", "CONFIG",  ": 設定値の出力（起動時のポート番号など）");
        System.out.printf("  %-10s %s%n", "FINE",    ": デバッグ詳細（本番では非表示、開発時に確認する）");
        System.out.printf("  %-10s %s%n", "FINER",   ": さらに詳細なデバッグ情報");
        System.out.printf("  %-10s %s%n", "FINEST",  ": 最も詳細（ライブラリの内部トレースなど）");
        System.out.println();

        // ---------------------------------------------------------
        // 3. After: java.util.logging のセットアップ
        // ---------------------------------------------------------
        System.out.println("=== 3. After: java.util.logging セットアップ ===");
        System.out.println();
        System.out.println("ログ出力先: コンソール（INFO以上）+ ファイル（" + logFilePath + "、FINE以上）");
        System.out.println();

        Logger logger = Logger.getLogger(LoggingBasics.class.getName());
        FileHandler fileHandler = setupLogger(logger, logFilePath, Level.INFO);

        // SimpleFormatter の出力フォーマットはシステムプロパティで変更できる:
        //   -Djava.util.logging.SimpleFormatter.format="%1$tF %1$tT %4$-7s %5$s%n"
        // デフォルトは詳細な複数行フォーマット（タイムスタンプ・クラス名・メソッド名）

        // ---------------------------------------------------------
        // 4. OrderService を使ったログ出力デモ
        // ---------------------------------------------------------
        System.out.println("=== 4. OrderService の操作（コンソールには INFO 以上が表示される） ===");
        System.out.println();

        OrderService service = new OrderService(logger);

        service.addProduct("ノートPC", 10);         // INFO: 正常追加
        service.processOrder("ノートPC",  3, 10);   // INFO: 注文成功（在庫残7）
        service.processOrder("ノートPC", 15, 10);   // WARNING: 在庫不足
        service.addProduct("マウス", -1);            // WARNING: 追加数不正
        service.processOrder("エラー商品", 1, -5);  // SEVERE: バグ検出
        System.out.println();

        // ---------------------------------------------------------
        // 5. コンソールレベルを WARNING に変更して INFO を隠す
        // ---------------------------------------------------------
        System.out.println("=== 5. コンソールレベルを WARNING に変更（INFO は表示されなくなる） ===");
        System.out.println();
        System.out.println("  ※ ファイル（" + logFilePath + "）には引き続きすべてのレベルが記録される");
        System.out.println();

        // ConsoleHandler のレベルを WARNING に上げる
        // logger.getHandlers()[0] は ConsoleHandler（setupLogger で最初に追加したもの）
        logger.getHandlers()[0].setLevel(Level.WARNING);

        service.addProduct("キーボード", 5);        // INFO: コンソール非表示、ファイルには記録
        service.processOrder("キーボード", 10, 3);  // WARNING: コンソールに表示
        System.out.println();
        System.out.println("  → INFO の「商品を追加しました: キーボード」はコンソールに表示されなかった");
        System.out.println("    （ファイルには記録されている）");
        System.out.println();

        // ---------------------------------------------------------
        // 6. ファイルの内容を表示する
        // ---------------------------------------------------------
        System.out.println("=== 6. " + logFilePath + " の内容（FINE 以上がすべて記録されている）===");
        System.out.println();

        // FileHandler を閉じてからファイルを読む（フラッシュ・ロック解放のため）
        fileHandler.close();

        // [Java 7 不可] Files.readString() は Java 11 以降。Java 7 では BufferedReader を使う
        String logContent = Files.readString(Path.of(logFilePath), StandardCharsets.UTF_8);
        // ログファイルは長くなるため最初の1000文字だけ表示する
        if (logContent.length() > 1000) {
            System.out.println(logContent.substring(0, 1000));
            System.out.println("... （以降省略）");
        } else {
            System.out.println(logContent);
        }

        // ---------------------------------------------------------
        // 7. logging.properties の説明（コードなし）
        // ---------------------------------------------------------
        System.out.println("=== 7. 本番での設定変更: logging.properties ===");
        System.out.println();
        System.out.println("  コードを変えずにログレベルを変えるには logging.properties を使う:");
        System.out.println();
        System.out.println("  # logging.properties の例");
        System.out.println("  com.example.io_and_network.level = FINE");
        System.out.println("  java.util.logging.ConsoleHandler.level = WARNING");
        System.out.println();
        System.out.println("  起動コマンド:");
        System.out.println("  java -Djava.util.logging.config.file=logging.properties \\");
        System.out.println("       -cp out/ com.example.io_and_network.LoggingBasics");
        System.out.println();
        System.out.println("  → 再コンパイル不要でログレベルを変更できる");
        System.out.println("     PropertiesConfig.java の「設定の外出し」と同じ考え方だ");
        System.out.println();

        // ---------------------------------------------------------
        // 8. まとめ
        // ---------------------------------------------------------
        System.out.println("=== 8. まとめ ===");
        System.out.println();
        System.out.printf("  %-24s  %s%n", "System.out.println()",  "ログではない。レベル制御・ファイル出力が不可");
        System.out.printf("  %-24s  %s%n", "logger.info()",         "タイムスタンプ・クラス名が自動付与される");
        System.out.printf("  %-24s  %s%n", "ログレベル",              "SEVERE > WARNING > INFO > CONFIG > FINE > ...");
        System.out.printf("  %-24s  %s%n", "ConsoleHandler",         "コンソールへの出力（本番: INFO 以上）");
        System.out.printf("  %-24s  %s%n", "FileHandler",            "ファイルへの出力（開発: FINE まで記録）");
        System.out.printf("  %-24s  %s%n", "setUseParentHandlers()", "false にしてルートへの二重出力を防ぐ");
        System.out.printf("  %-24s  %s%n", "logging.properties",     "コード変更なしでレベルを変更できる");
        System.out.println();

        // ---------------------------------------------------------
        // 9. 後片付け: 一時ファイルを削除する
        // ---------------------------------------------------------
        // FileHandler はすでに close() 済み。ログファイル本体とロックファイルを削除する
        Files.deleteIfExists(Path.of(logFilePath));
        Files.deleteIfExists(Path.of(logFilePath + ".lck")); // FileHandler のロックファイル
        System.out.println("一時ファイルを削除しました: " + logFilePath);
    }
}
