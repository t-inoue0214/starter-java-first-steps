/**
 * 【なぜこのコードを学ぶのか】
 * Use Case への入力データを運ぶ「入力 DTO（Data Transfer Object）」だ。
 * Clean Architecture では生の文字列や int をそのまま Use Case に渡さず、
 * Request オブジェクトに包んでから渡す。
 *
 * この設計の利点：
 * ・Use Case のシグネチャが安定する（引数が増えても Request を変えるだけ）
 * ・Controller が「何を渡すべきか」を型として明示できる
 * ・Onion Architecture の `service.addProduct(String, int)` と比較すると
 *   引数が増えたときの拡張性の違いが見えてくる
 *
 * ★ このクラスは「構造体」であり、ビジネスロジックは持たない。
 *   バリデーションは Entity（Product）のコンストラクタで行う。
 */
package com.example.clean_architecture.clean.usecase;

// [Java 7 不可] Record は Java 16 以降
// Java 7 では通常クラス（フィールド + コンストラクタ + getter）で書く
public record AddProductRequest(String name, int price) {
    // バリデーションはここではなく Product エンティティで行う
    // Request は「入力データの入れ物」に徹する
}
