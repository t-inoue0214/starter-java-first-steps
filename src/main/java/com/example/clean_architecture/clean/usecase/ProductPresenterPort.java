/**
 * 【なぜこのコードを学ぶのか】
 * これが Clean Architecture の最大の特徴「出力ポート（Output Boundary）」だ。
 *
 * Onion Architecture との最大の違いはここにある：
 *   Onion: ProductPresenter が service.getAllProducts() を呼んで戻り値を受け取る（Pull型）
 *   Clean: Interactor が presenter.presentProducts() を呼んでデータを渡す（Push型）
 *
 * なぜ「戻り値」ではなく「コールバック」で渡すのか：
 * ・Interactor は「結果をどう表示するか」を一切知らなくてよい
 * ・コンソール表示・Web API・CLI への変更が Presenter の差し替えだけで済む
 * ・Interactor が「出力が終わったこと」を知る必要がない（責務の分離）
 *
 * このインターフェースをコンソール版（ConsolePresenter）から
 * JSON 版（JsonPresenter）に差し替えても Interactor は無変更——
 * これが「出力ポート」をインターフェースにする理由だ。
 */
package com.example.clean_architecture.clean.usecase;

import java.util.List;

// ★ Use Case 層（usecase パッケージ）に出力ポートのインターフェースを置く
// Adapter 層の ConsolePresenter がこれを implements する（依存の向きが逆転）
public interface ProductPresenterPort {

    // 全商品一覧を表示する（GetAllProductsInteractor が呼ぶ）
    // [Java 7 不可] List はここでは Java 2 以降だが、引数の List<ProductResponse> はジェネリクス（Java 5）
    void presentProducts(List<ProductResponse> products);

    // 追加成功を表示する（AddProductInteractor が呼ぶ）
    void presentAddSuccess(ProductResponse product);

    // エラーを表示する（バリデーション失敗時に AddProductInteractor が呼ぶ）
    void presentError(String message);
}
