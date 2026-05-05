/**
 * 【なぜこのコードを学ぶのか】
 * Clean Architecture では Use Case を「インターフェース（入力ポート）」として定義する。
 * Onion Architecture の ProductService はクラスだったが、
 * Clean Architecture の AddProductUseCase はインターフェースだ。
 *
 * なぜインターフェースにするのか：
 * ・テスト時に AddProductInteractor を別実装に差し替えられる
 * ・「何ができるか（What）」と「どうやるか（How）」を分離できる
 * ・Controller は "AddProductUseCase" という概念だけを知ればよく、
 *   Interactor の実装詳細を知る必要がない
 *
 * これが「入力ポート（Input Port）」と呼ばれる理由：
 * 外部（Controller）からシステムへの入口（ポート）を定義している。
 */
package com.example.clean_architecture.clean.usecase;

// ★ Use Case をインターフェースとして定義する（Onion では ProductService クラスだった）
// Controller はこのインターフェースだけに依存し、Interactor を直接知らない
public interface AddProductUseCase {

    // 商品を追加するユースケース
    // 出力（結果）は戻り値ではなく ProductPresenterPort（出力ポート）経由で渡される
    void execute(AddProductRequest request);
}
