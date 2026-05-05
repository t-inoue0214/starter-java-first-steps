/**
 * 【なぜこのコードを学ぶのか】
 * 文字列の分岐で処理を切り替える設計は、支払い方法が増えるたびにif文が増え続ける。
 * インターフェースを使うと「新しい支払い方法を追加しても呼び出し元のコードを一切変更しない」
 * 設計が実現できる。これが「ポリモーフィズム（多態性）」の本質であり、
 * 現場で保守しやすいコードを書くための基礎になる。
 *
 * 【第06章における位置づけ】
 * LambdaBasics・FunctionalInterfaces・HigherOrderFunctions の3ファイルは
 * 「ラムダ式で振る舞いを渡す（関数型の発想）」を学んだ。
 * このファイルからは視点が変わる。
 * 「クラスで振る舞いを実装し、インターフェースで型をそろえる（OOP の発想）」を学ぶ。
 * 同じ Java で2つのパラダイムを使い分けることが、現場での実際の姿だ。
 */
package com.example.oop_and_type_system;

import java.util.ArrayList;
import java.util.List;

public class InterfaceAndPolymorphism {

    // ---------------------------------------------------------
    // ========== Before: 文字列分岐による支払い処理（問題のある設計） ==========
    // ---------------------------------------------------------

    // 支払い方法を文字列で受け取り、if-else で処理を分岐している
    // 新しい支払い方法が増えるたびに、このメソッドを修正しなければならない
    private static void processPaymentBefore(String type, int amount) {
        if ("credit".equals(type)) { // "credit" と type が逆にすると null の場合 に NullPointerException になる
            System.out.println("[Before] クレジットカードで " + amount + "円 を支払いました");
        } else if ("paypay".equals(type)) {
            System.out.println("[Before] PayPayで " + amount + "円 を支払いました");
        } else if ("cash".equals(type)) {
            System.out.println("[Before] 現金で " + amount + "円 を支払いました");
        } else {
            System.out.println("[Before] 不明な支払い方法: " + type);
        }
        // ← コンビニ払いを追加するときは「ここにも else if を足す」必要がある
        //   このメソッドを知っている全員が修正の影響を受ける
    }

    // ---------------------------------------------------------
    // なぜ extends（継承）ではなく implements（インターフェース実装）を使うのか？
    // ---------------------------------------------------------
    // extends は「CreditPayment は AbstractPayment だ（is-a 関係）」を表します。
    // implements は「CreditPayment は Payment という振る舞いができる（can-do 関係）」を表します。
    //
    // クレジットカードと PayPay と現金に「共通の親クラス」はありません。
    // 「支払う」という能力を共通化したいだけなので、インターフェースが正解です。
    // また extends と違い、implements は複数同時に指定できます（多重実装）。

    // ---------------------------------------------------------
    // ========== After: インターフェースで支払い処理を統一 ==========
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // 【インターフェースの3つのルール】
    // ---------------------------------------------------------
    // ① インターフェース内のメソッドは「宣言だけ」—本体 {} は書かない（抽象メソッド）
    // ② implements したクラスは、全メソッドを必ず実装しなければコンパイルエラー
    // ③ インターフェース型の変数には、それを implements したクラスなら何でも代入できる
    //
    //   Payment p = new CreditPayment();  ← OK（CreditPayment は Payment を implements）
    //   Payment p = new CashPayment();    ← OK（CashPayment も Payment を implements）
    //   Payment p = new PayPayPayment();  ← OK
    //
    // ③ のことを「ポリモーフィズム（多態性）」と呼ぶ。
    // 「同じ Payment 型として扱えるが、pay() を呼ぶと実際の動作はクラスによって異なる」
    // これが呼び出し元のコードを変えずに振る舞いを差し替えられる仕組みの正体。
    // ---------------------------------------------------------
    // 「支払う」という振る舞いを定義するインターフェース
    // どんな支払い方法も、このインターフェースを implements すれば呼び出し元から使える
    interface Payment {
        void pay(int amount);   // 支払い処理（各クラスがそれぞれ実装する）
        String name();           // 支払い方法の名前を返す
    }

    // クレジットカード支払いの実装
    // 注意: Javaは「単一継承」なので extends できるクラスは1つだけ
    //       ただし implements は複数のインターフェースを同時に指定できる
    private static class CreditPayment implements Payment {

