/**
 * 【なぜこのコードを学ぶのか】
 * 「全商品を取得して表示する」ユースケースの実装クラスだ。
 * Onion Architecture の ProductService.getAllProducts() に相当するが、
 * Clean Architecture ではこの1操作だけを担うクラスになる。
 *
 * Onion との最大の違いはデータの流れ方だ：
 *   Onion: presenter.showAll() → service.getAllProducts() → return List<Product>（Pull型）
 *   Clean: interactor.execute() → gateway.findAll() → presenter.presentProducts(DTOs)（Push型）
 *
 * Entity（Product）を直接 Presenter に渡さず、ProductResponse DTO に変換する理由：
 * ・Product は Entity であり、表示のために変形することは Entity の責務ではない
 * ・Presenter は「何を表示するか（DTO のフィールド）」だけを知ればよい
 * ・将来 Product に内部フィールドを追加しても Presenter に影響しない
 */
package com.example.clean_architecture.clean.usecase;

import com.example.clean_architecture.clean.entity.Product;

import java.util.List;

public class GetAllProductsInteractor implements GetAllProductsUseCase {

    private final ProductGatewayPort gateway;
    private final ProductPresenterPort presenter;

    public GetAllProductsInteractor(ProductGatewayPort gateway, ProductPresenterPort presenter) {
        this.gateway = gateway;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        // Gateway から Entity（Product）のリストを取得する
        List<Product> products = gateway.findAll();

        // ★ Entity を DTO（ProductResponse）に変換してから Presenter に渡す
        // Entity が直接 Presenter に露出しない——これが「境界を越えるときは DTO」の原則
        // [Java 7 不可] Stream API・メソッド参照は Java 8 以降
        // [Java 7 不可] List.toList() は Java 16 以降。Java 7 では Collectors.toList() を使う
        List<ProductResponse> responses = products.stream()
                .map(p -> new ProductResponse(p.id(), p.name(), p.price()))
                .toList();

        // ★ 戻り値ではなく Presenter（出力ポート）に Push する
        presenter.presentProducts(responses);
    }
}
