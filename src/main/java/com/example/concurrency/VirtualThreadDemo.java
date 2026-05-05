/**
 * 【なぜこのコードを学ぶのか】
 * Java 21 で正式導入された仮想スレッドは、従来のプラットフォームスレッド（OS スレッドに1対1対応）
 * よりも遥かに軽量で、数千〜数万スレッドを作っても問題ない。
 * I/O バウンドなサーバーサイドアプリ（Web API, DB アクセス）で圧倒的な同時接続性能を実現する。
 * Vert.x 4.5 以降では仮想スレッド上で Verticle を動かせるため、
 * 将来の現場で使うことになる最新技術を今のうちに体験しておく。
 */
package com.example.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreadDemo {

    // 注意: このプログラムはマルチスレッドのため、実行するたびに結果が変わることがあります。
    // これはバグではなく、スレッドの実行順序が不定であることを体験するためのサンプルです。
    public static void main(String[] args) throws InterruptedException {

        System.out.println("================================================");
        System.out.println("  VirtualThreadDemo: 仮想スレッド（Java 21）");
        System.out.println("================================================");
        System.out.println();

        // ---------------------------------------------------------
        // section1: プラットフォームスレッド vs 仮想スレッドの起動方法を比較する
        // ---------------------------------------------------------
        section1ThreadCreation();

        // ---------------------------------------------------------
        // section2: 1000スレッドを起動して処理時間を比較する
        // ---------------------------------------------------------
        section2BenchmarkComparison();

        // ---------------------------------------------------------
        // section3: Executors.newVirtualThreadPerTaskExecutor() を使う
        // ---------------------------------------------------------
        section3VirtualThreadExecutor();

        // ---------------------------------------------------------
        // section4: 仮想スレッドの注意点（コメントで解説）
        // ---------------------------------------------------------
        section4Caveats();
    }

    // ==========================================================
    // section1: プラットフォームスレッド vs 仮想スレッドの起動方法
    // ==========================================================
    private static void section1ThreadCreation() throws InterruptedException {

        System.out.println("--- section1: スレッドの種類と起動方法の違い ---");

        // ========== Before: プラットフォームスレッド（従来のスレッド） ==========
        // OS スレッドに1対1でマッピングされる。1スレッドあたり1〜2MBのスタックを消費する。
        // 数百〜数千スレッドが上限の目安（メモリとOS管理コストの制約）。

        System.out.println("--- Before: プラットフォームスレッド（OS スレッドに1対1対応） ---");

        // [Java 7 不可] ラムダ式は Java 8 以降
        Thread platformThread = new Thread(() -> {
            System.out.println("  プラットフォームスレッドが動いている");
            System.out.println("  スレッド名: " + Thread.currentThread().getName());
            System.out.println("  仮想スレッドか: " + Thread.currentThread().isVirtual());
        });
        platformThread.start();
        platformThread.join();

        System.out.println();

        // ========== After: 仮想スレッド（Java 21 の新機能） ==========
        // JVM が管理する軽量スレッド。1スレッドあたり数KBのメモリしか消費しない。
        // 数万〜数百万スレッドを作っても問題ない（I/O 待ち中は JVM がスレッドを一時停止して再利用）。

        System.out.println("--- After: 仮想スレッド（Java 21 以降） ---");

        // [Java 7 不可] Thread.ofVirtual() は Java 21 以降。Java 7〜20 では使えない。
        //   Java 20 以前では Executors.newVirtualThreadPerTaskExecutor() も使えない。
        Thread virtualThread = Thread.ofVirtual().start(() -> {
            System.out.println("  仮想スレッドが動いている");
            System.out.println("  スレッド名: " + Thread.currentThread().getName());
            System.out.println("  仮想スレッドか: " + Thread.currentThread().isVirtual());
        });
        virtualThread.join();

        System.out.println();

        // スレッドに名前を付けて起動する方法
        // [Java 7 不可] Thread.ofVirtual().name() は Java 21 以降
        Thread namedVirtualThread = Thread.ofVirtual()
                .name("my-virtual-thread-1")
                .start(() -> {
                    System.out.println("  名前付き仮想スレッド: " + Thread.currentThread().getName());
                });
        namedVirtualThread.join();

        System.out.println();
    }

    // ==========================================================
    // section2: 1000スレッドを起動して処理時間を計測・比較する
    // ==========================================================
    private static void section2BenchmarkComparison() throws InterruptedException {

        System.out.println("--- section2: プラットフォームスレッド vs 仮想スレッド（1000本起動） ---");
        System.out.println("各スレッドは Thread.sleep(10) で I/O 待ちをシミュレーションする");
        System.out.println();

        int threadCount = 1000;

        // --- プラットフォームスレッド 1000本 ---
        List<Thread> platformThreads = new ArrayList<>();
        AtomicInteger platformCount = new AtomicInteger(0);

        long platformStart = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            // [Java 7 不可] ラムダ式は Java 8 以降
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(10); // I/O 待ちのシミュレーション（10ms）
                    platformCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            platformThreads.add(t);
            t.start();
        }

        for (Thread t : platformThreads) {
            t.join();
        }

        long platformElapsed = System.nanoTime() - platformStart;
        System.out.println("プラットフォームスレッド 1000本:");
        System.out.println("  完了スレッド数: " + platformCount.get());
        System.out.println("  処理時間: " + (platformElapsed / 1_000_000) + " ms");
        System.out.println();

        // --- 仮想スレッド 1000本 ---
        List<Thread> virtualThreads = new ArrayList<>();
        AtomicInteger virtualCount = new AtomicInteger(0);

        long virtualStart = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            // [Java 7 不可] Thread.ofVirtual() は Java 21 以降
            // [Java 7 不可] ラムダ式は Java 8 以降
            Thread t = Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(10); // I/O 待ちのシミュレーション（10ms）
                    virtualCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            virtualThreads.add(t);
        }

        for (Thread t : virtualThreads) {
            t.join();
        }

        long virtualElapsed = System.nanoTime() - virtualStart;
        System.out.println("仮想スレッド 1000本 [Java 21 以降]:");
        System.out.println("  完了スレッド数: " + virtualCount.get());
        System.out.println("  処理時間: " + (virtualElapsed / 1_000_000) + " ms");
        System.out.println();
        System.out.println("→ 仮想スレッドは I/O 待ちが多いほど有利（sleep 中は JVM がスレッドを一時退避）");
        System.out.println("  ※ 環境によっては差が小さいこともある（マシンのコア数・JVM の最適化による）");
        System.out.println();
    }

    // ==========================================================
    // section3: Executors.newVirtualThreadPerTaskExecutor()
    // ==========================================================
    private static void section3VirtualThreadExecutor() throws InterruptedException {

        System.out.println("--- section3: Executors.newVirtualThreadPerTaskExecutor() ---");
        System.out.println("タスクごとに新しい仮想スレッドを割り当てる ExecutorService");
        System.out.println("Vert.x 4.5 以降の Virtual Thread Verticle もこの仕組みを使って Verticle を動かす");
        System.out.println();

        // [Java 7 不可] Executors.newVirtualThreadPerTaskExecutor() は Java 21 以降。
        //   Java 20 以前では使えない。代替: Executors.newFixedThreadPool() (Java 5 以降)
        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

        AtomicInteger completedCount = new AtomicInteger(0);
        int taskCount = 100;

        for (int i = 1; i <= taskCount; i++) {
            // [Java 7 不可] ラムダ式は Java 8 以降
            virtualExecutor.submit(() -> {
                try {
                    Thread.sleep(5); // 短い I/O 待ちのシミュレーション
                    int count = completedCount.incrementAndGet();
                    if (count % 20 == 0) {
                        // 20タスクごとに進捗を出力する
                        System.out.println("  " + count + " / " + taskCount + " タスク完了"
                                + " (仮想スレッドか: " + Thread.currentThread().isVirtual() + ")");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        virtualExecutor.shutdown();
        boolean finished = virtualExecutor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("全 " + taskCount + " タスク完了: " + finished);
        System.out.println();
        System.out.println("newVirtualThreadPerTaskExecutor の特徴:");
        System.out.println("  ・タスクごとに新しい仮想スレッドを使う（スレッドプールではない）");
        System.out.println("  ・仮想スレッドはプラットフォームスレッドより生成コストが低い");
        System.out.println("  ・I/O 待ち中に JVM がスレッドを一時退避し、別タスクに CPU を回す");
        System.out.println();
    }

    // ==========================================================
    // section4: 仮想スレッドの注意点
    // ==========================================================
    private static void section4Caveats() {

        System.out.println("--- section4: 仮想スレッドを使う上での注意点 ---");
        System.out.println();

        System.out.println("【注意1】CPU バウンドなタスクには効果が薄い");
        System.out.println("  仮想スレッドが真価を発揮するのは「I/O 待ち（DB クエリ・HTTP リクエスト・ファイル読み書き）」。");
        System.out.println("  CPU をひたすら使う計算処理（暗号化・動画エンコード等）には効果が薄い。");
        System.out.println("  CPU バウンドなタスクは ForkJoinPool や並列ストリームを検討する。");
        System.out.println();

        System.out.println("【注意2】synchronized ブロックとのピンニング問題（Java 21 時点）");
        System.out.println("  仮想スレッドが synchronized ブロック内で I/O 待ちをすると、");
        System.out.println("  JVM がスレッドを退避できず、プラットフォームスレッドを占有し続ける（ピンニング）。");
        System.out.println("  → synchronized の代わりに ReentrantLock を使うと回避できる。");
        System.out.println("  → Java 24 以降ではこの制限が緩和される予定（JEP 491）。");
        System.out.println();

        System.out.println("【注意3】ThreadLocal のキャッシュ用途は要注意");
        System.out.println("  仮想スレッドは大量に作られるため、ThreadLocal に大きなオブジェクトをキャッシュすると");
        System.out.println("  スレッドごとにオブジェクトが作られてメモリを大量消費する。");
        System.out.println("  Java 20 以降の ScopedValue（JEP 429）が代替として推奨される。");
        System.out.println();

        System.out.println("【注意4】デバッグ・監視ツールの対応確認");
        System.out.println("  仮想スレッドは JDK Flight Recorder / JVM Tool Interface で可視化できる。");
        System.out.println("  一部の APM ツール（古いバージョン）は仮想スレッドを正しく表示できない場合がある。");
        System.out.println();

        System.out.println("=== 第12章のまとめ ===");
        System.out.println("  Thread 基本     : extends Thread / Runnable ラムダ / synchronized / AtomicInteger");
        System.out.println("  デッドロック     : ロック順序を統一して回避 / tryLock でタイムアウト検出");
        System.out.println("  ExecutorService  : スレッドプールでリソースを制御 / Future で結果を受け取る");
        System.out.println("  CompletableFuture: 非同期処理をチェーンで記述（Java 8+）");
        System.out.println("  仮想スレッド     : 軽量・大量起動可能・I/O バウンドに最適（Java 21+）");
    }
}
