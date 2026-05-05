/**
 * 【なぜこのコードを学ぶのか】
 * このインターフェースを「ドメイン層」に置くことが Onion Architecture の核心だ。
 * 「商品を保存・取得する能力が欲しい」というのはビジネスの要求であり、
 * 「どうやって保存するか（DB・ファイル・インメモリ）」はインフラの詳細にすぎない。
 *
 * 通常の依存関係では Application → Infrastructure（DB）という方向になる。
 * しかし ProductRepository インターフェースをドメイン層に置くことで、
 * Infrastructure（InMemoryProductRepository）が Domain（ProductRepository）を実装する形になり、
 * 依存の矢印が逆転する。これが「依存逆転の原則（DIP: Dependency Inversion Principle）」だ。
 *
 * この設計により：
 * ・テスト時はモック実装に差し替えられる
 * ・DB を変えても Application 層は無変更
 * ・ドメイン層はどの技術にも依存しない（最も安定した層）
 */
package com.example.architecture.onion.domain;

import java.util.List;
import java.util.Optional;

// ★ インターフェースを Domain 層に置く——これが DIP（依存逆転の原則）の実践
// Infrastructure 層がこれを implements することで依存の矢印が内側（Domain）を向く
public interface ProductRepository {

    // 全商品を返す
    List<Product> findAll();

    // 指定 ID の商品を返す。存在しない場合は空の Optional を返す（null を返さない）
    // [Java 7 不可] Optional は Java 8 以降。Java 7 では戻り値を null で表現し、呼び出し側で null チェック:
    //   Product findById(int id);  // null を返す可能性がある
    Optional<Product> findById(int id);

    // 商品を保存する（新規追加）
    void save(Product product);
}
