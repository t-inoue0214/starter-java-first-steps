# 第15章 実装計画: clean_architecture（クリーンアーキテクチャ）

## 概要

第14章で Onion Architecture（レイヤー分割）を学んだ後、同じ「商品管理」機能を Clean Architecture で実装し直す。
両者を比較することで、各アーキテクチャの思想・違い・使い分けを体験で理解する。

## 必須実装内容

- Clean Architecture の4層（Entity・Use Case・Adapter・Infrastructure）をサブパッケージで実装
- 入力ポート（Input Port）としての Use Case インターフェース（`AddProductUseCase` 等）
- 出力ポート（Output Boundary）としての Presenter インターフェース（`ProductPresenterPort`）
- Request DTO（`AddProductRequest`）・Response DTO（`ProductResponse`）による境界の明確化
- Interactor が Presenter を呼ぶ「Push 型」データフローの体験
- 第14章（Onion: Pull型）との比較を `Main.java` の出力で明示する

## 第14章（Onion）vs 第15章（Clean）の主な違い

| 概念 | Onion Architecture（第14章） | Clean Architecture（第15章） |
| --- | --- | --- |
| ビジネスロジックの単位 | `ProductService` クラスのメソッド | `AddProductInteractor`（Use Case 1つ = クラス1つ） |
| 入力の定義 | メソッドシグネチャ `addProduct(String, int)` | Input Port（インターフェース）+ `AddProductRequest` DTO |
| 出力の方法 | 戻り値（`List<Product>`）— Pull 型 | Output Boundary 経由の呼び出し — Push 型 |
| ドメインの越境 | `Product` が層間を直接流通 | `ProductResponse` DTO に変換して流通 |
| Controller の存在 | Presenter がサービスを直接呼ぶ | Controller が入力を DTO に変換してから呼ぶ |

## ファイル構成

```
src/main/java/com/example/clean_architecture/
├── Main.java                                     # エントリーポイント兼 Composition Root
└── clean/
    ├── entity/
    │   └── Product.java                          # エンティティ（第14章と同じ Record）
    ├── usecase/
    │   ├── AddProductRequest.java                # 入力 DTO
    │   ├── ProductResponse.java                  # 出力 DTO
    │   ├── AddProductUseCase.java                # 入力ポート（インターフェース）
    │   ├── GetAllProductsUseCase.java            # 入力ポート（インターフェース）
    │   ├── ProductPresenterPort.java             # 出力ポート（インターフェース）
    │   ├── ProductGatewayPort.java               # データアクセスポート（インターフェース）
    │   ├── AddProductInteractor.java             # ユースケース実装
    │   └── GetAllProductsInteractor.java         # ユースケース実装
    ├── adapter/
    │   ├── ProductController.java                # コントローラー（入力を DTO に変換）
    │   └── ConsolePresenter.java                 # プレゼンター（出力ポートを実装）
    └── infrastructure/
        └── InMemoryProductGateway.java           # データアクセスポートを実装
```

## 特記事項

- `clean/` サブパッケージは例外的に複数ファイル構成になる
- エントリーポイントの `Main.java` を必ず用意すること
- 過度な抽象化・設計パターンは**第14・15章のみ**許可する
- `nextId()` を `ProductGatewayPort` に持たせ、ID 採番を Infrastructure に委譲する
- Composition Root では `presenter` を `interactor` より先に作成すること（Interactor が Presenter を必要とするため）

## 合わせて更新するファイル

- `src/main/java/com/example/architecture/README.md`
  - ナビゲーションバー右セルを「[第15章: クリーンアーキテクチャ →](../clean_architecture/README.md)」に変更

## 実行コマンド

```bash
javac -d out/ $(find src/main/java/com/example/clean_architecture -name "*.java")
java -cp out/ com.example.clean_architecture.Main
```
