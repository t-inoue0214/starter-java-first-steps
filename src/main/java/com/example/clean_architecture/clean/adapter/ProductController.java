/**
 * 【なぜこのコードを学ぶのか】
 * Controller は「外部の入力を Use Case が理解できる形式（DTO）に変換する」役割を持つ。
 * Onion Architecture では Presenter が Service を直接呼んでいたが、
 * Clean Architecture では Controller が明示的に分離される。
 *
 * Controller の責務はこの1つだけ：
 * ・生の入力（String・int）を AddProductRequest DTO に変換して Use Case を呼ぶ
 *
 * Controller はビジネスロジックを持たず、バリデーションも行わない。
 * バリデーションは Entity（Product コンストラクタ）の責務であり、
 * エラー通知は ProductPresenterPort（出力ポート）の責務だ。
 *
 * Spring MVC の @Controller / @RestController も同じ思想だ。
 * フレームワークが「HTTP リクエスト → Controller メソッドの引数」に変換し、
 * Controller が DTO に詰め替えて Service（Use Case）を呼ぶ。
 */
package com.example.clean_architecture.clean.adapter;

import com.example.clean_architecture.clean.usecase.AddProductRequest;
import com.example.clean_architecture.clean.usecase.AddProductUseCase;
import com.example.clean_architecture.clean.usecase.GetAllProductsUseCase;

public class ProductController {

    // ★ Use Case のインターフェースにのみ依存——Interactor の実装を知らない
    private final AddProductUseCase addProductUseCase;
    private final GetAllProductsUseCase getAllProductsUseCase;

    public ProductController(AddProductUseCase addProductUseCase,
                             GetAllProductsUseCase getAllProductsUseCase) {
        this.addProductUseCase = addProductUseCase;
        this.getAllProductsUseCase = getAllProductsUseCase;
    }

    // 生の入力（String・int）を AddProductRequest DTO に変換してから Use Case に渡す
    // ★ Controller はここで「変換」だけを行い、ビジネスロジックは書かない
    public void addProduct(String name, int price) {
        AddProductRequest request = new AddProductRequest(name, price);
        addProductUseCase.execute(request);
    }

    // 全商品取得は条件なし——そのまま Use Case を呼ぶだけ
    public void getAllProducts() {
        getAllProductsUseCase.execute();
    }
}
