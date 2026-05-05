/**
 * 【なぜこのコードを学ぶのか】
 * Use Case からの出力データを運ぶ「出力 DTO（Data Transfer Object）」だ。
 * Onion Architecture では Product エンティティを直接 Presenter に渡していた。
 * Clean Architecture では ProductResponse DTO に変換してから渡す。
 *
 * なぜエンティティをそのまま渡さないのか：
 * ・エンティティに「表示に必要な書式情報」を追加すると Entity が汚染される
 * ・Presenter が Entity のどのフィールドを使っているかが暗黙的になる
 * ・Entity の変更が Presenter に直接影響する（密結合）
 *
 * ★ Response は「出力データの入れ物」に徹し、表示ロジックは持たない。
 *   書式整形（カンマ区切り・フォーマット）は Presenter が担当する。
 */
package com.example.clean_architecture.clean.usecase;

// [Java 7 不可] Record は Java 16 以降
public record ProductResponse(int id, String name, int price) {
    // 生の値だけを保持する。フォーマット（%,d など）は Presenter の責務
}
