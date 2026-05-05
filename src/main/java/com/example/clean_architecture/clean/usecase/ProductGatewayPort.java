/**
 * 【なぜこのコードを学ぶのか】
 * データアクセスのインターフェースを Use Case 層に置く——これは Onion Architecture の
 * ProductRepository（Domain 層に置いたインターフェース）と同じ依存逆転の考え方だ。
 *
 * Onion Architecture との違い：
 *   Onion: ProductRepository（Domain 層）← Infrastructure が implements する
 *   Clean: ProductGatewayPort（Use Case 層）← Infrastructure が implements する
 *
 * 「Gateway（ゲートウェイ）」という名前が使われる理由：
 * ・DB・ファイル・API などの外部システムへの「出口（ゲートウェイ）」を抽象化する
 * ・Interactor はこのインターフェースだけを知り、永続化の詳細（SQL・インメモリ）を知らない
 *
 * `nextId()` を追加している理由：
 * ・ID 採番ロジックを Infrastructure 側に委譲するため
 *   （インメモリでは最大値+1、DB では AUTO_INCREMENT を使うなど実装が異なる）
 */
package com.example.clean_architecture.clean.usecase;

import com.example.clean_architecture.clean.entity.Product;

import java.util.List;
import java.util.Optional;

// ★ Use Case 層にデータアクセスのインターフェースを置く（依存逆転の原則）
// Infrastructure 層の InMemoryProductGateway がこれを implements する
public interface ProductGatewayPort {

    List<Product> findAll();

    // [Java 7 不可] Optional は Java 8 以降
    Optional<Product> findById(int id);

    // 次に使うべき ID を返す（採番ロジックを Infrastructure に委譲）
    int nextId();

    void save(Product product);
}
