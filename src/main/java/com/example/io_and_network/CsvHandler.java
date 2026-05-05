/**
 * 【なぜこのコードを学ぶのか】
 * CSV（Comma-Separated Values）は現場で最もよく扱うファイル形式。
 * DBエクスポート・スプレッドシート連携・バッチ処理の入出力として日常的に登場する。
 * 「カンマで split するだけ」では値の中にカンマや改行が含まれる場合に壊れる—
 * RFC 4180 のクォートルールを実装して、現場で通用するCSV処理の基礎を学ぶ。
 */
package com.example.io_and_network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvHandler {

    // ---------------------------------------------------------
    // 商品を表すレコード（インナータイプとして定義）
    // ---------------------------------------------------------
    // [Java 7 不可] record は Java 16 以降。
    //   Java 7 では private final フィールド + コンストラクタ + getter で代替:
    //   private static final class Product {
    //       private final String name; private final int price; private final String category;
    //       public Product(String name, int price, String category) { ... }
    //       public String getName() { return name; } ...
    //   }
    private record Product(String name, int price, String category) {}

    // ---------------------------------------------------------
    // サンプルデータ（意図的にカンマを含む名前を入れる）
    // ---------------------------------------------------------
    // [Java 7 不可] List.of() は Java 9 以降。
    //   Java 7 では Arrays.asList() を使う:
    //   private static final List<Product> PRODUCTS = Arrays.asList(
    //       new Product("ノートPC", 150_000, "PC"), ...);
    private static final List<Product> PRODUCTS = List.of(
        new Product("ノートPC",         150_000, "PC"),
        new Product("マウス, ワイヤレス",  3_500, "周辺機器"), // カンマを含む名前（クォート必須）
        new Product("キーボード",          8_000, "周辺機器"),
        new Product("モニター",           45_000, "PC")
    );

    // ---------------------------------------------------------
    // CSV書き込みメソッド
    // ---------------------------------------------------------
    /**
     * 商品リストを CSV ファイルに書き込む。
     * RFC 4180 に従い、値にカンマや二重引用符が含まれる場合はクォートする。
     *
     * @param products 書き込む商品リスト
     * @param filePath 出力先ファイルパス
     */
    private static void writeCsv(List<Product> products, String filePath) throws IOException {
        // try-with-resources（Java 7）: ブロックを抜けると writer.close() が自動で呼ばれる
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath, StandardCharsets.UTF_8))) {

            // ヘッダー行を書く
            writer.write("name,price,category");
            writer.newLine();

            // 商品ごとに1行書く
            for (Product product : products) {
                // 各フィールドを CSV エスケープしてカンマでつなぐ
                String line = escapeCsvField(product.name())
                        + "," + escapeCsvField(String.valueOf(product.price()))
                        + "," + escapeCsvField(product.category());
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // ---------------------------------------------------------
    // CSV フィールドエスケープメソッド（RFC 4180）
    // ---------------------------------------------------------
    /**
     * 1つのフィールド値を RFC 4180 のルールに従ってエスケープする。
     * カンマ・二重引用符・改行が含まれる場合は二重引用符でくくる。
     * フィールド内の二重引用符は "" に置き換える。
     *
     * @param field エスケープするフィールド値
     * @return エスケープ済みの文字列
     */
    private static String escapeCsvField(String field) {
        // カンマ・二重引用符・改行のいずれかが含まれる場合はクォートが必要
        boolean needsQuote = field.contains(",")
                || field.contains("\"")
                || field.contains("\n")
                || field.contains("\r");

        if (!needsQuote) {
            // クォート不要ならそのまま返す
            return field;
        }

        // [Java 7 不可] String.replace() は Java 7 以前から使えるが念のため記載:
        // フィールド内の " を "" にエスケープしてから " で囲む
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    // ---------------------------------------------------------
    // CSV読み込みメソッド（RFC 4180 のクォート対応）
    // ---------------------------------------------------------
    /**
     * CSV ファイルを読み込み、各行をフィールドの配列として返す。
     * ヘッダー行は読み飛ばす。RFC 4180 のクォートルールに対応したステートマシンで解析する。
     *
     * @param filePath 読み込むファイルパス
     * @return 各行のフィールド配列のリスト（ヘッダーを除く）
     */
    private static List<String[]> readCsv(String filePath) throws IOException {
        List<String[]> result = new ArrayList<>();
        boolean isFirstLine = true; // ヘッダー行スキップ用フラグ

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // ヘッダー行は読み飛ばす
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // 1行をフィールド配列に分解して追加する
                result.add(parseCsvLine(line));
            }
        }
        return result;
    }

    // ---------------------------------------------------------
    // CSV 1行パースメソッド（ステートマシン方式）
    // ---------------------------------------------------------
    /**
     * CSV の1行をフィールド配列に分解する。
     * クォートの開始・終了・エスケープをステートマシンで処理する。
     *
     * @param line CSV の1行
     * @return フィールドの配列
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false; // 現在クォートの中にいるかどうか

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                // クォートの中にいる状態
                if (c == '"') {
                    // 次の文字も " ならエスケープされた " → フィールドに " を追加
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++; // 次の " をスキップ
                    } else {
                        // クォートの終わり
                        inQuotes = false;
                    }
                } else {
                    // クォート内の通常文字
                    currentField.append(c);
                }
            } else {
                // クォートの外にいる状態
                if (c == '"') {
                    // クォートの始まり
                    inQuotes = true;
                } else if (c == ',') {
                    // フィールドの区切り
                    fields.add(currentField.toString());
                    currentField.setLength(0); // StringBuilder をリセット
                } else {
                    // 通常文字
                    currentField.append(c);
                }
            }
        }

        // 最後のフィールドを追加する
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    // ---------------------------------------------------------
    // メインメソッド
    // ---------------------------------------------------------
    public static void main(String[] args) throws IOException {

        String csvFilePath = "tmp_products.csv";

        // ---------------------------------------------------------
        // 1. サンプルデータを CSV ファイルに書き込む
        // ---------------------------------------------------------
        System.out.println("=== 1. CSV ファイルへの書き込み ===");
        System.out.println();
        writeCsv(PRODUCTS, csvFilePath);
        System.out.println("CSV ファイルを書き込みました: " + csvFilePath);
        System.out.println();

        // ---------------------------------------------------------
        // 2. 書き込んだ CSV ファイルの内容をそのまま表示（cat 相当）
        // ---------------------------------------------------------
        System.out.println("=== 2. CSV ファイルの内容（生テキスト） ===");
        System.out.println();
        // [Java 7 不可] Files.readString() は Java 11 以降。Java 7 では BufferedReader を使う
        String rawContent = Files.readString(Path.of(csvFilePath), StandardCharsets.UTF_8);
        System.out.println(rawContent);

        // ---------------------------------------------------------
        // 3. CSV ファイルを読み込んで商品一覧を出力する
        // ---------------------------------------------------------
        System.out.println("=== 3. CSV ファイルを読み込んで商品一覧を表示 ===");
        System.out.println();

        List<String[]> rows = readCsv(csvFilePath);
        System.out.printf("%-25s %10s  %-10s%n", "商品名", "価格(円)", "カテゴリ");
        System.out.println("-".repeat(50));
        for (String[] row : rows) {
            // row[0]=name, row[1]=price, row[2]=category
            System.out.printf("%-25s %10s  %-10s%n", row[0], row[1], row[2]);
        }
        System.out.println();

        // ---------------------------------------------------------
        // 4. Before セクション: split(",") アンチパターンを実演
        // ---------------------------------------------------------
        System.out.println("=== 4. Before: split(\",\") だけでは値の中のカンマに対応できない ===");
        System.out.println();

        // [アンチパターン] 値の中にカンマが含まれると正しく分割できない
        String badLine = "\"マウス, ワイヤレス\",3500,周辺機器";
        String[] cols = badLine.split(",");
        // → cols[0]="\"マウス"  cols[1]=" ワイヤレス\""  cols[2]="3500"  cols[3]="周辺機器"
        //   本来 3 フィールドのはずが 4 フィールドに壊れる
        System.out.println("[アンチパターン] 対象行: " + badLine);
        System.out.println("[アンチパターン] split(\",\") のフィールド数: " + cols.length + " （正しくは 3 フィールド）");
        for (int i = 0; i < cols.length; i++) {
            System.out.println("  cols[" + i + "] = \"" + cols[i] + "\"");
        }
        System.out.println("→ カンマを含む \"マウス, ワイヤレス\" が2つのフィールドに分断されてしまう");
        System.out.println();

        System.out.println("[正しい解析] parseCsvLine() を使った結果:");
        String[] correctFields = parseCsvLine(badLine);
        System.out.println("  結果: " + Arrays.toString(correctFields));
        System.out.println("→ クォートを考慮することで正しく3フィールドに分割できる");
        System.out.println();

        // ---------------------------------------------------------
        // 補足: JSON / XML の扱い（コード実行はしない）
        // ---------------------------------------------------------
        // ========== 補足: JSON / XML の扱い ==========
        // 現場では CSV 以外に JSON・XML もよく登場する。
        //
        // JSON: com.fasterxml.jackson.databind.ObjectMapper や Gson が標準的。
        //       外部ライブラリなしで扱う場合は org.json や手書きパーサを使う。
        //       Java 標準ライブラリには JSON パーサは含まれない（Java 21 現在）。
        //
        // XML:  javax.xml.parsers.DocumentBuilder（Java 標準）で DOM として読める。
        //       本番では JAXB（Java Architecture for XML Binding）が一般的。
        //
        // ポイント: CSV・JSON・XML いずれも「ファイルから読む」基本はここで学んだ
        //           BufferedReader / Files.readAllLines() が起点になる。

        System.out.println("=== 補足: 現場でよく使う他のフォーマット ===");
        System.out.println("JSON: Jackson / Gson 等の外部ライブラリが標準的（Java 標準には JSON パーサなし）");
        System.out.println("XML:  javax.xml.parsers.DocumentBuilder（Java 標準）で読める");
        System.out.println("いずれも BufferedReader / Files.readAllLines() が基礎になる");
        System.out.println();

        // ---------------------------------------------------------
        // 5. 後片付け: 一時ファイルを削除する
        // ---------------------------------------------------------
        // [Java 7 不可] Files.deleteIfExists() は Java 7 以降（NIO.2）で使える。
        //   Java 6 以前では new File(csvFilePath).delete() を使う。
        Files.deleteIfExists(Path.of(csvFilePath));
        System.out.println("一時ファイルを削除しました: " + csvFilePath);
    }
}
