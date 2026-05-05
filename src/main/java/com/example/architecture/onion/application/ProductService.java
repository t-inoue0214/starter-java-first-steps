/**
 * 【なぜこのコードを学ぶのか】
 * アプリケーション層は「何をするか（ユースケース）」を記述し、
 * 「どうやってするか（DB 操作・表示）」は一切関知しない。
 * ProductRepository のインターフェースだけに依存するため、
 * テスト時はモック実装に差し替え可能で、DB 切替時もこのクラスは無変更だ。
 *
 * コンストラクタで依存を受け取る設計を「依存注入（DI: Dependency Injection）」と呼ぶ。
 * Spring の @Autowired・@Service も内部でまったく同じことをしている。
 * 自分でフレームワークなしに DI を書くことで、フレームワークが「何を」解決しているかが見える。
 */
package com.example.architecture.onion.application;

import com.example.architecture.onion.domain.Product;
import com.example.architecture.onion.domain.ProductRepository;

import java.util.List;
import java.util.Optional;

public class ProductService {

    // ★ インターフェース型で保持——具体的な実装クラス（InMemoryProductRepository 等）を知らない
    // これが依存逆転の原則（DIP）の実践だ。
    // テストでは「モック実装」に差し替えることで DB なしで検証できる。
    private final ProductRepository repository;

    // コンストラクタ注入（Constructor Injection）: 依存をコンストラクタで受け取る
    // フィールド注入（@Autowired でフィールドに直接）より推奨される理由:
    //   1. テストでモックを渡しやすい
    //   2. final にできる（不変性の保証）
    //   3. 依存が多すぎる設計の臭いに気づきやすい
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    // 全商品一覧を返す
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    // 指定 ID の商品を返す。存在しない場合は空の Optional を返す
    // [Java 7 不可] Optional は Java 8 以降
    public Optional<Product> findProduct(int id) {
        return repository.findById(id);
    }

    // 商品を追加する（ID 自動採番・バリデーションはドメインオブジェクトに委譲）
    public void addProduct(String name, int price) {
        // 現在の最大 ID を取得して次の ID を決める
        // [Java 7 不可] Stream API・メソッド参照は Java 8 以降
        // [Java 7 不可] Optional.orElse() は Java 8 以降
        int nextId = repository.findAll().stream()
                .mapToInt(Product::id)   // Product::id はメソッド参照（Java 8 以降）
                .max()
                .orElse(0) + 1;          // 商品が0件なら ID=1 から始める

        // ★ バリデーションは Product のコンパクトコンストラクタで行われる
        // 「不正な商品は作れない」という制約をドメインオブジェクト自身が持つ設計
        // ここでは IllegalArgumentException が飛ぶ可能性があり、上位層（Presenter）が処理する
        Product product = new Product(nextId, name, price);
        repository.save(product);
    }
}
