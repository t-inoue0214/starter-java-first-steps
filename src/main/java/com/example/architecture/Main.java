/**
 * 【なぜこのコードを学ぶのか】
 * このクラスはアプリケーションの「エントリーポイント（起点）」であり、
 * 同時に「Composition Root（依存関係の組み立て場所）」でもある。
 * Onion Architecture では、具体的な実装クラス（InMemoryProductRepository 等）を
 * 知っているのはこの起点クラスだけだ——それ以外の層はインターフェースしか知らない。
 *
 * まず Before（BigMainAntiPattern）で問題を体験し、
 * その後 After（Onion Architecture）で解決策を体験する。
 * 同じ「商品管理」機能が、設計によってどれだけ変わるかを比較せよ。
 */
package com.example.architecture;

import com.example.architecture.onion.application.ProductService;
import com.example.architecture.onion.domain.ProductRepository;
import com.example.architecture.onion.infrastructure.InMemoryProductRepository;
import com.example.architecture.onion.presentation.ProductPresenter;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("  第14章: 設計とアーキテクチャ");
        System.out.println("========================================");
        System.out.println();

        // ---------------------------------------------------------
        // Before: 全ロジックを1クラスに詰め込むアンチパターン
        // ---------------------------------------------------------
        System.out.println("========== Before: 全ロジックを1クラスに詰め込む ==========");
        System.out.println();
        BigMainAntiPattern.run();

        System.out.println();
        System.out.println("--------------------------------------------------------");
        System.out.println();

        // ---------------------------------------------------------
        // After: Onion Architecture（レイヤー分割）
        // ---------------------------------------------------------
        System.out.println("========== After: Onion Architecture（レイヤー分割） ==========");
        System.out.println();

        // ★ Composition Root: 具体的な実装を知っているのはここだけ
        // InMemoryProductRepository を JdbcProductRepository に変えるとき、
        // 修正が必要な箇所はこの1行だけ——他の層は何も変えなくてよい
        ProductRepository repository = new InMemoryProductRepository();

        // ProductService には「リポジトリのインターフェース」だけを渡す
        // Spring の @Autowired も内部でこれと同じコンストラクタ注入を行っている
        ProductService service = new ProductService(repository);
        ProductPresenter presenter = new ProductPresenter(service);

        // --- 一覧表示 ---
        presenter.showAll();

        // --- 検索（存在するID / 存在しないID）---
        presenter.showById(2);
        presenter.showById(99);

        // --- 商品追加（正常系 / バリデーションエラー）---
        presenter.addProduct("キーボード", 8000);
        presenter.addProduct("", -1000);  // バリデーションエラーを体験

        // --- 追加後の一覧表示 ---
        presenter.showAll();

        // ---------------------------------------------------------
        // アーキテクチャの学習まとめ
        // ---------------------------------------------------------
        System.out.println("=== Onion Architecture: 依存の方向 ===");
        System.out.println();
        System.out.println("  Presentation  →  Application  →  Domain  ←  Infrastructure");
        System.out.println();
        System.out.println("  ★ Domain 層（Product, ProductRepository）は何にも依存しない");
        System.out.println("     最も変化が少ない「ビジネスの核心」を最内層に置く");
        System.out.println();
        System.out.println("  ★ Infrastructure は Domain のインターフェースを implements する");
        System.out.println("     依存の矢印が内側（Domain）を向いている——これが「依存逆転」");
        System.out.println();
        System.out.println("  ★ Application 層はインターフェースにしか依存しない");
        System.out.println("     DB をインメモリ → JDBC → JPA に変えても、ここは無変更");
        System.out.println();
        System.out.println("  ★ Presentation 層は「どう見せるか」だけを知っている");
        System.out.println("     コンソール → Web API → CLI に変えても、ビジネスロジックは無変更");
        System.out.println();
        System.out.println("  各層を独立してテスト可能（モックに差し替えて JUnit で検証できる）");
    }
}
