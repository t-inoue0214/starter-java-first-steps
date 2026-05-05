/**
 * 【なぜこのコードを学ぶのか】
 * 生の Thread を毎回 new して管理するのはリソース効率が悪く、スレッド数も制御できない。
 * アクセスが集中したとき無制限にスレッドが増えると、メモリ枯渇でサーバーが落ちる。
 * ExecutorService はスレッドプールを管理し、タスクを効率よく実行する仕組みを提供する。
 * Vert.x の Worker Verticle やワーカースレッドプールもこの仕組みの上に成り立っている。
 * Future / CompletableFuture で非同期処理の結果を受け取る方法も習得する。
 */
package com.example.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceDemo {

    // 注意: このプログラムはマルチスレッドのため、実行するたびに結果が変わることがあります。
    // これはバグではなく、スレッドの実行順序が不定であることを体験するためのサンプルです。
    public static void main(String[] args)
            throws InterruptedException, ExecutionException {

        System.out.println("============================================");
        System.out.println("  ExecutorServiceDemo: スレッドプールと非同期処理");
        System.out.println("============================================");
        System.out.println();

        // ---------------------------------------------------------
        // section1: Before = 生スレッドの辛さを体験する
        // ---------------------------------------------------------
        section1RawThreadProblem();

        // ---------------------------------------------------------
        // section2: After = ExecutorService（スレッドプール）を使う
        // ---------------------------------------------------------
        section2ExecutorService();

        // ---------------------------------------------------------
        // section3: Future で非同期処理の結果を受け取る
        // ---------------------------------------------------------
        section3Future();

        // ---------------------------------------------------------
        // section4: CompletableFuture で非同期処理をチェーンする
        // ---------------------------------------------------------
        section4CompletableFuture();
    }

    // ==========================================================
    // section1: Before = 生スレッドの辛さ
    // ==========================================================
    private static void section1RawThreadProblem() throws InterruptedException {

        System.out.println("--- section1: Before = 生スレッドを毎回 new する問題 ---");

        int taskCount = 5;
        List<Thread> threads = new ArrayList<>();

        long start = System.nanoTime();

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i + 1;
            // [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では Runnable の匿名クラスで書く:
            //   Thread t = new Thread(new Runnable() {
            //       @Override public void run() { /* ... */ }
            //   });
            Thread thread = new Thread(() -> {
                System.out.println("  タスク " + taskId + " を実行中 (スレッド: "
                        + Thread.currentThread().getName() + ")");
                try {
                    Thread.sleep(50); // 重い処理のシミュレーション（50ms）
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("  タスク " + taskId + " 完了");
            });
            threads.add(thread);
            thread.start(); // スレッドを開始するたびにOSがスレッドを生成するコストがかかる
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("処理時間: " + (elapsed / 1_000_000) + " ms");
        System.out.println();
        System.out.println("問題点:");
        System.out.println("  1. タスクのたびに新しいスレッドを生成する → OSリソースを毎回消費する");
        System.out.println("  2. 同時実行スレッド数に上限がない → アクセス集中でメモリ枯渇の危険");
        System.out.println("  3. 全スレッドを List で管理する必要がある → コードが複雑になる");
        System.out.println();
    }

    // ==========================================================
    // section2: After = ExecutorService（スレッドプール）
    // ==========================================================
    private static void section2ExecutorService() throws InterruptedException {

        System.out.println("--- section2: After = ExecutorService（スレッドプール） ---");

        // [Java 5 以降] Executors.newFixedThreadPool() は Java 5 以降。
        // 3スレッドのプールを作成する。タスクが10個あっても同時に動くスレッドは最大3本。
        // 残りのタスクはキューに入り、スレッドが空いたら順番に実行される（スレッドを再利用）。
        ExecutorService executor = Executors.newFixedThreadPool(3);

        System.out.println("スレッドプールサイズ: 3 / 投入タスク数: 10");

        long start = System.nanoTime();

        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            // [Java 7 不可] ラムダ式は Java 8 以降
            executor.submit(() -> {
                System.out.println("  タスク " + taskId + " を実行中 (スレッド: "
                        + Thread.currentThread().getName() + ")");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("  タスク " + taskId + " 完了");
            });
        }

        // 新しいタスクの受け付けを停止し、実行中・キュー内のタスクが終わるのを待つ
        executor.shutdown();

        // 最大10秒待つ（正常に終わらない場合はタイムアウト）
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

        long elapsed = System.nanoTime() - start;
        System.out.println("全タスク完了: " + finished + " / 処理時間: " + (elapsed / 1_000_000) + " ms");
        System.out.println();
        System.out.println("改善点:");
        System.out.println("  1. スレッドを3本だけ作り再利用する → OS スレッド生成コストが1回だけ");
        System.out.println("  2. 同時実行数を3本に制限 → サーバーのリソースを守れる");
        System.out.println("  3. タスクの投入・管理が executor.submit() だけで済む → コードがシンプル");
        System.out.println("  スレッド名が Thread-1,2,3 のどれかを繰り返していることに注目！（再利用の証拠）");
        System.out.println();
    }

    // ==========================================================
    // section3: Future で非同期処理の結果を受け取る
    // ==========================================================
    private static void section3Future() throws InterruptedException, ExecutionException {

        System.out.println("--- section3: Future で計算結果を受け取る ---");

        // [Java 5 以降] ExecutorService は Java 5 以降
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Callable<T> は結果を返せる Runnable。戻り値の型を型引数で指定する。
        // [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では Callable の匿名クラスで書く:
        //   Callable<Integer> task1 = new Callable<Integer>() {
        //       @Override public Integer call() throws Exception {
        //           Thread.sleep(200);
        //           return 42;
        //       }
        //   };
        Callable<Integer> task1 = () -> {
            System.out.println("  [task1] 重い計算を開始（200ms かかる）...");
            Thread.sleep(200);
            int result = 42; // 計算結果
            System.out.println("  [task1] 計算完了: " + result);
            return result;
        };

        // [Java 7 不可] ラムダ式は Java 8 以降
        Callable<Integer> task2 = () -> {
            System.out.println("  [task2] 重い計算を開始（150ms かかる）...");
            Thread.sleep(150);
            int result = 100;
            System.out.println("  [task2] 計算完了: " + result);
            return result;
        };

        // submit() で Callable を投入すると、Future が即座に返る
        // （まだ結果はないが、「将来の結果への参照」を受け取れる）
        Future<Integer> future1 = executor.submit(task1);
        Future<Integer> future2 = executor.submit(task2);

        System.out.println("  タスクを投入した。メインスレッドは処理を続けながら待てる...");

        // future.get() で結果を受け取る（計算が終わるまでここでブロッキング待機する）
        int result1 = future1.get(); // task1 が終わるまで待つ
        int result2 = future2.get(); // task2 が終わるまで待つ

        System.out.println("  result1 + result2 = " + (result1 + result2));

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println();
        System.out.println("Future の特徴:");
        System.out.println("  ・submit() はすぐに返る（タスクはバックグラウンドで動く）");
        System.out.println("  ・get() で結果を取得する（結果が出るまでブロッキング待機）");
        System.out.println("  ・get() にタイムアウトを指定することもできる: future.get(5, TimeUnit.SECONDS)");
        System.out.println();
    }

    // ==========================================================
    // section4: CompletableFuture で非同期処理をチェーンする
    // ==========================================================
    private static void section4CompletableFuture() throws InterruptedException {

        System.out.println("--- section4: CompletableFuture で非同期処理をチェーンする ---");
        System.out.println("  CompletableFuture は Future の進化版（Java 8 以降）");
        System.out.println("  .thenApply() / .thenAccept() でコールバックを繋げられる");
        System.out.println();

        // [Java 7 不可] CompletableFuture は Java 8 以降。Java 7 では Future + ExecutorService で代替。

        // supplyAsync: 別スレッドで処理を実行し、CompletableFuture を返す
        // [Java 7 不可] ラムダ式・CompletableFuture ともに Java 8 以降
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("  [非同期処理1] 重い計算を開始... (スレッド: "
                    + Thread.currentThread().getName() + ")");
            try {
                Thread.sleep(200); // 重い計算のシミュレーション
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            int result = 21;
            System.out.println("  [非同期処理1] 計算完了: " + result);
            return result;
        })
        // thenApply: 前の処理が終わったら結果を変換する（同期的に別スレッドで続けて実行される）
        // [Java 7 不可] ラムダ式は Java 8 以降
        .thenApply(result -> {
            System.out.println("  [変換処理] " + result + " を2倍にする");
            return result * 2;
        })
        // thenApply をさらに続けることもできる（メソッドチェーン）
        // [Java 7 不可] ラムダ式は Java 8 以降
        .thenApply(result -> {
            System.out.println("  [変換処理] " + result + " に 100 を足す");
            return result + 100;
        });

        // thenAccept: 最終結果を受け取って何か処理する（戻り値なし）
        // [Java 7 不可] ラムダ式は Java 8 以降
        CompletableFuture<Void> printFuture = future.thenAccept(finalResult -> {
            System.out.println("  [最終結果] " + finalResult);
        });

        System.out.println("  CompletableFuture を設定した。メインスレッドは他の処理を続けられる...");

        // join() で全ての処理が完了するまで待つ（get() と違い checked exception を投げない）
        printFuture.join();

        System.out.println();

        // ---------------------------------------------------------
        // 複数の CompletableFuture を並列で実行してすべての完了を待つ
        // ---------------------------------------------------------
        System.out.println("--- 複数の非同期処理を並列実行して全完了を待つ ---");

        // [Java 7 不可] ラムダ式・CompletableFuture ともに Java 8 以降
        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("  [cf1] 完了: 10");
            return 10;
        });

        // [Java 7 不可] ラムダ式・CompletableFuture ともに Java 8 以降
        CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("  [cf2] 完了: 20");
            return 20;
        });

        // [Java 7 不可] ラムダ式・CompletableFuture ともに Java 8 以降
        CompletableFuture<Integer> cf3 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(80); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("  [cf3] 完了: 30");
            return 30;
        });

        // [Java 7 不可] CompletableFuture.allOf() は Java 8 以降。全タスクの完了を待つ。
        CompletableFuture.allOf(cf1, cf2, cf3).join();

        int total = cf1.join() + cf2.join() + cf3.join();
        System.out.println("  合計: " + total + "（全て並列で実行完了）");
        System.out.println();
        System.out.println("=== まとめ: 非同期処理の段階的な進化 ===");
        System.out.println("  生 Thread    : new Thread(() -> ...).start() → 管理が大変");
        System.out.println("  ExecutorService : スレッドプールで効率化");
        System.out.println("  Future       : 計算結果を非同期で受け取れる");
        System.out.println("  CompletableFuture : 処理を繋げてパイプライン化できる（Java 8+）");
    }
}
