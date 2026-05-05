/**
 * 【なぜこのコードを学ぶのか】
 * Interactor は Use Case の「実装クラス」だ。
 * Onion Architecture の ProductService.addProduct() に相当するが、
 * Clean Architecture では「商品追加」だけを担う専用クラスになる。
 *
 * このクラスが依存するのはすべてインターフェース：
 * ・ProductGatewayPort（データアクセスの抽象）
 * ・ProductPresenterPort（出力の抽象）
 *
 * そのため：
 * ・DB をインメモリから JDBC に変えても Interactor は無変更
 * ・コンソール表示を JSON 出力に変えても Interactor は無変更
 * ・テスト時は両方をモックに差し替えてビジネスロジックだけを検証できる
 */
package com.example.clean_architecture.clean.usecase;

import com.example.clean_architecture.clean.entity.Product;

public class AddProductInteractor implements AddProductUseCase {

    // ★ すべてインターフェース型で保持——具体的な実装クラスを知らない
    private final ProductGatewayPort gateway;
    private final ProductPresenterPort presenter;

    // コンストラクタ注入: テスト時はモックを渡せる
    public AddProductInteractor(ProductGatewayPort gateway, ProductPresenterPort presenter) {
        this.gateway = gateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(AddProductRequest request) {
        try {
            // 次の ID を Gateway に問い合わせる（採番ロジックを Gateway に委譲）
            int nextId = gateway.nextId();

            // Product エンティティのコンストラクタでバリデーション
            // 不正な入力なら IllegalArgumentException が発生する
            Product product = new Product(nextId, request.name(), request.price());
            gateway.save(product);

            // ★ 成功結果を戻り値ではなく Presenter（出力ポート）に渡す（Push型）
            // Presenter の実装がコンソールでも JSON でも、ここは変えなくてよい
            ProductResponse response = new ProductResponse(product.id(), product.name(), product.price());
            presenter.presentAddSuccess(response);

        } catch (IllegalArgumentException e) {
            // バリデーションエラーも Presenter 経由で通知する
            presenter.presentError(e.getMessage());
        }
    }
}
