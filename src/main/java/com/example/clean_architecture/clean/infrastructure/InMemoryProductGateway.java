/**
 * 【なぜこのコードを学ぶのか】
 * Gateway はデータアクセスの具体的な実装クラスだ。
 * ProductGatewayPort（Use Case 層のインターフェース）を implements することで、
 * 依存の矢印が Infrastructure → Use Case（内側）を向く。
 *
 * Onion Architecture の InMemoryProductRepository と何が違うのか：
 * ・Onion の Repository: Domain 層のインターフェースを implements する
 * ・Clean の Gateway: Use Case 層のインターフェースを implements する
 * どちらも「依存逆転の原則（DIP）」を実践しているが、インターフェースの置き場所が異なる。
 *
 * `nextId()` メソッドを追加している理由：
 * Onion Architecture では ProductService が ID 採番ロジック（repository.findAll().stream()...max()）
 * を持っていたが、Clean Architecture では Gateway に委譲している。
 * これにより Interactor がより「ビジネスルール」に集中できる。
 *
 * このクラスを JdbcProductGateway に差し替えると DB 対応が完了し、
 * Use Case 層・Adapter 層は一行も変えなくてよい。
 */
package com.example.clean_architecture.clean.infrastructure;

import com.example.clean_architecture.clean.entity.Product;
import com.example.clean_architecture.clean.usecase.ProductGatewayPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ★ Use Case 層のインターフェースを implements——依存の矢印が内側（Use Case 層）を向く
public class InMemoryProductGateway implements ProductGatewayPort {

    // [Java 7 不可] List.of() は Java 9 以降。Java 7 では new ArrayList<>() + add() で書く
    private final List<Product> products = new ArrayList<>(List.of(
            new Product(1, "ノートPC", 80000),
            new Product(2, "マウス", 3000),
            new Product(3, "モニター", 50000)
    ));

    @Override
    public List<Product> findAll() {
        // [Java 7 不可] List.copyOf() は Java 10 以降。Java 7 では new ArrayList<>(products) で代替
        return List.copyOf(products);
    }

    @Override
    // [Java 7 不可] Optional は Java 8 以降
    // [Java 7 不可] Stream API・ラムダ式は Java 8 以降
    public Optional<Product> findById(int id) {
        return products.stream()
                .filter(p -> p.id() == id)
                .findFirst();
    }

    @Override
    public int nextId() {
        // 現在の最大 ID + 1 を次の ID として返す（商品が0件なら 1 から始める）
        // [Java 7 不可] Stream API・メソッド参照は Java 8 以降
        // [Java 7 不可] Optional.orElse() は Java 8 以降
        return products.stream()
                .mapToInt(Product::id)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public void save(Product product) {
        products.add(product);
    }
}
