/**
 * 【なぜこのコードを学ぶのか】
 * Presenter は「Use Case からの出力データ（DTO）を画面に表示できる形式に変換する」役割を持つ。
 * ProductPresenterPort（出力ポート）を implements し、Interactor から呼ばれる。
 *
 * Onion Architecture との違い：
 *   Onion: ProductPresenter が service.getAllProducts() を呼んでデータを取りに行く（Pull型）
 *   Clean: ConsolePresenter は呼ばれるのを待ち、渡された DTO を表示するだけ（Push型）
 *
 * 「Humble Object（ハンブルオブジェクト）パターン」：
 * Presenter はテストが難しいコンソール出力などを担当するため、
 * ロジックを極力減らして「謙虚（Humble）」なオブジェクトにする。
 * 書式整形（%,d などのフォーマット）は Presenter の仕事だが、
 * 「何を表示するか」の判断は Interactor が行う。
 *
 * このクラスを JsonPresenter（JSON 出力）に差し替えても Interactor は無変更——
 * これが出力ポートをインターフェースにした理由だ。
 */
package com.example.clean_architecture.clean.adapter;

import com.example.clean_architecture.clean.usecase.ProductPresenterPort;
import com.example.clean_architecture.clean.usecase.ProductResponse;

import java.util.List;

// ★ Adapter 層が Use Case 層のインターフェース（ProductPresenterPort）を implements する
// 依存の矢印: ConsolePresenter → ProductPresenterPort（Use Case 層）
public class ConsolePresenter implements ProductPresenterPort {

    @Override
    public void presentProducts(List<ProductResponse> products) {
        System.out.println("【商品一覧】");
        for (ProductResponse p : products) {
            // 書式整形（%,d: カンマ区切り整数）はここ Presenter の責務
            System.out.printf("  ID=%-3d 商品名=%-12s 価格=%,d円%n",
                    p.id(), p.name(), p.price());
        }
        System.out.println();
    }

    @Override
    public void presentAddSuccess(ProductResponse product) {
        System.out.printf("【追加完了】%s（%,d円）を登録しました%n%n",
                product.name(), product.price());
    }

    @Override
    public void presentError(String message) {
        System.out.println("【バリデーションエラー】" + message);
        System.out.println();
    }
}
