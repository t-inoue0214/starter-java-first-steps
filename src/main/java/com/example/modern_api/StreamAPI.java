/**
 * 【なぜこのコードを学ぶのか】
 * 「年収500万円以上のエンジニア部門の社員名一覧を取得する」という同じ処理を
 * 手続き型・OOP・Stream API の3通りで実装する。
 * 3つの結果はまったく同じになる。大切なのは「どれが正解か」ではなく、
 * 「なぜその書き方を選ぶか」を状況で判断できるようになることだ。
 * 第06章で学んだ「プログラミングパラダイムの地図」をコードとして体験する章。
 */
package com.example.modern_api;

import java.util.ArrayList;
import java.util.List;

public class StreamAPI {

    // ---------------------------------------------------------
    // Employee: 社員情報を表す内部クラス
    // ---------------------------------------------------------
    static class Employee {
        private final String name;
        private final String department;
        private final int salary; // 万円単位

        Employee(String name, String department, int salary) {
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public int getSalary() {
            return salary;
        }
    }

    public static void main(String[] args) {

        System.out.println("=== Stream API: 3つのパラダイムで同じ問題を解く ===");
        System.out.println();

        // ---------------------------------------------------------
        // テストデータの準備
        // [Java 7 不可] List.of() は Java 9 以降。Java 7 では Arrays.asList() を使う:
        //   List<Employee> employees = Arrays.asList(
        //       new Employee("田中 太郎", "エンジニア", 600), ...);
        // ---------------------------------------------------------
        List<Employee> employees = List.of(
            new Employee("田中 太郎", "エンジニア", 600),
            new Employee("鈴木 花子", "エンジニア", 480),
            new Employee("佐藤 次郎", "エンジニア", 520),
            new Employee("山田 三郎", "営業",       550),
            new Employee("伊藤 美咲", "エンジニア", 700),
            new Employee("渡辺 健一", "人事",       500)
        );

        System.out.println("【条件】部門=エンジニア かつ 年収=500万円以上");
        System.out.println();

        // ========== Step 1: 手続き型（for ループ + if 文）==========
        // 処理の手順をそのままコードに書く「命令型」の書き方。
        // 「どのように」取り出すかを1ステップずつ記述する。
        // デバッグしやすく、初学者にとって読みやすいのが利点。

        System.out.println("--- Step 1: 手続き型（for ループ + if 文）---");

        List<String> result1 = new ArrayList<>(); // 結果を蓄積する中間変数
        for (Employee e : employees) {
            // 条件に合う社員だけを選別する
            if (e.getDepartment().equals("エンジニア") && e.getSalary() >= 500) {
                result1.add(e.getName());
            }
        }
        System.out.println("Step1 結果: " + result1);
        System.out.println();

        // ========== Step 2: OOP（メソッドに切り出して再利用可能にする）==========
        // 処理をメソッドとして名前を付けることで「何をするか」が明確になる。
        // 同じ条件で別の最低年収を使いたいとき、メソッドを呼ぶだけで済む。

        System.out.println("--- Step 2: OOP（メソッドに切り出して再利用可能にする）---");

        List<Employee> filtered2 = filterEngineers(employees, 500); // 絞り込み
        List<String> result2 = extractNames(filtered2);             // 名前だけ取り出す
        System.out.println("Step2 結果: " + result2);
        System.out.println();

        // ========== Step 3: Stream API（関数型パラダイム）==========
        // 「何をするか」を宣言的に書く書き方。
        // データの加工パイプラインが一目でわかる。
        // 「どのように」は Stream の内部が担い、コードには「何を」だけ書く。

        System.out.println("--- Step 3: Stream API（関数型パラダイム）---");

        // [Java 7 不可] Stream API は Java 8 以降。Java 7 では拡張 for ループで書く:
        //   List<String> result3 = new ArrayList<>();
        //   for (Employee e : employees) {
        //       if (e.getDepartment().equals("エンジニア") && e.getSalary() >= 500) {
        //           result3.add(e.getName());
        //       }
        //   }
        // [Java 7 不可] toList() は Java 16 以降。Java 8〜15 では .collect(Collectors.toList()) を使う
        List<String> result3 = employees.stream()
            .filter(e -> e.getDepartment().equals("エンジニア")) // 部門で絞り込む
            .filter(e -> e.getSalary() >= 500)                   // 年収で絞り込む
            .map(Employee::getName)                               // 名前だけ取り出す
            .toList();                                            // リストに変換する
        System.out.println("Step3 結果: " + result3);
        System.out.println();

        // ---------------------------------------------------------
        // まとめ確認: 3つの結果が同じであることを確認する
        // ---------------------------------------------------------
        System.out.println("=== まとめ確認: 3つの結果は同じか? ===");
        System.out.println("Step1: " + result1);
        System.out.println("Step2: " + result2);
        System.out.println("Step3: " + result3);
        System.out.println("Step1 == Step2: " + result1.equals(result2));
        System.out.println("Step2 == Step3: " + result2.equals(result3));
        System.out.println();

        // 【どれが正解か？】
        // 正解はない。現場のコードは3つが混在するのが現実だ。
        // 手続き型: デバッグしやすい。初学者には読みやすい
        // OOP: ロジックを再利用・テストしやすい
        // 関数型: 宣言的で読みやすい。ただし慣れが必要
        System.out.println("【どれが正解か？】");
        System.out.println("  正解はない。現場のコードは3つが混在するのが現実だ。");
        System.out.println("  手続き型: デバッグしやすい。初学者には読みやすい");
        System.out.println("  OOP    : ロジックを再利用・テストしやすい");
        System.out.println("  関数型  : 宣言的で読みやすい。ただし慣れが必要");
        System.out.println();

        // ========== Stream の代表メソッド体験 ==========

        System.out.println("=== Stream の代表メソッド体験 ===");
        System.out.println();

        // reduce(): 全要素を1つの値に畳み込む
        // [Java 7 不可] Stream API は Java 8 以降。Java 7 では for ループで合計を計算する:
        //   int total = 0;
        //   for (Employee e : employees) { total += e.getSalary(); }
        int totalSalary = employees.stream()
            .map(Employee::getSalary)           // 年収の Stream に変換する
            .reduce(0, Integer::sum);           // 初期値0から合計を計算する
        System.out.println("全社員の年収合計: " + totalSalary + " 万円");

        // distinct() + sorted(): ユニークな部門一覧を並び順で取得する
        // [Java 7 不可] Stream API は Java 8 以降。Java 7 では Set + List でソートする:
        //   Set<String> deptSet = new HashSet<>();
        //   for (Employee e : employees) { deptSet.add(e.getDepartment()); }
        //   List<String> depts = new ArrayList<>(deptSet); Collections.sort(depts);
        List<String> departments = employees.stream()
            .map(Employee::getDepartment) // 部門名の Stream に変換する
            .distinct()                   // 重複を除去する
            .sorted()                     // アルファベット・五十音順に並べる
            .toList();                    // リストに変換する
        System.out.println("ユニークな部門一覧（ソート済み）: " + departments);

        // count(): 条件に合う要素数をカウントする
        // [Java 7 不可] Stream API は Java 8 以降。Java 7 では for ループでカウントする:
        //   int count = 0;
        //   for (Employee e : employees) {
        //       if (e.getDepartment().equals("エンジニア") && e.getSalary() >= 500) count++;
        //   }
        long engineerCount = employees.stream()
            .filter(e -> e.getDepartment().equals("エンジニア"))
            .filter(e -> e.getSalary() >= 500)
            .count(); // 条件に合う件数を返す
        System.out.println("年収500万円以上のエンジニア数: " + engineerCount + " 名");
    }

    // ---------------------------------------------------------
    // filterEngineers: 部門と最低年収で社員リストを絞り込む（Step2 で使用）
    // ---------------------------------------------------------
    private static List<Employee> filterEngineers(List<Employee> employees, int minSalary) {
        List<Employee> result = new ArrayList<>();
        for (Employee e : employees) {
            if (e.getDepartment().equals("エンジニア") && e.getSalary() >= minSalary) {
                result.add(e);
            }
        }
        return result;
    }

    // ---------------------------------------------------------
    // extractNames: 社員リストから名前だけを取り出す（Step2 で使用）
    // ---------------------------------------------------------
    private static List<String> extractNames(List<Employee> employees) {
        List<String> names = new ArrayList<>();
        for (Employee e : employees) {
            names.add(e.getName());
        }
        return names;
    }
}
