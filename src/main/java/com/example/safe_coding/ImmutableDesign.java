/**
 * 【なぜこのコードを学ぶのか】
 * setter のあるクラスは「いつ・どこで・誰が」変更したかの追跡が難しい。
 * final フィールドと Record でイミュータブルにすることで、
 * 変更の追跡が不要になり、スレッド安全性も自動的に得られる。
 * 第15章（Onion Architecture）でも record Product を使ったが、
 * この章では「なぜイミュータブルにするのか」という設計判断の理由を掘り下げる。
 */
package com.example.safe_coding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutableDesign {

    // ---------------------------------------------------------
    // ========== Before: setter のある可変クラス ==========
    // ---------------------------------------------------------

    /**
     * setter があるユーザープロファイル（Before）。
     * どこからでも値を書き換えられるため、
     * 「いつ・誰が・なぜ」変更したかを追跡することが難しい。
     */
    static class MutableUserProfile {

        private String       name;
        private int          age;
        private List<String> roles;

        MutableUserProfile(String name, int age, List<String> roles) {
            this.name  = name;
            this.age   = age;
            // 防衛的コピーをしていない: 外から渡されたリストをそのまま保持
            // → コンストラクタ呼び出し後に元のリストを変更すると、内部状態も変わってしまう
            this.roles = roles;
        }

        public String       getName()  { return name; }
        public int          getAge()   { return age; }
        public List<String> getRoles() { return roles; } // 内部リストをそのまま返す（危険）

        public void setName(String name) { this.name = name; }
        public void setAge(int age)      { this.age = age; }

        @Override
        public String toString() {
            return "MutableUserProfile{name='" + name + "', age=" + age + ", roles=" + roles + "}";
        }
    }

    // ---------------------------------------------------------
    // ========== After: final フィールドで不変にしたクラス ==========
    // ---------------------------------------------------------

    /**
     * イミュータブルなユーザープロファイル（After）。
     * すべてのフィールドが final で setter がないため、
     * 作成後は状態が変わらない。スレッドセーフも自動的に保証される。
     */
    static final class ImmutableUserProfile {

        private final String       name;
        private final int          age;
        private final List<String> roles;

        ImmutableUserProfile(String name, int age, List<String> roles) {
            this.name  = name;
            this.age   = age;
            // [Java 10+] List.copyOf() でコレクションの防衛的コピーを作成する
            // Java 7 代替: this.roles = new ArrayList<>(roles);
            // copyOf() は変更不可能な独立したコピーを返すため、元リストを変更しても影響しない
            this.roles = List.copyOf(roles);
        }

        public String getName() { return name; }
        public int    getAge()  { return age; }

        public List<String> getRoles() {
            // [Java 10+] List.copyOf() でコピーを返す
            // Java 7 代替: return Collections.unmodifiableList(new ArrayList<>(roles));
            // 呼び出し元がリストを変更しようとしても UnsupportedOperationException になる
            return List.copyOf(roles);
        }

        @Override
        public String toString() {
            return "ImmutableUserProfile{name='" + name + "', age=" + age + ", roles=" + roles + "}";
        }
    }

    // ---------------------------------------------------------
    // ========== After（さらに簡潔）: Record ==========
    // ---------------------------------------------------------

    // [Java 7 不可] Record は Java 16 以降。Java 7 では通常のクラス（コンストラクタ＋getter）で書く
    // Record は final フィールド・コンストラクタ・getter・equals・hashCode・toString を自動生成する
    record UserProfileRecord(String name, int age, List<String> roles) {
        // コンパクトコンストラクタ: フィールドへの代入の前に処理を挟める
        // [Java 7 不可] コンパクトコンストラクタは Record の機能（Java 16+）
        UserProfileRecord {
            // [Java 10+] List.copyOf() で防衛的コピー。Java 7 代替: new ArrayList<>(roles)
            roles = List.copyOf(roles);
        }
    }

    // ---------------------------------------------------------
    // 内部から状態を変更するヘルパーメソッド（Before の問題を示すために使う）
    // ---------------------------------------------------------

    /**
     * プロファイルを「処理する」メソッドのふりをして、内部で name を書き換える。
     * 呼び出し側からはこの中身が見えないため、
     * name が書き換えられることに気づきにくい。
     */
    private static void processProfile(MutableUserProfile profile) {
        // 呼び出し側は知らないが、このメソッドが name を書き換えている
        profile.setName(profile.getName() + "（処理済み）");
    }

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // セクション1: setter の問題（誰が変えたかわからない）
        // ---------------------------------------------------------
        System.out.println("=== 1. Before: setter があると「誰が変えたか」追跡できない ===");
        System.out.println();

        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う
        MutableUserProfile profile = new MutableUserProfile("山田太郎", 30, new ArrayList<>(List.of("USER")));
        System.out.println("  processProfile() 呼び出し前: " + profile);

        processProfile(profile); // 内部で name を書き換えている（呼び出し側からは見えない）

        System.out.println("  processProfile() 呼び出し後: " + profile);
        System.out.println("  → name が変わっている！どこで誰が変えたか、コードを追わないとわからない。");

        // ---------------------------------------------------------
        // セクション2: コレクションをそのまま返す問題
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 2. Before: コレクションをそのまま返すと外から内部状態を変更できる ===");
        System.out.println();

        MutableUserProfile profile2 = new MutableUserProfile("鈴木花子", 25, new ArrayList<>(List.of("USER")));
        System.out.println("  変更前: " + profile2);

        // getRoles() が内部リストをそのまま返すため、呼び出し元がリストを自由に変更できる
        List<String> roles = profile2.getRoles();
        roles.add("ADMIN"); // profile2 の内部リストに直接 ADMIN を追加してしまった

        System.out.println("  変更後: " + profile2);
        System.out.println("  → ADMIN が追加されている！getterを呼んだだけなのに内部が変わってしまった。");

        // ---------------------------------------------------------
        // セクション3: After（final フィールド + 防衛的コピー）
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 3. After: final フィールド + 防衛的コピー ===");
        System.out.println();

        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う
        List<String> originalRoles = new ArrayList<>(List.of("USER"));
        ImmutableUserProfile immutable = new ImmutableUserProfile("佐藤次郎", 35, originalRoles);
        System.out.println("  コンストラクタ呼び出し後: " + immutable);

        // 元のリストを変更しても、immutable には影響しない（防衛的コピー済み）
        originalRoles.add("ADMIN");
        System.out.println("  originalRoles に ADMIN 追加後: " + immutable);
        System.out.println("  → immutable は変わっていない。コンストラクタで独立したコピーを作ったため。");

        // getRoles() が返すリストを変更しようとすると例外が発生する
        System.out.println();
        System.out.println("  getRoles() の戻り値を変更しようとすると...");
        try {
            immutable.getRoles().add("ADMIN");
        } catch (UnsupportedOperationException e) {
            System.out.println("  → UnsupportedOperationException が発生! 変更不可能なリストが返されている。");
        }

        // ---------------------------------------------------------
        // セクション4: Record でさらに簡潔に
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 4. After: Record でイミュータブルを宣言的に表現する ===");
        System.out.println();

        // [Java 7 不可] Record は Java 16 以降
        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う
        UserProfileRecord record = new UserProfileRecord("田中三郎", 28, List.of("USER", "EDITOR"));
        System.out.println("  record: " + record);
        System.out.println("  name=" + record.name() + ", age=" + record.age() + ", roles=" + record.roles());
        System.out.println();
        System.out.println("  Record が自動生成するもの:");
        System.out.println("    final フィールド（外から変更不可）");
        System.out.println("    全フィールドを受け取るコンストラクタ");
        System.out.println("    getter（フィールド名と同名のメソッド: name(), age(), roles()）");
        System.out.println("    equals() / hashCode()（フィールドの値で比較）");
        System.out.println("    toString()（フィールド名と値をフォーマットして返す）");

        // ---------------------------------------------------------
        // セクション5: unmodifiableList と List.copyOf() の違い
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 5. Collections.unmodifiableList() と List.copyOf() の違い ===");
        System.out.println();

        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う
        List<String> source = new ArrayList<>(List.of("A", "B"));

        // Collections.unmodifiableList() は「ラッパー」を返す
        // → ラッパー経由の変更は禁止されるが、元リストを直接変更すると反映される
        List<String> unmodifiable = Collections.unmodifiableList(source);

        source.add("C"); // 元リストに C を追加
        System.out.println("  source に C を追加後 unmodifiable: " + unmodifiable);
        System.out.println("  → C が見えている。元リストの変更が unmodifiable に透過する。");

        // [Java 10+] List.copyOf() は独立したコピーを返す
        // → 元リストを変更しても、コピーには影響しない
        // Java 7 代替: Collections.unmodifiableList(new ArrayList<>(source))
        List<String> copied = List.copyOf(source);

        source.add("D"); // 元リストに D を追加
        System.out.println("  source に D を追加後 copied: " + copied);
        System.out.println("  → D が見えない。List.copyOf() は独立したコピーなので元リストの変更が反映されない。");

        // ---------------------------------------------------------
        // セクション6: まとめ
        // ---------------------------------------------------------
        System.out.println();
        System.out.println("=== 6. まとめ ===");
        System.out.println();
        System.out.println("  setter あり（Mutable）:");
        System.out.println("    → いつ・どこで・誰が変更したか追跡が難しい");
        System.out.println("    → スレッド間で状態が競合する可能性がある");
        System.out.println();
        System.out.println("  final フィールド + 防衛的コピー（Immutable）:");
        System.out.println("    → 作成後は変更されないため追跡不要");
        System.out.println("    → スレッドセーフが自動的に保証される");
        System.out.println();
        System.out.println("  Record（Java 16+）:");
        System.out.println("    → イミュータブルを「宣言的」に1行で表現できる");
        System.out.println("    → equals/hashCode/toString もコンパイラが自動生成する");
    }
}
