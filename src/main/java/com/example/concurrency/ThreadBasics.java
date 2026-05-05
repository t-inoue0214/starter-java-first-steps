/**
 * 【なぜこのコードを学ぶのか】
 * CPUコアが複数ある現代のPCでは、処理を「本当に同時に」動かせる。
 * Javaのスレッドはその制御単位であり、Vert.x の Event Loop もワーカースレッドも
 * 内部ではこの仕組みで動いている。
 * しかし「複数スレッドが1つの変数を同時に書き換える」と結果が壊れる（競合状態）。
 * この怖さを実際に体験し、synchronized / AtomicInteger で防ぐ方法を習得する。
 */
package com.example.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadBasics {

    // ---------------------------------------------------------
    // section3・section4 で複数スレッドから書き換えるカウンタ
    // ---------------------------------------------------------

    // synchronized なしの危険なカウンタ（競合状態を体験させる）
    private static int unsafeCounter = 0;

    // synchronized を使った安全なカウンタ
    private static int safeCounter = 0;

    // synchronized のロック対象オブジェクト
    private static final Object lock = new Object();

    // [Java 5 以降] AtomicInteger はロックなしで原子的な操作を保証する
    private static final AtomicInteger atomicCounter = new AtomicInteger(0);

    // 注意: このプログラムはマルチスレッドのため、実行するたびに結果が変わることがあります。
    // これはバグではなく、スレッドの実行順序が不定であることを体験するためのサンプルです。
    public static void main(String[] args) throws InterruptedException {

        System.out.println("========================================");
        System.out.println("  ThreadBasics: スレッドの基本と競合状態");
        System.out.println("========================================");
        System.out.println();

        // ---------------------------------------------------------
        // section1: スレッドの基本 ─ 2種類の書き方
        // ---------------------------------------------------------
        section1BasicThreads();

        // ---------------------------------------------------------
        // section2: シングルスレッド vs マルチスレッドの速度比較
        // ---------------------------------------------------------
        section2SpeedComparison();

        // ---------------------------------------------------------
        // section3: 競合状態（Race Condition）の体験
        // ---------------------------------------------------------
        section3RaceCondition();

        // ---------------------------------------------------------
        // section4: synchronized で解決
        // ---------------------------------------------------------
        section4Synchronized();

        // ---------------------------------------------------------
        // section5: AtomicInteger で解決
        // ---------------------------------------------------------
        section5AtomicInteger();
    }

    // ==========================================================
    // section1: スレッドの基本
    // ==========================================================
    private static void section1BasicThreads() throws InterruptedException {

        System.out.println("--- section1: スレッドの基本 ---");

        // CPUコア数を確認する（スレッドを増やす意味を理解する起点）
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("このマシンの利用可能CPUコア数: " + cores);
        System.out.println("コア数だけスレッドを同時に本当に並列で動かせる。");
        System.out.println();

        // ========== Before: Thread クラスを extends する（旧来の書き方） ==========
        // Java 1.0 から存在する方法。クラス継承を消費してしまうため、
        // 他のクラスを extends できなくなるデメリットがある。

        System.out.println("--- Before: Thread を extends した書き方 ---");

        Thread t1 = new WorkerThread("Thread-A");
        Thread t2 = new WorkerThread("Thread-B");

        t1.start(); // スレッドを開始する（run() が別スレッドで呼ばれる）
        t2.start();

        t1.join();  // t1 の完了を待つ
        t2.join();  // t2 の完了を待つ

        System.out.println();

        // ========== After: Runnable をラムダで渡す（モダンな書き方） ==========
        // [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では匿名クラスで書く:
        //   Runnable r = new Runnable() {
        //       @Override public void run() { /* ... */ }
        //   };
        // Runnable を使うと「何をするか」と「スレッドの管理」を分離できる。
        // また、継承を消費しないため柔軟性が高い。

        System.out.println("--- After: Runnable をラムダで渡すモダンな書き方 ---");

        // [Java 7 不可] ラムダ式 () -> { ... } は Java 8 以降
        Runnable taskC = () -> {
            for (int i = 0; i < 3; i++) {
                System.out.println("Thread-C が実行中 (i=" + i + ")");
                try {
                    Thread.sleep(100); // 100ms 待つ（他スレッドが動く余地を作る）
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // [Java 7 不可] ラムダ式は Java 8 以降
        Runnable taskD = () -> {
            for (int i = 0; i < 3; i++) {
                System.out.println("Thread-D が実行中 (i=" + i + ")");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread t3 = new Thread(taskC);
        Thread t4 = new Thread(taskD);

        t3.start();
        t4.start();

        t3.join();
        t4.join();

        System.out.println("→ Thread-C と Thread-D の実行順序は実行のたびに変わる（非決定的）");
        System.out.println();
    }

    // ==========================================================
    // section2: シングルスレッド vs マルチスレッドの速度比較
    // ==========================================================
    private static void section2SpeedComparison() throws InterruptedException {

        System.out.println("--- section2: シングルスレッド vs マルチスレッドの速度比較 ---");

        final long N = 10_000_000L; // 1から加算する上限値

        // --- シングルスレッド（全部メインスレッドで計算する） ---
        long singleStart = System.nanoTime();
        long singleSum = 0;
        for (long i = 1; i <= N; i++) {
            singleSum += i;
        }
        long singleElapsed = System.nanoTime() - singleStart;

        System.out.println("シングルスレッド: sum=" + singleSum
                + " / 処理時間=" + (singleElapsed / 1_000_000) + " ms");

        // --- マルチスレッド（2スレッドに前半・後半を半分ずつ割り振る） ---
        // 各スレッドの計算結果を受け取る配列（添字0=前半, 1=後半）
        long[] results = new long[2];

        long multiStart = System.nanoTime();

        // [Java 7 不可] ラムダ式は Java 8 以降
        Thread firstHalf = new Thread(() -> {
            long sum = 0;
            for (long i = 1; i <= N / 2; i++) {
                sum += i;
            }
            results[0] = sum; // 共有配列への書き込み（各スレッドが異なる添字を使うため安全）
        });

        // [Java 7 不可] ラムダ式は Java 8 以降
        Thread secondHalf = new Thread(() -> {
            long sum = 0;
            for (long i = N / 2 + 1; i <= N; i++) {
                sum += i;
            }
            results[1] = sum;
        });

        firstHalf.start();
        secondHalf.start();
        firstHalf.join();
        secondHalf.join();

        long multiSum = results[0] + results[1];
        long multiElapsed = System.nanoTime() - multiStart;

        System.out.println("マルチスレッド: sum=" + multiSum
                + " / 処理時間=" + (multiElapsed / 1_000_000) + " ms");
        System.out.println("※ CPUコア数が1の環境ではマルチスレッドでも速くならない場合がある");
        System.out.println("  （コンテキストスイッチのオーバーヘッドが加わるため逆に遅くなることもある）");
        System.out.println();
    }

    // ==========================================================
    // section3: 競合状態（Race Condition）の体験
    // ==========================================================
    private static void section3RaceCondition() throws InterruptedException {

        System.out.println("--- section3: 競合状態（Race Condition）の体験 ---");
        System.out.println("100スレッドがそれぞれ1000回インクリメントする → 期待値: 100,000");

        // unsafeCounter をリセット
        unsafeCounter = 0;

        int threadCount = 100;
        int incrementsPerThread = 1000;

        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            // [Java 7 不可] ラムダ式は Java 8 以降
            Thread thread = new Thread(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    // ★ 問題のある書き方: この3ステップの間に別スレッドが割り込める
                    //   1. unsafeCounter の値をメモリから読む
                    //   2. 値に 1 を足す
                    //   3. 結果をメモリに書き戻す
                    // 2スレッドが同時に「読む→足す→書く」すると、片方の更新が消える！
                    unsafeCounter++;
                }
            });
            threads.add(thread);
        }

        // 全スレッドを開始
        for (Thread thread : threads) {
            thread.start();
        }
        // 全スレッドの完了を待つ
        for (Thread thread : threads) {
            thread.join();
        }

        int expected = threadCount * incrementsPerThread;
        System.out.println("[競合あり] 期待値: " + expected + ", 実際: " + unsafeCounter);
        if (unsafeCounter < expected) {
            System.out.println("↑ 競合状態により値が失われた！（実行のたびに変わる）");
        } else {
            System.out.println("↑ 今回はたまたま一致した。繰り返せばズレが生じる。");
        }
        System.out.println();
    }

    // ==========================================================
    // section4: synchronized で解決
    // ==========================================================
    private static void section4Synchronized() throws InterruptedException {

        System.out.println("--- section4: synchronized で競合状態を解決 ---");

        // safeCounter をリセット
        safeCounter = 0;

        int threadCount = 100;
        int incrementsPerThread = 1000;

        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            // [Java 7 不可] ラムダ式は Java 8 以降
            Thread thread = new Thread(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    // synchronized ブロック: lock を取得したスレッドだけが中に入れる
                    // 他のスレッドは lock が解放されるまで待機する（ミューテックス）
                    synchronized (lock) {
                        safeCounter++;
                    }
                }
            });
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        int expected = threadCount * incrementsPerThread;
        System.out.println("[synchronized] 期待値: " + expected + ", 実際: " + safeCounter
                + " ← 必ず一致する");
        System.out.println("デメリット: スレッドが順番待ちするため、スレッドが多いほど遅くなる");
        System.out.println();
    }

    // ==========================================================
    // section5: AtomicInteger で解決
    // ==========================================================
    private static void section5AtomicInteger() throws InterruptedException {

        System.out.println("--- section5: AtomicInteger で解決（ロックなしで安全） ---");

        // atomicCounter をリセット
        atomicCounter.set(0);

        int threadCount = 100;
        int incrementsPerThread = 1000;

        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            // [Java 7 不可] ラムダ式は Java 8 以降
            Thread thread = new Thread(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    // [Java 5 以降] AtomicInteger.incrementAndGet() は CPU のアトミック命令を使い、
                    // synchronized ブロックなしでスレッドセーフなインクリメントを実現する。
                    // CAS（Compare-And-Swap）命令によりロックより高速に動作する。
                    atomicCounter.incrementAndGet();
                }
            });
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        int expected = threadCount * incrementsPerThread;
        System.out.println("[AtomicInteger] 期待値: " + expected + ", 実際: " + atomicCounter.get()
                + " ← ロックなしで安全");
        System.out.println();
        System.out.println("=== まとめ: カウンタの安全性比較 ===");
        System.out.println("  unsafeCounter  : スレッドセーフでない → 値が壊れる");
        System.out.println("  synchronized   : スレッドセーフ → ロック待ちが発生");
        System.out.println("  AtomicInteger  : スレッドセーフ → ロックなし・高速（単純な数値操作向け）");
    }
}

// ==========================================================
// Before: Thread クラスを extends する旧来の書き方
// ==========================================================
// 問題点: Java は単一継承なので、WorkerThread は他のクラスを extends できなくなる
class WorkerThread extends Thread {

    private final String workerName;

    public WorkerThread(String workerName) {
        this.workerName = workerName;
    }

    @Override
    public void run() {
        // run() の中身がスレッドとして別々に動く
        for (int i = 0; i < 3; i++) {
            System.out.println(workerName + " が実行中 (i=" + i + ")");
            try {
                Thread.sleep(100); // 100ms 待つ（他スレッドが動く余地を作る）
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
