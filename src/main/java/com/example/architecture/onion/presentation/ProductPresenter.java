/**
 * 【なぜこのコードを学ぶのか】
 * プレゼンテーション層は「どう見せるか」だけを担当し、ビジネスロジックを持たない。
 * このクラスをコンソール表示から REST API レスポンス（JSON 出力）に変えても、
 * ProductService・Product・ProductRepository には一切触れる必要がない。
 *
 * Web フレームワーク（Spring MVC・Vert.x）のコントローラーや、
 * バッチ処理のメインロジックがこの層に相当する。
 * 「表示の都合」（フォーマット・改行・単位の表記）をここに集め、
 * 他の層を汚染しないのが設計の目標だ。
 */
package com.example.architecture.onion.presentation;

import com.example.architecture.onion.application.ProductService;
import com.example.architecture.onion.domain.Product;

import java.util.List;
import java.util.Optional;

public class ProductPresenter {

    // ★ Application 層（ProductService）にのみ依存——Domain や Infrastructure は直接知らない
    private final ProductService service;

    public ProductPresenter(ProductService service) {
        this.service = service;
    }

    // 全商品一覧を整形して表示する
    public void showAll() {
        System.out.println("【商品一覧】");
        List<Product> productList = service.getAllProducts();
        for (Product p : productList) {
            // %-3d: 左詰め3桁、%-12s: 左詰め12文字、%,d: カンマ区切り整数
            System.out.printf("  ID=%-3d 商品名=%-12s 価格=%,d円%n",
                    p.id(), p.name(), p.price());
        }
        System.out.println();
    }

    // 指定 ID の商品を検索して表示する
    public void showById(int id) {
        System.out.println("【ID=" + id + " の商品を検索】");
        // [Java 7 不可] Optional は Java 8 以降
        Optional<Product> result = service.findProduct(id);
        if (result.isPresent()) {
            Product p = result.get();
            System.out.printf("  見つかった: %s（%,d円）%n", p.name(), p.price());
        } else {
            System.out.println("  ID=" + id + " の商品は存在しません");
        }
        System.out.println();
    }

    // 商品を追加し、結果を表示する（バリデーションエラーはここで受け取る）
    public void addProduct(String name, int price) {
        System.out.printf("【商品を追加: %s %,d円】%n", name, price);
        try {
            service.addProduct(name, price);
            System.out.println("  追加完了");
        } catch (IllegalArgumentException e) {
            // Product コンストラクタのバリデーションエラーをここで表示する
            // ★ エラー表示（プレゼンテーションの責任）をこの層に閉じ込めている
            System.out.println("  [バリデーションエラー] " + e.getMessage());
        }
        System.out.println();
    }
}