        @Override
        public void pay(int amount) {
            System.out.println("[After] クレジットカードで " + amount + "円 を支払いました（3Dセキュア認証済み）");
        }

        @Override
        public String name() {
            return "クレジットカード";
        }
    }

    // PayPay支払いの実装
    private static class PayPayPayment implements Payment {

        @Override
        public void pay(int amount) {
            System.out.println("[After] PayPayで " + amount + "円 を支払いました（QRコード読取完了）");
        }

        @Override
        public String name() {
            return "PayPay";
        }
    }

    // 現金支払いの実装
    private static class CashPayment implements Payment {

        @Override
        public void pay(int amount) {
            System.out.println("[After] 現金で " + amount + "円 を支払いました（お釣りをお確かめください）");
        }

        @Override
        public String name() {
            return "現金";
        }
    }

    // ---------------------------------------------------------
    // 新しい支払い方法を「追加するだけ」で対応できることを実証
    // ---------------------------------------------------------

    // コンビニ払いを追加しても、呼び出し元（main）のコードは一切変更しない
    private static class ConveniencePayment implements Payment {

        @Override
        public void pay(int amount) {
            System.out.println("[After] コンビニで " + amount + "円 を支払いました（番号票をレジへ）");
        }

        @Override
        public String name() {
            return "コンビニ払い";
        }
    }

    // 支払いを処理するメソッド（After版）
    // Payment型で受け取るため、どんな実装クラスでも同じように処理できる
    private static void processPaymentAfter(Payment payment, int amount) {
        System.out.print(payment.name() + " → ");
        payment.pay(amount);
        // ← このメソッドは将来どんな支払い方法が追加されても変更不要
    }

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // Before: 文字列分岐（if-else が増え続ける設計）
        // ---------------------------------------------------------
        System.out.println("=== Before: 文字列分岐による支払い処理 ===");

        processPaymentBefore("credit", 3000);
        processPaymentBefore("paypay", 1500);
        processPaymentBefore("cash",   500);
        // 誤字してもコンパイルは通る（Enumと同じ問題）
        processPaymentBefore("creditcard", 1000); // → 不明な支払い方法

        // ---------------------------------------------------------
        // After: インターフェースによるポリモーフィズム
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== After: インターフェースによる支払い処理 ===");

        // Payment型のリストに実装クラスを詰める（ポリモーフィズムの活用）
        // 「インターフェース型の変数に実装クラスのインスタンスを代入できる」のがポイント
        // List<Payment> と宣言することで「Payment インターフェースを実装した何か」なら何でも入れられる。
        // ArrayList<Payment> と書くと具体的な実装に縛られる。インターフェース型で宣言するのが現場の習慣。
        List<Payment> paymentMethods = new ArrayList<>();
        paymentMethods.add(new CreditPayment());
        paymentMethods.add(new PayPayPayment());
        paymentMethods.add(new CashPayment());

        // 呼び出し元はループするだけ—if文もswitch文も不要
        for (Payment payment : paymentMethods) {
            processPaymentAfter(payment, 2000);
        }

        // ---------------------------------------------------------
        // 新しい支払い方法を追加しても呼び出し元コードは無変更
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== コンビニ払いを追加（呼び出し元は変更なし） ===");

        // リストに追加するだけ。processPaymentAfter() もループも変更不要
        paymentMethods.add(new ConveniencePayment());

        // 同じループがそのまま動く—新しい支払い方法も正しく処理される
        for (Payment payment : paymentMethods) {
            processPaymentAfter(payment, 5000);
        }

        // ---------------------------------------------------------
        // 継承との対比（コメントで説明）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 継承 vs インターフェース の使い分け ===");
        System.out.println("extends（継承）: 1つのクラスからしか継承できない（単一継承）");
        System.out.println("  → 「is-a関係」を表す。Dog extends Animal（犬は動物だ）");
        System.out.println("implements（実装）: 複数のインターフェースを同時に実装できる");
        System.out.println("  → 「can-do関係」を表す。CreditPayment implements Payment（クレジットカードは支払いができる）");
        System.out.println("  → 今回の例は implements を使うべきケース（支払い方法に共通の「親クラス」はない）");
    }
}
