/**
 * 【なぜこのコードを学ぶのか】
 * Infrastructure 層は「どうやってデータを永続化するか」の詳細を担当する。
 * 今はインメモリ（ArrayList）だが、このクラスを JdbcProductRepository に差し替えるだけで
 * Application 層・Presentation 層は一行も変えずに DB 対応できる。
 *
 * 第11章（JDBC）で学んだ Connection・PreparedStatement の使い方を
 * この infrastructure 層に閉じ込めるのが正しい設計だ。
 * SQL の詳細が Application 層や Domain 層に漏れ出さない構造を実現する。
 *
 * implements ProductRepository と書くことで、依存の矢印が
 * Infrastructure → Domain（インターフェース）という方向になる。
 * これが依存逆転の原則（DIP）によって「逆転」した依存関係だ。
 */
package com.example.architecture.onion.infrastructure;

import com.example.architecture.onion.domain.Product;
import com.example.architecture.onion.domain.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ★ Domain 層のインターフェースを implements する——依存の矢印は内側（Domain）を向く
// JdbcProductRepository に差し替えるときは、このクラスをまるごと入れ替えるだけでよい
public class InMemoryProductRepository implements ProductRepository {

    // 初期データを事前に投入している（本番では DB から読み込む）
    // [Java 7 不可] List.of() は Java 9 以降。Java 7 では new ArrayList<>() + add() で書く:
    //   List<Product> products = new ArrayList<>();
    //   products.add(new Product(1, "ノートPC", 80000));
    private final List<Product> products = new ArrayList<>(List.of(
            new Product(1, "ノートPC", 80000),
            new Product(2, "マウス", 3000),
            new Product(3, "モニター", 50000)
    ));

    @Override
    public List<Product> findAll() {
        // 外部から直接 products リストを変更されないようにコピーを返す（防御的コピー）
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
    public void save(Product product) {
        products.add(product);
    }
}
