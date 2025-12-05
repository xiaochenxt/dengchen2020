package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间处理工具类
 *
 * @author xiaochen
 * @since 2022/8/19
 */
@NullMarked
public abstract class DateTimeUtils {

    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * {@link LocalDateTime}转换为{@link Date}，使用系统默认时区
     * @param localDateTime {@link LocalDateTime}
     * @return {@link Date}
     */
    public static Date date(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * {@link ZonedDateTime}转换为{@link Date}，使用系统默认时区
     * @param zonedDateTime {@link ZonedDateTime}
     * @return {@link Date}
     */
    public static Date date(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * {@link LocalDateTime}转换为{@link Date}
     * @param localDateTime localDateTime
     * @param zoneId 时区ID
     * @return {@link Date}
     */
    public static Date date(LocalDateTime localDateTime, ZoneId zoneId) {
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    /**
     * {@link LocalDate}转换为{@link Date}，使用系统默认时区
     * @param localDate {@link LocalDate}
     * @return {@link Date}
     */
    public static Date date(LocalDate localDate) {
        return date(localDate.atStartOfDay());
    }

    /**
     * {@link Date}转换为{@link LocalDateTime}，使用系统默认时区
     * @param date date
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime localDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * {@link Date}转换为{@link LocalDateTime}，使用系统默认时区
     * @param date date
     * @param zoneId 时区ID
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime localDateTime(Date date, ZoneId zoneId) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId);
    }

    /**
     * 获取开始时间
     *
     * @param date 时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime beginOfDay(Date date) {
        return beginOfDay(localDateTime(date));
    }

    /**
     * 获取开始时间
     *
     * @param localDateTime 时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime beginOfDay(LocalDateTime localDateTime) {
        return localDateTime.with(LocalTime.MIN);
    }

    /**
     * 获取结束时间
     *
     * @param date 时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime endOfDay(Date date) {
        return endOfDay(localDateTime(date));
    }

    /**
     * 获取结束时间
     *
     * @param localDateTime 时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime endOfDay(LocalDateTime localDateTime) {
        return localDateTime.with(LocalTime.MAX);
    }

    /**
     * 获取日期当天的结束时间
     *
     * @param localDate 日期
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime endOfDay(LocalDate localDate) {
        return LocalDateTime.of(localDate,LocalTime.MAX);
    }

    /**
     * 第二天开始时间
     *
     * @param date 时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime beginOfNextDay(Date date) {
        return beginOfNextDay(localDateTime(date));
    }

    /**
     * 第二天开始时间
     *
     * @param localDateTime 日期时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime beginOfNextDay(LocalDateTime localDateTime) {
        return localDateTime.with(LocalTime.MIN).plusDays(1);
    }

    /**
     * 昨天开始时间
     *
     * @param date 时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime beginOfYestDay(Date date) {
        return beginOfYestDay(localDateTime(date));
    }

    /**
     * 昨天开始时间
     *
     * @param localDateTime 日期时间
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime beginOfYestDay(LocalDateTime localDateTime) {
        return localDateTime.with(LocalTime.MIN).plusDays(-1);
    }

    /**
     * 时间差，取绝对值
     *
     * @param date  时间1
     * @param date2 时间2
     * @return long
     */
    public static long betweenMills(Date date, Date date2) {
        return Math.abs(date2.getTime() - date.getTime());
    }

    /**
     * 时间差，取绝对值
     *
     * @param localDateTime  日期时间1
     * @param localDateTime2 日期时间2
     * @return long
     */
    public static long betweenMills(LocalDateTime localDateTime, LocalDateTime localDateTime2) {
        return Math.abs(timestamp(localDateTime) - timestamp(localDateTime2));
    }

    /**
     * 时间差，取绝对值
     *
     * @param zonedDateTime  日期时间1
     * @param zonedDateTime2 日期时间2
     * @return long
     */
    public static long betweenMills(ZonedDateTime zonedDateTime, ZonedDateTime zonedDateTime2) {
        return Math.abs(timestamp(zonedDateTime) - timestamp(zonedDateTime2));
    }

    /**
     * 时间差，取绝对值
     *
     * @param instant  日期时间1
     * @param instant2 日期时间2
     * @return long
     */
    public static long betweenMills(Instant instant, Instant instant2) {
        return Math.abs(timestamp(instant) - timestamp(instant2));
    }

    /**
     * 时间差，取绝对值
     *
     * @param date  时间1
     * @param date2 时间2
     * @return long
     */
    public static long betweenSecond(Date date, Date date2) {
        return Math.abs(date2.getTime() - date.getTime()) / 1000;
    }

    /**
     * 时间差，取绝对值
     *
     * @param localDateTime  日期时间1
     * @param localDateTime2 日期时间2
     * @return long
     */
    public static long betweenSecond(LocalDateTime localDateTime, LocalDateTime localDateTime2) {
        return Math.abs(timestamp(localDateTime) - timestamp(localDateTime2)) / 1000;
    }

    /**
     * 时间差，取绝对值
     *
     * @param zonedDateTime  日期时间1
     * @param zonedDateTime2 日期时间2
     * @return long
     */
    public static long betweenSecond(ZonedDateTime zonedDateTime, ZonedDateTime zonedDateTime2) {
        return Math.abs(timestamp(zonedDateTime) - timestamp(zonedDateTime2)) / 1000;
    }

    /**
     * 时间差，取绝对值
     *
     * @param instant  日期时间1
     * @param instant2 日期时间2
     * @return long
     */
    public static long betweenSecond(Instant instant, Instant instant2) {
        return Math.abs(timestamp(instant) - timestamp(instant2)) / 1000;
    }

    /**
     * 是否是同一天
     * @param localDateTime1 日期时间1
     * @param localDateTime2 日期时间2
     * @return true：是同一天，false：不是同一天
     */
    public static boolean isSameDay(LocalDateTime localDateTime1, LocalDateTime localDateTime2) {
        return localDateTime1.toLocalDate().isEqual(localDateTime2.toLocalDate());
    }

    /**
     * 是否是同一天
     * @param zonedDateTime1 日期时间1
     * @param zonedDateTime2 日期时间2
     * @param zoneId 时区 使用相同的时区才能准确判断是否在同一天
     * @return true：是同一天，false：不是同一天
     */
    public static boolean isSameDay(ZonedDateTime zonedDateTime1, ZonedDateTime zonedDateTime2, ZoneId zoneId) {
        return zonedDateTime1.withZoneSameInstant(zoneId).toLocalDate().isEqual(zonedDateTime2.withZoneSameInstant(zoneId).toLocalDate());
    }

    /**
     * 是否是同一天
     * @param instant1 日期时间1
     * @param instant2 日期时间2
     * @param zoneId 时区 使用相同的时区才能准确判断是否在同一天
     * @return true：是同一天，false：不是同一天
     */
    public static boolean isSameDay(Instant instant1, Instant instant2, ZoneId zoneId) {
        return instant1.atZone(zoneId).toLocalDate().isEqual(instant2.atZone(zoneId).toLocalDate());
    }

    /**
     * 是否是同一天
     * @param localDate1 日期1
     * @param localDate2 日期2
     * @return true：是同一天，false：不是同一天
     */
    public static boolean isSameDay(LocalDate localDate1, LocalDate localDate2) {
        return localDate1.isEqual(localDate2);
    }

    /**
     * {@link ZonedDateTime}转时间戳，使用系统默认时区
     * @param timestamp 时间戳
     * @return {@link ZonedDateTime}
     */
    public static ZonedDateTime zonedDateTime(long timestamp){
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
    }

    /**
     * {@link ZonedDateTime}转时间戳
     * @param timestamp 时间戳
     * @param zoneId 时区ID
     * @return {@link ZonedDateTime}
     */
    public static ZonedDateTime zonedDateTime(long timestamp, ZoneId zoneId){
        return Instant.ofEpochMilli(timestamp).atZone(zoneId);
    }

    /**
     * 时间戳转{@link ZonedDateTime}
     * @param zonedDateTime {@link ZonedDateTime}
     * @return 时间戳
     */
    public static long timestamp(ZonedDateTime zonedDateTime){
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 时间戳转{@link LocalDateTime}
     * @param timestamp 时间戳
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime localDateTime(long timestamp){
        return zonedDateTime(timestamp).toLocalDateTime();
    }

    /**
     * {@link LocalDateTime}转时间戳，使用系统默认时区
     * @param localDateTime {@link LocalDateTime}
     * @return 时间戳
     */
    public static long timestamp(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * {@link LocalDateTime}转时间戳
     * @param localDateTime {@link LocalDateTime}
     * @param zoneId 时区ID
     * @return 时间戳
     */
    public static long timestamp(LocalDateTime localDateTime, ZoneId zoneId){
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    /**
     * {@link LocalDateTime}转时间戳
     * @param instant {@link Instant}
     * @return 时间戳
     */
    public static long timestamp(Instant instant){
        return instant.toEpochMilli();
    }

    /**
     * 判断当前时间是否在指定的开始时间和结束时间范围内，支持跨天，开始时间和结束时间自身不包含在范围内
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param currentTime 当前时间
     * @return 如果当前时间在范围内返回 true，否则返回 false
     */
    public static boolean isWithinTimeRange(LocalTime startTime, LocalTime endTime, LocalTime currentTime) {
        if (startTime.equals(endTime)) {
            // 00:00:00-00:00:00 代表一整天，返回true
            return startTime.equals(LocalTime.MIDNIGHT);
        }
        // 正常时间范围，不跨天
        if (startTime.isBefore(endTime)) {
            return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
        } else {
            // 跨天时间范围
            return currentTime.isAfter(startTime) || currentTime.isBefore(endTime);
        }
    }

    /**
     * 返回当前的UTC时区时间
     * @return {@link Instant}
     */
    public static Instant nowUTC() {
        return Instant.now();
    }

    /**
     * 格式化{@link Date}为指定格式字符串
     *
     * @param date    时间
     * @param pattern 时间格式字符串
     * @return {@link String}
     */
    public static String format(Date date, String pattern) {
        return format(localDateTime(date), pattern);
    }

    /**
     * 格式化{@link LocalDateTime}为指定格式字符串
     *
     * @param localDateTime 日期时间
     * @param pattern       日期时间格式字符串
     * @return {@link String}
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localDateTime);
    }

    /**
     * 格式化{@link ZonedDateTime}为指定格式字符串
     *
     * @param zonedDateTime 日期时间
     * @param pattern       日期时间格式字符串
     * @return {@link String}
     */
    public static String format(ZonedDateTime zonedDateTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(zonedDateTime);
    }

    /**
     * 格式化{@link LocalDate}为指定格式字符串
     *
     * @param localDate 日期
     * @param pattern   日期格式字符串
     * @return {@link String}
     */
    public static String format(LocalDate localDate, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localDate);
    }

    /**
     * 格式化{@link LocalTime}为指定格式字符串
     *
     * @param localTime 时间
     * @param pattern   时间格式字符串
     * @return {@link String}
     */
    public static String format(LocalTime localTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localTime);
    }

    /**
     * 转换时间字符串为时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式
     * @return {@link Date}
     */
    public static Date parse(String dateStr, String pattern) {
        return date(parseDateTime(dateStr, pattern));
    }

    /**
     * 转换日期时间字符串为日期时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseDateTime(String dateStr, String pattern) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 转换日期时间字符串为日期时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式
     * @return {@link LocalDateTime}
     */
    public static ZonedDateTime parseZoneDateTime(String dateStr, ZoneId zoneId, String pattern) {
        return ZonedDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern).withZone(zoneId));
    }

    /**
     * 转换日期字符串为日期
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式
     * @return {@link LocalDate}
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        return  LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 转换时间字符串为时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式
     * @return {@link LocalTime}
     */
    public static LocalTime parseTime(String dateStr, String pattern) {
        return LocalTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

}
