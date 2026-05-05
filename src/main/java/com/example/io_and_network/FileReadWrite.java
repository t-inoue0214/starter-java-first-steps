/**
 * 【なぜこのコードを学ぶのか】
 * ファイルI/Oは「プログラムの外にあるデータ」を扱う最初の一歩。
 * 第09章の System.nanoTime() で計測したコード処理との速度差を実測して
 * 「なぜファイル読み書きはCPU処理より何桁も遅いのか」を理解する。
 * try-with-resources でストリームを確実にクローズする習慣を身につける。
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
import java.util.List;

public class FileReadWrite {

    public static void main(String[] args) throws IOException {

        // 一時ファイルのパス（main メソッド末尾で削除する）
        String filePath = "tmp_filereadwrite.txt";

        // ---------------------------------------------------------
        // ========== Before: BufferedWriter / BufferedReader（Java 7 から使える） ==========
        // ---------------------------------------------------------
        // 歴史的に広く使われてきた書き方。冗長に見えるが、
        // 「ストリームを開いて→書いて→閉じる」の流れが明示されているため理解しやすい。

        System.out.println("=== Before: BufferedWriter / BufferedReader ===");
        System.out.println();

        // ファイルに3行書き込む
        // try-with-resources（Java 7）: ブロックを抜けると writer.close() が自動で呼ばれる
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write("1行目: Javaのファイル入出力");
            writer.newLine(); // 改行文字を書き込む（OS 依存の改行コードになる）
            writer.write("2行目: try-with-resources でクローズ忘れを防ぐ");
            writer.newLine();
            writer.write("3行目: BufferedWriter は書き込みをバッファリングして効率化する");
            writer.newLine();
        }
        System.out.println("ファイルへの書き込みが完了しました: " + filePath);
        System.out.println();

        // ファイルを1行ずつ読み込む
        System.out.println("--- ファイル読み込み（行番号付き） ---");
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 1;
            // readLine() はファイル末尾で null を返す
            while ((line = reader.readLine()) != null) {
                System.out.println(lineNumber + ": " + line);
                lineNumber++;
            }
        }
        System.out.println();

        // ---------------------------------------------------------
        // ========== After: Files.writeString() / Files.readString()（Java 11+） ==========
        // ---------------------------------------------------------
        // Java 11 以降ではファイル全体を1行のコードで読み書きできる。
        // 短いファイルなら After の方が圧倒的にシンプル。
        // 大きなファイルは全内容をメモリに乗せるため、After はファイルサイズに注意。

        System.out.println("=== After: Files.writeString() / Files.readString() ===");
        System.out.println();

        // [Java 7 不可] Files.writeString() は Java 11 以降。
        //   Java 7 では BufferedWriter を使う（Before を参照）。
        String content = "1行目: Javaのファイル入出力\n"
                + "2行目: try-with-resources でクローズ忘れを防ぐ\n"
                + "3行目: BufferedWriter は書き込みをバッファリングして効率化する\n";

        // [Java 7 不可] Path.of() は Java 11 以降。Java 7 では Paths.get() を使う:
        //   Path path = Paths.get("tmp_filereadwrite.txt");
        Files.writeString(Path.of(filePath), content, StandardCharsets.UTF_8);
        System.out.println("Files.writeString() で書き込み完了");

        // [Java 7 不可] Files.readString() は Java 11 以降。
        //   Java 7 では BufferedReader を使う（Before を参照）。
        String readContent = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        System.out.println("Files.readString() で読み込んだ内容:");
        System.out.println(readContent);

        // ---------------------------------------------------------
        // 全行読み込みセクション: Files.readAllLines()（Java 8+）
        // ---------------------------------------------------------
        // 各行を List<String> として受け取れる。for ループで処理しやすい。

        System.out.println("=== Files.readAllLines() で全行取得 ===");
        System.out.println();

        // [Java 7 不可] Files.readAllLines() は Java 8 以降。
        //   Java 7 では BufferedReader の while ループで List に add していく:
        //   List<String> lines = new ArrayList<>();
        //   try (BufferedReader r = new BufferedReader(new FileReader(filePath))) {
        //       String l; while ((l = r.readLine()) != null) { lines.add(l); } }
        List<String> lines = Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8);
        System.out.println("行数: " + lines.size());
        for (int i = 0; i < lines.size(); i++) {
            System.out.println((i + 1) + ": " + lines.get(i));
        }
        System.out.println();

        // ---------------------------------------------------------
        // 計測セクション: メモリ操作 vs ファイルI/O 速度比較
        // ---------------------------------------------------------
        // HDDやSSDはCPU・RAMより何桁も速度が劣る。
        // OS のディスクキャッシュがあってもカーネル呼び出しのオーバーヘッドがある。
        // この計測では「体感できる差」を数値で確認する。

        System.out.println("=== 速度比較: メモリ操作 vs ファイルI/O ===");
        System.out.println();

        // 1000行のサンプルデータを用意する
        StringBuilder sampleDataBuilder = new StringBuilder();
        for (int i = 1; i <= 1000; i++) {
            sampleDataBuilder.append("行").append(i).append(": サンプルデータ 0123456789 ABCDEFGHIJ\n");
        }
        String sampleData = sampleDataBuilder.toString();

        int trialCount = 100; // 計測を安定させるため100回繰り返す

        // --- メモリ上での文字列処理（×100回） ---
        long memoryStart = System.nanoTime();
        for (int i = 0; i < trialCount; i++) {
            // メモリ上でサンプルデータを StringBuilder に追記して行数を数えるだけ
            String[] memLines = sampleData.split("\n");
            int count = memLines.length; // 処理結果を参照して最適化除去を防ぐ
            if (count < 0) {
                System.out.println("ここには到達しない"); // dead-code 除去対策
            }
        }
        long memoryElapsed = System.nanoTime() - memoryStart;

        // --- ファイル書き込み + 読み込み（×100回） ---
        String benchFilePath = "tmp_filereadwrite_bench.txt";
        long fileStart = System.nanoTime();
        for (int i = 0; i < trialCount; i++) {
            // [Java 7 不可] Files.writeString() / readString() は Java 11 以降
            Files.writeString(Path.of(benchFilePath), sampleData, StandardCharsets.UTF_8);
            String readBack = Files.readString(Path.of(benchFilePath), StandardCharsets.UTF_8);
            if (readBack.isEmpty()) {
                System.out.println("ここには到達しない"); // dead-code 除去対策
            }
        }
        long fileElapsed = System.nanoTime() - fileStart;

        // ベンチ用一時ファイルを削除する
        Files.deleteIfExists(Path.of(benchFilePath));

        // ミリ秒単位に変換（1 ms = 1,000,000 ns）
        double memoryMs = memoryElapsed / 1_000_000.0;
        double fileMs   = fileElapsed   / 1_000_000.0;

        System.out.printf("メモリ操作 %d回: %.2f ms%n", trialCount, memoryMs);
        System.out.printf("ファイルI/O %d回: %.2f ms%n", trialCount, fileMs);

        // ゼロ除算を避けるため、メモリ計測が極端に速くて 0 に丸まった場合に備える
        if (memoryMs > 0.001) {
            double ratio = fileMs / memoryMs;
            System.out.printf("ファイルI/O はメモリの %.0f 倍遅い%n", ratio);
        } else {
            System.out.println("メモリ操作が速すぎて倍率を計測できませんでした（ファイルI/Oは明らかに遅い）");
        }

        System.out.println();
        System.out.println("[解説]");
        System.out.println("  HDDやSSDはCPU・RAMより何桁も速度が劣る。");
        System.out.println("  OSのディスクキャッシュが効いていても、カーネル呼び出しのオーバーヘッドがある。");
        System.out.println("  大量のファイルI/OはBufferedWriter/BufferedReaderでバッファリングすることで");
        System.out.println("  カーネル呼び出し回数を減らし、速度を改善できる。");
        System.out.println();

        // ---------------------------------------------------------
        // 後片付け: 一時ファイルを削除する
        // ---------------------------------------------------------
        Files.deleteIfExists(Path.of(filePath));
        System.out.println("一時ファイルを削除しました: " + filePath);
    }
}
