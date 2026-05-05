/**
 * 【なぜこのコードを学ぶのか】
 * このクラスはアプリケーションの「Composition Root（依存関係の組み立て場所）」だ。
 * Clean Architecture でも Onion Architecture でも、Composition Root の役割は同じ：
 * 「具体的な実装クラスを知っているのはここだけ」にすることで、
 * 変更箇所を1か所に集める。
 *
 * 第14章（Onion）と第15章（Clean）の Composition Root の違いは：
 *   Onion: repository → service → presenter の順に組み立てる
 *   Clean: gateway・presenter → interactor（Use Case） → controller の順に組み立てる
 *         ※ Interactor が presenter を直接持つため、先に presenter を作る必要がある
 *
 * 「なぜ presenter を先に作るのか」：
 * Interactor（AddProductInteractor）のコンストラクタが gateway と presenter の両方を必要とする。
 * Interactor が execute() の中で presenter.presentAddSuccess() を呼ぶため。
 * これが Clean Architecture の「Push型」データフローの証拠だ。
 */
package com.example.clean_architecture;

import com.example.clean_architecture.clean.adapter.ConsolePresenter;
import com.example.clean_architecture.clean.adapter.ProductController;
import com.example.clean_architecture.clean.infrastructure.InMemoryProductGateway;
import com.example.clean_architecture.clean.usecase.AddProductInteractor;
import com.example.clean_architecture.clean.usecase.AddProductUseCase;
import com.example.clean_architecture.clean.usecase.GetAllProductsInteractor;
import com.example.clean_architecture.clean.usecase.GetAllProductsUseCase;
import com.example.clean_architecture.clean.usecase.ProductGatewayPort;
import com.example.clean_architecture.clean.usecase.ProductPresenterPort;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("  第15章: クリーンアーキテクチャ");
        System.out.println("========================================");
        System.out.println();

        // ---------------------------------------------------------
        // Composition Root: 依存関係をここで組み立てる
        // ---------------------------------------------------------

        // ★ Step 1: Infrastructure（具体的な実装）を作成する
        ProductGatewayPort gateway = new InMemoryProductGateway();

        // ★ Step 2: Presenter（出力ポートの実装）を作成する
        // Clean Architecture では Interactor が Presenter を呼ぶため、先に Presenter を用意する
        // Onion Architecture では Service → Presenter の順だったが、
        // Clean Architecture では Interactor が Presenter に Push するため順序が逆になる
        ProductPresenterPort presenter = new ConsolePresenter();

        // ★ Step 3: Interactor（Use Case 実装）に gateway と presenter を注入する
        // Interactor はインターフェースしか知らない——具体クラスはここでだけ確定する
        AddProductUseCase addUseCase = new AddProductInteractor(gateway, presenter);
        GetAllProductsUseCase getAllUseCase = new GetAllProductsInteractor(gateway, presenter);

        // ★ Step 4: Controller に Use Case（インターフェース）を注入する
        ProductController controller = new ProductController(addUseCase, getAllUseCase);

        // ---------------------------------------------------------
        // 動作確認
        // ---------------------------------------------------------
        System.out.println("--- 初期データの商品一覧 ---");
        controller.getAllProducts();

        System.out.println("--- 商品を追加（正常系） ---");
        controller.addProduct("キーボード", 8000);

        System.out.println("--- 商品を追加（バリデーションエラー: 空の名前・負の価格） ---");
        controller.addProduct("", -1000);

        System.out.println("--- 追加後の商品一覧 ---");
        controller.getAllProducts();

        // ---------------------------------------------------------
        // Onion Architecture（第14章）との比較まとめ
        // ---------------------------------------------------------
        System.out.println("=== 第14章（Onion）と第15章（Clean）の比較 ===");
        System.out.println();
        System.out.println("  【Onion Architecture: Pull型（第14章）】");
        System.out.println("    presenter.showAll()");
        System.out.println("      → service.getAllProducts()  ← 戻り値でデータを受け取る");
        System.out.println("      → for (Product p : products) { 表示 }");
        System.out.println();
        System.out.println("  【Clean Architecture: Push型（第15章）】");
        System.out.println("    controller.getAllProducts()");
        System.out.println("      → interactor.execute()");
        System.out.println("        → gateway.findAll()  ← データ取得");
        System.out.println("        → presenter.presentProducts(dtos)  ← Interactor が Push");
        System.out.println();
        System.out.println("  Use Case（Interactor）はデータをどう表示するかを知らない。");
        System.out.println("  ConsolePresenter を JsonPresenter に差し替えても Interactor は無変更。");
        System.out.println();
        System.out.println("  どちらも「依存逆転の原則（DIP）」を実践しており優劣はない。");
        System.out.println("  規模・チーム・要件に応じて使い分けることが設計力の本質だ。");
    }
}
