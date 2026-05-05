/**
 * 【なぜこのコードを学ぶのか】
 * XMLはシステム間連携・設定ファイル・Javaのフレームワーク設定（web.xml等）で広く使われる。
 * CSVより冗長だが「スキーマ（XSD）で構造を強制できる」という強みがある。
 * Javaの標準ライブラリ（JAXP）だけでXMLの生成・解析・スキーマ検証ができる。
 * フレームワークが裏で何をしているかを理解するための基礎を学ぶ。
 */
package com.example.io_and_network;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlProcessing {

    // ---------------------------------------------------------
    // 商品を表すレコード（XML の要素と対応させる）
    // ---------------------------------------------------------
    // [Java 7 不可] record は Java 16 以降。
    //   Java 7 では private static final class Product { private final int id; ... } で代替する
    private record Product(int id, String name, int price, String category) {}

    // ---------------------------------------------------------
    // サンプルデータ
    // ---------------------------------------------------------
    // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う
    private static final java.util.List<Product> PRODUCTS = java.util.List.of(
        new Product(1, "ノートPC",    150_000, "PC"),
        new Product(2, "マウス",        3_500, "周辺機器"),
        new Product(3, "キーボード",    8_000, "周辺機器")
    );

    // ---------------------------------------------------------
    // XML 生成メソッド（DocumentBuilder → Document → Transformer）
    // ---------------------------------------------------------
    private static void generateXml(String filePath)
            throws ParserConfigurationException, TransformerException {

        // Step 1: DocumentBuilder を作成する（DOM ツリーを構築するファクトリ）
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Step 2: 空のDocumentを作成する（XML の木構造のルート）
        Document document = builder.newDocument();
        document.setXmlVersion("1.0");

        // Step 3: ルート要素 <products> を作成してDocumentに追加する
        Element root = document.createElement("products");
        document.appendChild(root);

        // Step 4: 商品ごとに <product id="..."> 要素を作成してルートに追加する
        for (Product product : PRODUCTS) {
            Element productElement = document.createElement("product");
            // id は属性（attribute）として設定する
            productElement.setAttribute("id", String.valueOf(product.id()));

            // 子要素 <name>, <price>, <category> を追加する
            Element nameElement = document.createElement("name");
            nameElement.setTextContent(product.name());
            productElement.appendChild(nameElement);

            Element priceElement = document.createElement("price");
            priceElement.setTextContent(String.valueOf(product.price()));
            productElement.appendChild(priceElement);

            Element categoryElement = document.createElement("category");
            categoryElement.setTextContent(product.category());
            productElement.appendChild(categoryElement);

            root.appendChild(productElement);
        }

        // Step 5: Transformer で Document をファイルに書き出す
        // TransformerFactory は XML のシリアライズ（Java オブジェクト → XML テキスト）を担う
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        // OutputKeys.INDENT = "yes" で階層構造を持つ整形済み XML を出力する
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        // {http://xml.apache.org/xslt}indent-amount: インデントのスペース数を制御する
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(
            new DOMSource(document),
            new StreamResult(new File(filePath))
        );
    }

    // ---------------------------------------------------------
    // XML 解析メソッド（DocumentBuilder.parse → NodeList → テキスト取得）
    // ---------------------------------------------------------
    private static void parseXml(String filePath)
            throws ParserConfigurationException, SAXException, IOException {

        // DocumentBuilder で XML ファイルを解析して DOM ツリーを取得する
        // DOM（Document Object Model）: XML 全体をメモリに木構造として読み込む方式
        // → 小〜中規模のXMLではこれが最も扱いやすい
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filePath));

        // document.normalize(): テキストノードを正規化する（連続するテキストノードを結合する）
        document.getDocumentElement().normalize();

        // getElementsByTagName() で <product> 要素を全件取得する
        NodeList productNodes = document.getElementsByTagName("product");

        System.out.printf("  %-6s %-14s %10s  %-10s%n", "ID", "商品名", "価格(円)", "カテゴリ");
        System.out.println("  " + "-".repeat(45));

        for (int i = 0; i < productNodes.getLength(); i++) {
            // NodeList の要素を Element にキャストして属性・子要素にアクセスする
            Element product = (Element) productNodes.item(i);

            // getAttribute() で属性値（id）を取得する
            String id = product.getAttribute("id");

            // getElementsByTagName().item(0).getTextContent() で子要素のテキストを取得する
            String name     = product.getElementsByTagName("name").item(0).getTextContent();
            String price    = product.getElementsByTagName("price").item(0).getTextContent();
            String category = product.getElementsByTagName("category").item(0).getTextContent();

            System.out.printf("  %-6s %-14s %10s  %-10s%n", id, name, price, category);
        }
    }

    // ---------------------------------------------------------
    // スキーマ検証メソッド（SchemaFactory → Schema → Validator）
    // ---------------------------------------------------------
    private static boolean validateXml(String xmlPath, String xsdPath) {
        try {
            // SchemaFactory でXSD（XML Schema Definition）を読み込む
            // XMLConstants.W3C_XML_SCHEMA_NS_URI: W3C の XML Schema 名前空間を指定する
            SchemaFactory schemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(xsdPath));

            // Validator でXMLを検証する
            // → スキーマに違反している場合は SAXException がスローされる
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
            return true; // 検証OK

        } catch (SAXException e) {
            // スキーマ違反: 具体的なエラーメッセージを表示する
            System.out.println("  検証NG: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("  エラー: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------
    // メインメソッド
    // ---------------------------------------------------------
    public static void main(String[] args) throws Exception {

        String xmlPath     = "tmp_products.xml";
        String xsdPath     = "tmp_products.xsd";
        String invalidPath = "tmp_invalid.xml";

        // ---------------------------------------------------------
        // 1. XML とは何か（概念説明）
        // ---------------------------------------------------------
        System.out.println("=== 1. XML と CSV の違い ===");
        System.out.println();
        System.out.println("  CSV: シンプルで軽量。ただし「形」を強制する仕組みがない");
        System.out.println("  XML: 冗長だが「スキーマ（XSD）で構造を定義・強制」できる");
        System.out.println();
        System.out.println("  XMLの構造:");
        System.out.println("  <?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        System.out.println("  <products>                   ← ルート要素（必ず1つ）");
        System.out.println("    <product id=\"1\">           ← 要素 + 属性");
        System.out.println("      <name>ノートPC</name>    ← 子要素（テキストコンテンツ）");
        System.out.println("      <price>150000</price>");
        System.out.println("    </product>");
        System.out.println("  </products>");
        System.out.println();

        // ---------------------------------------------------------
        // 2. XML 生成（DocumentBuilder → Document → Transformer）
        // ---------------------------------------------------------
        System.out.println("=== 2. XML 生成（DocumentBuilder → Document → Transformer） ===");
        System.out.println();

        generateXml(xmlPath);
        System.out.println("XMLファイルを生成しました: " + xmlPath);
        System.out.println();

        System.out.println("--- 生成されたXMLの内容 ---");
        // [Java 7 不可] Files.readString() は Java 11 以降。Java 7 では BufferedReader を使う
        System.out.println(Files.readString(Path.of(xmlPath), StandardCharsets.UTF_8));

        // ---------------------------------------------------------
        // 3. XML 解析（DOM: parse → NodeList → テキスト取得）
        // ---------------------------------------------------------
        System.out.println("=== 3. XML 解析（DOM: DocumentBuilder.parse → NodeList） ===");
        System.out.println();

        // DOMの特性: XML全体をメモリに読み込む。ランダムアクセスが可能
        // → 大きなファイル（数百MB以上）には不向き（SAX や StAX を使う）
        System.out.println("商品一覧（XMLから読み込み）:");
        parseXml(xmlPath);
        System.out.println();

        // ---------------------------------------------------------
        // 4. スキーマ（XSD）とは何か
        // ---------------------------------------------------------
        System.out.println("=== 4. スキーマ（XSD）とは何か ===");
        System.out.println();
        System.out.println("  XSD（XML Schema Definition）はXMLの「設計図」。");
        System.out.println("  「products 要素の中に product 要素が1つ以上あること」");
        System.out.println("  「price は正の整数であること」などを形式的に定義する。");
        System.out.println();
        System.out.println("  → 送受信するシステム間で「このXMLはこの形で」という契約書になる");
        System.out.println("  → JSONにも JSON Schema という同様の仕組みがある");
        System.out.println();

        // XSD ファイルをプログラムで生成する（自己完結のため）
        String xsdContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                  <xs:element name="products">
                    <xs:complexType>
                      <xs:sequence>
                        <!-- product 要素が1つ以上あることを定義する -->
                        <xs:element name="product" maxOccurs="unbounded">
                          <xs:complexType>
                            <xs:sequence>
                              <xs:element name="name"     type="xs:string"/>
                              <!-- price は正の整数（1以上）でなければならない -->
                              <xs:element name="price"    type="xs:positiveInteger"/>
                              <xs:element name="category" type="xs:string"/>
                            </xs:sequence>
                            <!-- id 属性は必須 -->
                            <xs:attribute name="id" type="xs:positiveInteger" use="required"/>
                          </xs:complexType>
                        </xs:element>
                      </xs:sequence>
                    </xs:complexType>
                  </xs:element>
                </xs:schema>
                """;
        // [Java 7 不可] テキストブロック（"""）は Java 15 以降。Java 7 では String 連結を使う
        // [Java 7 不可] Files.writeString() は Java 11 以降。Java 7 では BufferedWriter を使う
        Files.writeString(Path.of(xsdPath), xsdContent, StandardCharsets.UTF_8);
        System.out.println("XSDファイルを生成しました: " + xsdPath);
        System.out.println();

        // ---------------------------------------------------------
        // 5. スキーマ検証（正常系）
        // ---------------------------------------------------------
        System.out.println("=== 5. スキーマ検証（SchemaFactory → Validator） ===");
        System.out.println();

        System.out.println("--- 正常なXMLを検証する ---");
        boolean validResult = validateXml(xmlPath, xsdPath);
        if (validResult) {
            System.out.println("  検証OK: " + xmlPath + " はスキーマに適合しています");
        }
        System.out.println();

        // ---------------------------------------------------------
        // 6. スキーマ検証（エラー系: 意図的に不正なXMLを検証する）
        // ---------------------------------------------------------
        System.out.println("--- 不正なXMLを検証する（エラーを意図的に発生させる） ---");
        System.out.println();

        // 不正なXML: price が負の値（xs:positiveInteger 違反）
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <products>
                  <product id="99">
                    <name>不正な商品</name>
                    <price>-999</price>
                    <category>エラーテスト</category>
                  </product>
                </products>
                """;
        Files.writeString(Path.of(invalidPath), invalidXml, StandardCharsets.UTF_8);
        System.out.println("不正なXML（price=-999）を生成しました: " + invalidPath);
        System.out.println("スキーマに対して検証します...");
        boolean invalidResult = validateXml(invalidPath, xsdPath);
        if (!invalidResult) {
            System.out.println("  → price は xs:positiveInteger（正の整数）なので -999 は違反");
        }
        System.out.println();

        // ---------------------------------------------------------
        // 7. DOM vs SAX vs StAX（補足）
        // ---------------------------------------------------------
        System.out.println("=== 7. 補足: DOM / SAX / StAX の使い分け ===");
        System.out.println();
        System.out.printf("  %-8s  %-16s  %s%n", "方式", "特徴", "向いている場面");
        System.out.println("  " + "-".repeat(60));
        System.out.printf("  %-8s  %-16s  %s%n", "DOM",  "全体をメモリに読む",  "小〜中規模XML（この章で使用）");
        System.out.printf("  %-8s  %-16s  %s%n", "SAX",  "イベント駆動型",      "大規模XML（数百MB以上）");
        System.out.printf("  %-8s  %-16s  %s%n", "StAX", "プル型、SAXより簡潔", "大規模XMLを逐次処理（Java 6+）");
        System.out.println();

        // ---------------------------------------------------------
        // 8. 後片付け: 一時ファイルを削除する
        // ---------------------------------------------------------
        System.out.println("=== 8. 後片付け ===");
        System.out.println();

        Files.deleteIfExists(Path.of(xmlPath));
        Files.deleteIfExists(Path.of(xsdPath));
        Files.deleteIfExists(Path.of(invalidPath));
        System.out.println("一時ファイルを削除しました: " + xmlPath + ", " + xsdPath + ", " + invalidPath);
    }
}
