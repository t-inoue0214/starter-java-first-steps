/**
 * 【なぜこのコードを学ぶのか】
 * java.util.Date と SimpleDateFormat には2つの深刻な問題がある。
 * 1. SimpleDateFormat はスレッドアンセーフ（詳細は第12章で体験する）
 * 2. Calendar の API は冗長で、「翌月を求めるだけ」で5行必要になる
 *
 * Java 8 で導入された java.time パッケージは不変オブジェクト（イミュータブル）で
 * スレッドセーフかつ直感的な API を提供する。
 * LocalDate / LocalDateTime / ZonedDateTime の使い分けを体験する。
 */
package com.example.modern_api;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class DateTimeApi {

    public static void main(String[] args) {

        System.out.println("=== 日付・時刻 API の進化 ===");
        System.out.println();

        // ========== Before: java.util.Date / Calendar / SimpleDateFormat の問題 ==========
        // Java 1.0 から存在する旧来の日付 API。
        // 問題1: Date クラスの toString() が読みにくい形式を返す
        // 問題2: Calendar は「翌月を求めるだけ」で複数行必要
        // 問題3: SimpleDateFormat はスレッドアンセーフ（第12章で詳述）

        System.out.println("--- Before: java.util.Date / Calendar / SimpleDateFormat ---");

        // 現在時刻の取得（読みにくい形式で表示される）
        Date now = new Date();
        System.out.println("現在時刻 (Date.toString): " + now); // 例: Thu Jan 01 09:00:00 JST 2025

        // 翌月を求めるのに Calendar を使うと冗長になる
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1); // 月を1つ進める
        Date nextMonth = cal.getTime();
        System.out.println("翌月 (Calendar)         : " + nextMonth);

        // SimpleDateFormat でフォーマットする
        // 注意: SimpleDateFormat はスレッドアンセーフ。
        // 複数スレッドで同じインスタンスを共有すると壊れる（第12章で体験する）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String formatted = sdf.format(now);
        System.out.println("フォーマット (SimpleDateFormat): " + formatted);
        System.out.println("→ Calendar は冗長、SimpleDateFormat はスレッドアンセーフという問題がある");
        System.out.println();

        // ========== After: java.time の直感的な API ==========
        // [Java 7 不可] java.time パッケージは Java 8 以降。
        //   Java 7 では java.util.Date / Calendar で代替する。
        // java.time のオブジェクトは不変（イミュータブル）なのでスレッドセーフ。

        System.out.println("--- After: java.time の直感的な API ---");
        System.out.println();

        // ---------------------------------------------------------
        // LocalDate: 日付のみ（時刻・タイムゾーンなし）
        // 誕生日・締め切り・記念日など「時刻が不要な日付」に使う
        // [Java 7 不可] LocalDate は Java 8 以降
        // ---------------------------------------------------------
        System.out.println("[ LocalDate: 日付のみ ]");
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);         // 翌日（1行で書ける）
        LocalDate deadline = LocalDate.of(2025, 12, 31); // 特定の日付を指定する
        long daysUntilDeadline = ChronoUnit.DAYS.between(today, deadline); // 日数差を計算する
        System.out.println("今日          : " + today);
        System.out.println("明日          : " + tomorrow);
        System.out.println("締め切り      : " + deadline);
        System.out.println("締め切りまで  : " + daysUntilDeadline + " 日");
        System.out.println();

        // ---------------------------------------------------------
        // LocalDateTime: 日付 + 時刻（タイムゾーンなし）
        // ログのタイムスタンプや「同じタイムゾーン内」の日時計算に使う
        // [Java 7 不可] LocalDateTime / DateTimeFormatter は Java 8 以降
        // ---------------------------------------------------------
        System.out.println("[ LocalDateTime: 日付 + 時刻 ]");
        LocalDateTime nowLocal = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        System.out.println("現在日時: " + nowLocal.format(formatter));
        System.out.println("翌月    : " + nowLocal.plusMonths(1).format(formatter)); // Calendar より簡潔
        System.out.println();

        // ---------------------------------------------------------
        // ZonedDateTime: タイムゾーンあり
        // 異なるタイムゾーン間でのデータ連携（国際対応アプリ）に使う
        // [Java 7 不可] ZonedDateTime / ZoneId は Java 8 以降
        // ---------------------------------------------------------
        System.out.println("[ ZonedDateTime: タイムゾーンあり ]");
        ZonedDateTime tokyoTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime utcTime = tokyoTime.withZoneSameInstant(ZoneId.of("UTC")); // 同じ瞬間をUTCで表す
        DateTimeFormatter zoneFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");
        System.out.println("東京時刻: " + tokyoTime.format(zoneFormatter));
        System.out.println("UTC時刻 : " + utcTime.format(zoneFormatter));
        System.out.println();

        // ---------------------------------------------------------
        // Period: 日付の差分（年・月・日の単位）
        // 「X年Y月Z日後」のような人間向けの期間表現に使う
        // [Java 7 不可] Period は Java 8 以降
        // ---------------------------------------------------------
        System.out.println("[ Period: 日付の差分（年・月・日） ]");
        Period period = Period.between(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 6, 15));
        System.out.println("2024/01/01 から 2025/06/15 までの期間: "
            + period.getYears() + "年 "
            + period.getMonths() + "ヶ月 "
            + period.getDays() + "日");
        System.out.println();

        // ---------------------------------------------------------
        // Duration: 時間の差分（時・分・秒の単位）
        // 処理時間の計測や「何時間何分作業したか」の計算に使う
        // [Java 7 不可] Duration は Java 8 以降
        // ---------------------------------------------------------
        System.out.println("[ Duration: 時間の差分（時・分・秒） ]");
        LocalDateTime workStart = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime workEnd   = LocalDateTime.of(2025, 1, 1, 17, 30);
        Duration duration = Duration.between(workStart, workEnd);
        System.out.println("作業時間: " + duration.toHours() + "時間 " + (duration.toMinutes() % 60) + "分");
        System.out.println();

        System.out.println("=== 使い分けまとめ ===");
        System.out.println("  LocalDate     : 日付のみ（誕生日・締め切り）");
        System.out.println("  LocalDateTime : 日付+時刻（ログ・同一タイムゾーン内の計算）");
        System.out.println("  ZonedDateTime : タイムゾーンあり（グローバルサービス・API連携）");
        System.out.println("  Period        : 年・月・日単位の差分（人間向け表示）");
        System.out.println("  Duration      : 時・分・秒単位の差分（処理時間計測）");
        System.out.println();
        System.out.println("【第12章への橋渡し】");
        System.out.println("  java.time のオブジェクトは不変（イミュータブル）。");
        System.out.println("  SimpleDateFormat と違い、複数スレッドで共有しても安全だ。");
        System.out.println("  → スレッドセーフの詳細は第12章「並行処理・非同期処理の基礎」で体験する");
    }
}
