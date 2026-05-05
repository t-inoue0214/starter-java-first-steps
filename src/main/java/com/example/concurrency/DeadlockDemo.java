/**
 * 【なぜこのコードを学ぶのか】
 * デッドロックとは「2つのスレッドが互いに相手のリソースを待ち続けて永遠に進まなくなる」状態。
 * 現場では再現が難しく、発生してからの原因特定に非常に時間がかかる。
 * 実際に「デッドロックが起きそうな状況」を tryLock でタイムアウト付きに体験してから、
 * 「ロック取得順序を全スレッドで統一する」という根本的な回避策を習得する。
 */
package com.example.concurrency;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockDemo {

    // ---------------------------------------------------------
    // 2つのロック（リソース）を定義する
    // ---------------------------------------------------------
    // ReentrantLock は synchronized より細かい制御ができる Lock の実装クラス。
    // tryLock() でタイムアウト付きのロック取得が可能。
    private static final Lock lockA = new ReentrantLock();
    private static final Lock lockB = new ReentrantLock();

    // 注意: このプログラムはマルチスレッドのため、実行するたびに結果が変わることがあります。
    // これはバグではなく、スレッドの実行順序が不定であることを体験するためのサンプルです。
    public static void main(String[] args) throws InterruptedException {

        System.out.println("========================================");
        System.out.println("  DeadlockDemo: デッドロックの体験と回避");
        System.out.println("========================================");
        System.out.println();

        // ---------------------------------------------------------
        // section1: ReentrantLock の基本動作を確認する
        // ---------------------------------------------------------
        section1LockBasics();

        // ---------------------------------------------------------
        // section2: Before = デッドロックが起きる構造を体験する
        // ---------------------------------------------------------
        section2DeadlockExperience();

        // ---------------------------------------------------------
        // section3: After = ロック順序の統一でデッドロックを回避する
        // ---------------------------------------------------------
        section3DeadlockAvoidance();
    }

    // ==========================================================
    // section1: Lock の基本（lock / unlock の使い方）
    // ==========================================================
    private static void section1LockBasics() {

        System.out.println("--- section1: ReentrantLock の基本 ---");

        // lock() でロックを取得する。取得できるまで待機する（ブロッキング）。
        lockA.lock();
        try {
            // クリティカルセクション: このブロック内は1スレッドしか実行できない
            System.out.println("lockA を取得した → 処理中...");
        } finally {
            // finally で必ず unlock する（例外が起きてもロックが解放されるように）
            lockA.unlock();
            System.out.println("lockA を解放した");
        }

        // tryLock() でタイムアウト付きのロック取得を試みる
        boolean acquired = false;
        try {
            // 最大500ms だけロック取得を待つ（タイムアウトしたら false を返す）
            acquired = lockB.tryLock(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (acquired) {
            try {
                System.out.println("lockB を取得した（tryLock 成功）");
            } finally {
                lockB.unlock();
                System.out.println("lockB を解放した");
            }
        } else {
            System.out.println("lockB の取得に失敗した（タイムアウト）");
        }

        System.out.println();
    }

    // ==========================================================
    // section2: Before = デッドロックが起きる構造
    // ==========================================================
    // Thread-1: lockA → lockB の順で取得しようとする
    // Thread-2: lockB → lockA の順で取得しようとする
    // → 互いに相手のロックを待ち続ける = デッドロック！
    // ※ tryLock(タイムアウト) を使うことで永遠に止まらないようにしている
    private static void section2DeadlockExperience() throws InterruptedException {

        System.out.println("--- section2: Before = デッドロックが起きる構造 ---");
        System.out.println("Thread-1 は lockA → lockB の順で取得を試みる");
        System.out.println("Thread-2 は lockB → lockA の順で取得を試みる");
        System.out.println("→ 互いに相手が持つロックを待ち続けてしまう（デッドロック）");
        System.out.println();

        // ========== Before: ロック順序がスレッドごとに異なる ==========

        // [Java 7 不可] ラムダ式は Java 8 以降。Java 7 では Runnable の匿名クラスで書く:
        //   Thread thread1 = new Thread(new Runnable() {
        //       @Override public void run() { /* ... */ }
        //   });
        Thread thread1 = new Thread(() -> {
            System.out.println("[Thread-1] lockA を取得しようとしている...");
            lockA.lock();
            System.out.println("[Thread-1] lockA を取得した！次に lockB を取得しようとしている...");

            try {
                // ここで少し待機して Thread-2 が lockB を取得する余地を作る
                // （デッドロックが発生しやすい状況を意図的に作る）
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            boolean acquiredB = false;
            try {
                // タイムアウト付きで lockB を取得しようとする（デッドロックで永遠に止まらないよう）
                acquiredB = lockB.tryLock(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                if (acquiredB) {
                    System.out.println("[Thread-1] lockB も取得成功 → 処理完了");
                    lockB.unlock();
                } else {
                    // ここがデッドロックの体験ポイント
                    System.out.println("[Thread-1] デッドロック発生: lockB を取得できなかった（タイムアウト）");
                }
            } finally {
                lockA.unlock();
                System.out.println("[Thread-1] lockA を解放した");
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            System.out.println("[Thread-2] lockB を取得しようとしている...");
            lockB.lock();
            System.out.println("[Thread-2] lockB を取得した！次に lockA を取得しようとしている...");

            try {
                Thread.sleep(100); // Thread-1 が lockA を取得する余地を作る
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            boolean acquiredA = false;
            try {
                acquiredA = lockA.tryLock(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                if (acquiredA) {
                    System.out.println("[Thread-2] lockA も取得成功 → 処理完了");
                    lockA.unlock();
                } else {
                    System.out.println("[Thread-2] デッドロック発生: lockA を取得できなかった（タイムアウト）");
                }
            } finally {
                lockB.unlock();
                System.out.println("[Thread-2] lockB を解放した");
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        System.out.println();
        System.out.println("→ 上記のように「互いの相手のロックを待つ」構造がデッドロックの原因");
        System.out.println("  本物のデッドロックではプログラムが永遠に止まるが、");
        System.out.println("  tryLock(タイムアウト) を使うことで検出・回避できる");
        System.out.println();
    }

    // ==========================================================
    // section3: After = ロック順序を統一してデッドロックを回避する
    // ==========================================================
    // ルール: 全スレッドが「lockA → lockB」の順でロックを取得する
    // → 一方が lockA を持ちながら lockB を待っても、
    //   もう一方も lockA を取得しようとするので「A を持つスレッドが B を待つ」構造にならない
    private static void section3DeadlockAvoidance() throws InterruptedException {

        System.out.println("--- section3: After = ロック順序の統一でデッドロックを回避 ---");
        System.out.println("両スレッドともに lockA → lockB の順で取得する");
        System.out.println();

        // ========== After: 全スレッドが同じ順序でロックを取得する ==========

        // [Java 7 不可] ラムダ式は Java 8 以降
        Thread thread1 = new Thread(() -> {
            System.out.println("[Thread-1] lockA を取得しようとしている...");
            lockA.lock();
            System.out.println("[Thread-1] lockA を取得した！次に lockB を取得しようとしている...");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Thread-1 も Thread-2 も「A → B」の順なので、デッドロックは起きない
            lockB.lock();
            System.out.println("[Thread-1] lockB も取得した → 処理中...");

            try {
                // 実際の業務処理はここに書く
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockB.unlock();
                System.out.println("[Thread-1] lockB を解放した");
                lockA.unlock();
                System.out.println("[Thread-1] lockA を解放した → 処理完了");
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            System.out.println("[Thread-2] lockA を取得しようとしている...");
            // Thread-2 も lockA から取得する（Thread-1 と同じ順序）
            lockA.lock();
            System.out.println("[Thread-2] lockA を取得した！次に lockB を取得しようとしている...");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            lockB.lock();
            System.out.println("[Thread-2] lockB も取得した → 処理中...");

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockB.unlock();
                System.out.println("[Thread-2] lockB を解放した");
                lockA.unlock();
                System.out.println("[Thread-2] lockA を解放した → 処理完了");
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        System.out.println();
        System.out.println("=== まとめ: デッドロックの回避策 ===");
        System.out.println("  Before: スレッドごとにロック順序が異なる → デッドロックの危険");
        System.out.println("  After : 全スレッドで lockA → lockB の順に統一 → デッドロックなし");
        System.out.println();
        System.out.println("  その他の回避策:");
        System.out.println("  ・tryLock(タイムアウト) でロック取得に上限時間を設ける");
        System.out.println("  ・ロックの数を減らす（ロック対象をシンプルに設計する）");
        System.out.println("  ・java.util.concurrent の高レベルAPI（ExecutorService 等）を使う");
    }
}
