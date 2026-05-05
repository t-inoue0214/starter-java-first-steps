/**
 * 【なぜこのコードを学ぶのか】
 * 「全商品を取得する」というユースケースを入力ポートとして定義する。
 * Clean Architecture では Use Case 1つをインターフェース1つで表現する。
 *
 * Onion Architecture では ProductService に getAllProducts()・findProduct()・addProduct() を
 * すべて書いていたが、Clean Architecture では用途ごとに分離する。
 * この分割により「このクラスが何をするか」が名前から一目瞭然になる。
 * 単一責任の原則（SRP: Single Responsibility Principle）の実践だ。
 */
package com.example.clean_architecture.clean.usecase;

// ★ 入力引数なし（全商品取得は条件なし）
// 出力は ProductPresenterPort.presentProducts() 経由で Presenter に渡される
public interface GetAllProductsUseCase {

    void execute();
}
