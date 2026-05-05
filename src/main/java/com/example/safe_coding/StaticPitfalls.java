/**
 * 【なぜこのコードを学ぶのか】
 * static フィールドは JVM に「1つだけ」存在する。
 * Web サーバーでは複数スレッドが同時にリクエストを処理するため、
 * static フィールドにリクエストデータを格納すると「顧客Aのデータが顧客Bに混入」する。
 * 本番で顧客情報漏洩の原因になるこのパターンを体験で理解する。
 * 第06章の StaticAndAnnotation.java では「テスト不可能性」を扱った。
 * この章では「本番の情報漏洩」という現場視点で static の危険性を体験する。
 */
package com.example.safe_coding;

import java.util.concurrent.CountDownLatch;

public class StaticPitfalls {

    // ---------------------------------------------------------
    // ========== Before: static フィールドにリクエストデータを格納（危険）==========
    // ---------------------------------------------------------

    /**
     * 注文リクエストを処理する Bad クラス。
     * static フィールドを使っているため、複数スレッドから同時に呼ばれると
     * 別のスレッドが書いた値を読み取ってしまう。
     */
    static class OrderHandlerBad {

        // ここが問題: static フィールドは JVM 上で1つだけ存在する
        // スレッドAが書いた直後に、スレッドBが別の値で上書きする
        static String currentUserId;
        static String currentOrderId;

        static void handleRequest(String userId, String orderId) throws InterruptedException {
            // ① リクエストデータをセット
            currentUserId  = userId;
            currentOrderId = orderId;

            // ② 処理時間を模倣して50ms待機する
            //    この間に別スレッドが ① を実行して、フィールドを上書きしてしまう
            Thread.sleep(50);

            // ③ セットしたはずの値を読み取って表示
            //    しかしここで読む値は「別スレッドに上書きされた後の値」かもしれない
            System.out.printf("  [Before] userId=%s の注文 %s を処理完了%n", currentUserId, currentOrderId);
        }
    }

    // ---------------------------------------------------------
    // ========== After: インスタンスフィールドで安全に管理 ==========
    // ---------------------------------------------------------

    /**
     * 注文リクエストを処理する Good クラス。
     * インスタンスフィールドを使い、各リクエストが独自の状態を持つ。
     * スレッドA用・スレッドB用に別々のインスタンスを生成するため、
     * 互いの値を上書きしない。
     */
    static class OrderHandlerGood {

        // final インスタンスフィールド: このオブジェクト専用の値
        // 別のオブジェクトのフィールドには影響しない
        private final String userId;
        private final String orderId;

        OrderHandlerGood(String userId, String orderId) {
            this.userId  = userId;
            this.orderId = orderId;
        }

        void handleRequest() throws InterruptedException {
            // 50ms 待機しても、自分のフィールドは誰にも上書きされない
            Thread.sleep(50);
            System.out.printf("  [After]  userId=%s の注文 %s を処理完了%n", userId, orderId);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // セクション1: 概念の説明
        // ---------------------------------------------------------
        System.out.println("=== 1. static フィールドは「クラスに1つ」しか存在しない ===");
        System.out.println();
        System.out.println("  通常のフィールド（インスタンス変数）:");
        System.out.println("    new OrderHandler() → handler1 が持つフィールド（handler1 専用）");
        System.out.println("    new OrderHandler() → handler2 が持つフィールド（handler2 専用）");
        System.out.println();
        System.out.println("  static フィールド:");
        System.out.println("    OrderHandler.currentUserId → JVM に1つだけ存在");
        System.out.println("    → handler1 が書いても handler2 が書いても同じ変数が書き換わる");
        System.out.println("    → Web サーバーで複数リクエストを同時処理すると「上書き合戦」になる");

        // ---------------------------------------------------------
        // セクション2: Before デモ（2スレッドを同時に起動して競合を起こす）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 2. Before: static フィールドの競合（情報漏洩）===");
        System.out.println("  ※ スレッドの実行順序は非決定的なため、結果は毎回異なる場合があります");
        System.out.println();

        // CountDownLatch を使い、2スレッドをほぼ同時にスタートさせる
        // [Java 7 不可] CountDownLatch は Java 5 以降だが、ラムダは Java 8 以降。
        //   Java 7 では Runnable の匿名クラスで書く:
        //   new Thread(new Runnable() { @Override public void run() { ... } })
        CountDownLatch startSignal = new CountDownLatch(1);

        Thread threadA = new Thread(() -> {
            try {
                startSignal.await(); // スタート合図を待つ
                OrderHandlerBad.handleRequest("USER-001", "ORDER-A");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread threadB = new Thread(() -> {
            try {
                startSignal.await(); // スタート合図を待つ
                OrderHandlerBad.handleRequest("USER-002", "ORDER-B");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        threadA.start();
        threadB.start();
        startSignal.countDown(); // 両スレッドを同時に走らせる
        threadA.join();
        threadB.join();

        System.out.println();
        System.out.println("  ↑ userId の表示がどちらも USER-002 になることがある。");
        System.out.println("    スレッドAが currentUserId = \"USER-001\" をセットした直後に、");
        System.out.println("    スレッドBが currentUserId = \"USER-002\" で上書きするためだ。");
        System.out.println("    これが本番環境で起きると、USER-001 の注文情報が USER-002 の画面に表示される。");

        // ---------------------------------------------------------
        // セクション3: After デモ（インスタンスフィールドで競合なし）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 3. After: インスタンスフィールドで安全に分離 ===");
        System.out.println();

        CountDownLatch startSignal2 = new CountDownLatch(1);

        Thread threadC = new Thread(() -> {
            try {
                startSignal2.await();
                new OrderHandlerGood("USER-001", "ORDER-A").handleRequest();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread threadD = new Thread(() -> {
            try {
                startSignal2.await();
                new OrderHandlerGood("USER-002", "ORDER-B").handleRequest();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        threadC.start();
        threadD.start();
        startSignal2.countDown();
        threadC.join();
        threadD.join();

        System.out.println();
        System.out.println("  ↑ USER-001 は必ず ORDER-A を処理する。");
        System.out.println("    各スレッドが独自のインスタンスを持つため、");
        System.out.println("    他スレッドがフィールドを上書きすることは絶対にない。");

        // ---------------------------------------------------------
        // セクション4: static を使っていいもの / 使ってはいけないもの
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 4. static を使っていいもの / 使ってはいけないもの ===");
        System.out.println();
        System.out.println("  用途                                  安全か？");
        System.out.println("  ────────────────────────────────────────────────────────────");
        System.out.println("  定数（static final）                  ✅ 変更されないので安全");
        System.out.println("  ユーティリティメソッド（引数のみ使用）  ✅ 状態を持たないので安全");
        System.out.println("  リクエストデータの格納                 ❌ スレッド間で上書きされる");
        System.out.println("  ログインユーザー情報の格納             ❌ 顧客情報が漏洩する");
        System.out.println("  処理中の一時データ                     ❌ 処理の途中で別値になる");
        System.out.println();
        System.out.println("  → リクエスト処理クラス（Controller・Service・Handler）のフィールドには");
        System.out.println("    絶対に static を付けてはならない。");
    }
}
