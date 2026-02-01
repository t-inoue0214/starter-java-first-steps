# starter-java-first-steps

環境構築不要！ブラウザだけで学べるJavaプログラミング超入門講座へようこそ。

このリポジトリは、**GitHub Codespaces** を使って、どのプログラミング言語も使ったことがない人でもプログラミング言語の学習をスタートできるようにしたいと思い作っています。

超入門レベルであれば、どのプログラミング言語でも良いのですが、仕事では多くの現場がJava言語です。そのため、ここではJava言語を使って簡単なプログラミングの経験を積み、より高度な学習にすすめることを目指します。

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/USERNAME/REPOSITORY_NAME)

---

## 💻 1. 開発環境 (Development Environment)

この勉強会では **GitHub Codespaces** を使用します。

面倒な環境構築は不要です。ブラウザさえあれば、すぐに学習を始められます。

1. **GitHubにログイン** してください（アカウントがない場合は作成してください）。

1. このリポジトリをフォークするため、右上の`fork`をクリックします。

    ![start-fork](./assets/start-fork.png)

1. `Create fork`ボタンをクリックして、フォーク（自分のアカウントにコピーして新しいリポジトリを作成）します。

    ![select-fork-option](./assets/select-fork-option.png)

1. `Codespace`を起動するため、`Code`タブに移動し、右上にある緑色の`code`のプルダウンメニューを開き、`Codespace`タブを開き、`Create codespace on main`をクリックします。

    ![success-setting](./assets/start-code-space.png)

1. `Codespace`の生成にはしばらく時間がかかるため、しばらく待ちます。

    ![create-now](./assets/create-now.png)

1. `VSCode`が起動しますが、画面左下が`リモートを開いています...`の間は待ちます。

    ![vscode-setup-now](./assets/vscode-setup-now.png)

1. 画面左下が`Codespace`になった場合は、`Codespace`が起動完了しました

    ![vscode-setup-finish](./assets/vscode-setup-finish.png)

環境が立ち上がったら、左側のファイル一覧から学習したい章のフォルダを開いてください。

### Codespaces利用上の注意

- `Github`の`Codespaces`を利用します。`Codespaces`は設定によってはコストがかかるものなので [Codespace の利用上の注意](./CODE_SPACES_SERICE.md) はよく確認してください。
- コストをかけないためにも、セキュリティの意味でも、使い終わったら [停止方法](./CODE_SPACES_SERICE.md#3-停止方法) に従って停止することを推奨します。

---

## 2. 学習の始め方

1. **環境の準備を待つ**
   - ブラウザでVS Codeが起動します。
   - 初回はJavaのセットアップや日本語化のために1〜2分ほどかかります。
   - 左下のステータスバーなどが落ち着くまで少し待ちましょう。

2. **学習スタート！**
   - 左側のファイル一覧から `01_introduction` フォルダを開きます。
   - `README.md` をクリックして開き、解説を読みながら進めてください。
   - `README.md` を右クリックして「プレビューを開く (Open Preview)」を選ぶと読みやすくなります。

---

## 📚 3. この講座で学ぶこと

Javaの「書き方」だけでなく、「なぜそう書くのか？」という仕組みや、プログラミングの楽しさを重視しています。

| 章 | タイトル | 学ぶ内容 |
| :--- | :--- | :--- |
| **01** | **[Javaに触れてみよう](./01_introduction/)** | Hello World, JShell, 実行方法 |
| **02** | **[データと型](./02_variables_and_types/)** | 変数, プリミティブ型, キャストの罠 |
| **03** | **[プログラムの流れを作る](./03_control_flow/)** | if文, for文, 配列, FizzBuzz |
| **04** | **[クラスとオブジェクト](./04_class_and_objects/)** | クラス設計, フィールド, メソッド, new |
| **05** | **[便利な道具箱とミニゲーム](./05_practical_java/)** | List, Map, Scanner, 数当てゲーム作成 |

---

## 💻 4. 開発環境について

この講座は以下の環境で動作するように設定されています（自動構築されます）。

- **OS:** Linux (Debian)
- **Java:** OpenJDK 21
- **Editor:** VS Code Web (日本語化済み)
- **Extensions:**
  - Extension Pack for Java
  - Japanese Language Pack
