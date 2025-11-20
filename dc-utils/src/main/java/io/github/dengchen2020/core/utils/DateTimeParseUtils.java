package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import static io.github.dengchen2020.core.utils.DateTimeUtils.date;
import static io.github.dengchen2020.core.utils.DateTimeUtils.localDateTime;
import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * 日期时间解析工具类
 *
 * @author xiaochen
 * @since 2022/8/19
 */
@NullMarked
public abstract class DateTimeParseUtils {

    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    public static final String[] DATETIME_PARSE_PATTERN = {"yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSX"};

    public static final String[] DATETIME_PARSE_PATTERN_19 = {DEFAULT_DATETIME_PATTERN, "yyyy-MM-dd'T'HH:mm:ss", "yyyy/MM/dd HH:mm:ss"};

    public static final String[] DATETIME_PARSE_PATTERN_16 = {"yyyy-MM-dd HH:mm"};

    public static final String[] DATETIME_PARSE_PATTERN_14 = {"yyyyMMddHHmmss"};

    public static final String[] DATE_PARSE_PATTERN_10 = {DEFAULT_DATE_PATTERN, "yyyy/MM/dd", "yyyy.MM.dd"};

    public static final String[] DATE_PARSE_PATTERN_8 = {"yyyyMMdd"};

    public static final String[] TIME_PARSE_PATTERN_8 = {DEFAULT_TIME_PATTERN, "HH.mm.ss"};

    public static final String[] TIME_PARSE_PATTERN_6 = {"HHmmss"};

    public static final String[] TIME_PARSE_PATTERN_5 = {"HH:mm", "HH.mm"};

    private static final ConcurrentMap<String, DateTimeFormatter> dateTimeFormatterCache = new ConcurrentReferenceHashMap<>();
    private static final ConcurrentMap<String, DateTimeFormatter> zoneDateTimeFormatterCache = new ConcurrentReferenceHashMap<>();

    /**
     * 格式化{@link Date}为日期时间格式的字符串：
     * <p>yyyy-MM-dd HH:mm:ss</p>
     *
     * @param date 时间
     * @return {@link String}
     */
    public static String format(Date date) {
        return format(localDateTime(date));
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
     * <p>yyyy-MM-dd HH:mm:ss</p>
     * @return {@link String}
     */
    public static String format(LocalDateTime localDateTime) {
        return dateTimeFormatterCache.computeIfAbsent(DEFAULT_DATETIME_PATTERN, DateTimeFormatter::ofPattern).format(localDateTime);
    }

    /**
     * 格式化{@link ZonedDateTime}为指定格式字符串
     *
     * @param zonedDateTime 日期时间
     * <p>yyyy-MM-dd HH:mm:ss</p>
     * @return {@link String}
     */
    public static String format(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * 格式化{@link LocalDateTime}为指定格式字符串
     *
     * @param localDateTime 日期时间
     * @param pattern       日期时间格式字符串
     * @return {@link String}
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        return dateTimeFormatterCache.computeIfAbsent(pattern, DateTimeFormatter::ofPattern).format(localDateTime);
    }

    /**
     * 格式化{@link ZonedDateTime}为指定格式字符串
     *
     * @param zonedDateTime 日期时间
     * @param pattern       日期时间格式字符串
     * @return {@link String}
     */
    public static String format(ZonedDateTime zonedDateTime, String pattern) {
        return dateTimeFormatterCache.computeIfAbsent(pattern, DateTimeFormatter::ofPattern).format(zonedDateTime);
    }

    /**
     * 格式化{@link LocalDate}为指定格式字符串
     *
     * @param localDate 日期
     * @param pattern   日期格式字符串
     * @return {@link String}
     */
    public static String format(LocalDate localDate, String pattern) {
        return dateTimeFormatterCache.computeIfAbsent(pattern, DateTimeFormatter::ofPattern).format(localDate);
    }

    /**
     * 格式化{@link LocalTime}为指定格式字符串
     *
     * @param localTime 时间
     * @param pattern   时间格式字符串
     * @return {@link String}
     */
    public static String format(LocalTime localTime, String pattern) {
        return dateTimeFormatterCache.computeIfAbsent(pattern, DateTimeFormatter::ofPattern).format(localTime);
    }

    /**
     * 转换时间字符串为时间
     *
     * @param dateStr 待转换的字符串
     * @return {@link Date}
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, EMPTY_STRING_ARRAY);
    }

    /**
     * 转换时间字符串为时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式，如果不传会使用预置的格式尽可能解析
     * @return {@link Date}
     */
    public static Date parse(String dateStr, String... pattern) {
        if (pattern.length == 0) return date(parseDateTime(dateStr, pattern));
        for (String patternStr : pattern) {
            if (!patternStr.contains("HH")){
                return date(parseDate(dateStr, patternStr));
            }else {
                return date(parseDateTime(dateStr, patternStr));
            }
        }
        throw new IllegalArgumentException(dateStr + "日期时间转换" + Arrays.toString(pattern) + "失败");
    }

    /**
     * 转换日期时间字符串为日期时间
     *
     * @param dateStr 待转换的字符串
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseDateTime(String dateStr) {
        return parseDateTime(dateStr, EMPTY_STRING_ARRAY);
    }

    /**
     * 转换日期时间字符串为日期时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式，如果不传会使用预置的格式尽可能解析
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime parseDateTime(String dateStr, String... pattern) {
        if (dateStr.length() == 20) {
            try {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                // 转换为系统默认时区的LocalDateTime
                return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            } catch (Exception ignored) {}
        }
        if (pattern.length == 0) {
            int len = dateStr.length();
            switch (len) {
                case 19 -> pattern = DATETIME_PARSE_PATTERN_19;
                case 10 -> pattern = DATE_PARSE_PATTERN_10;
                case 8 -> pattern = DATE_PARSE_PATTERN_8;
                case 14 -> pattern = DATETIME_PARSE_PATTERN_14;
                case 16 -> pattern = DATETIME_PARSE_PATTERN_16;
                default -> pattern = DATETIME_PARSE_PATTERN;
            }
            if (len <= 10) {
                for (String s : pattern) {
                    DateTimeFormatter dateTimeFormatter = dateTimeFormatterCache.computeIfAbsent(s, DateTimeFormatter::ofPattern);
                    try {
                        return LocalDate.parse(dateStr, dateTimeFormatter).atStartOfDay();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        for (String s : pattern) {
            DateTimeFormatter dateTimeFormatter = dateTimeFormatterCache.computeIfAbsent(s, DateTimeFormatter::ofPattern);
            try {
                return LocalDateTime.parse(dateStr, dateTimeFormatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException(dateStr + "日期时间转换" + Arrays.toString(pattern) + "失败");
    }

    /**
     * 转换日期时间字符串为日期时间
     *
     * @param dateStr 待转换的字符串
     * @return {@link LocalDateTime}
     */
    public static ZonedDateTime parseZoneDateTime(String dateStr, ZoneId zoneId) {
        return parseZoneDateTime(dateStr, zoneId, EMPTY_STRING_ARRAY);
    }

    /**
     * 转换日期时间字符串为日期时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式，如果不传会使用预置的格式尽可能解析
     * @return {@link LocalDateTime}
     */
    public static ZonedDateTime parseZoneDateTime(String dateStr, ZoneId zoneId, String... pattern) {
        if (pattern.length == 0) {
            int len = dateStr.length();
            switch (len) {
                case 19 -> pattern = DATETIME_PARSE_PATTERN_19;
                case 10 -> pattern = DATE_PARSE_PATTERN_10;
                case 8 -> pattern = DATE_PARSE_PATTERN_8;
                case 14 -> pattern = DATETIME_PARSE_PATTERN_14;
                case 16 -> pattern = DATETIME_PARSE_PATTERN_16;
                default -> pattern = DATETIME_PARSE_PATTERN;
            }
            if (len <= 10) {
                for (String s : pattern) {
                    DateTimeFormatter dateTimeFormatter = zoneDateTimeFormatterCache.computeIfAbsent(s, k -> DateTimeFormatter.ofPattern(k, Locale.US).withZone(zoneId));
                    try {
                        return LocalDate.parse(dateStr, dateTimeFormatter).atStartOfDay(zoneId);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        for (String s : pattern) {
            DateTimeFormatter dateTimeFormatter = zoneDateTimeFormatterCache.computeIfAbsent(s, k -> DateTimeFormatter.ofPattern(k, Locale.US).withZone(zoneId));
            try {
                return ZonedDateTime.parse(dateStr, dateTimeFormatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException(dateStr + "日期时间转换" + Arrays.toString(pattern) + "失败");
    }

    /**
     * 转换日期字符串为日期
     *
     * @param dateStr 待转换的字符串
     * @return {@link LocalDate}
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, EMPTY_STRING_ARRAY);
    }

    /**
     * 转换日期字符串为日期
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式，如果不传会使用预置的格式尽可能解析
     * @return {@link LocalDate}
     */
    public static LocalDate parseDate(String dateStr, String... pattern) {
        if (pattern.length == 0) {
            switch (dateStr.length()) {
                case 10 -> pattern = DATE_PARSE_PATTERN_10;
                case 8 -> pattern = DATE_PARSE_PATTERN_8;
                default -> throw new IllegalArgumentException("预置的日期格式中不支持解析该字符串：" + dateStr + "，请指定格式解析");
            }
        }
        for (String s : pattern) {
            DateTimeFormatter dateTimeFormatter = dateTimeFormatterCache.computeIfAbsent(s, DateTimeFormatter::ofPattern);
            try {
                return LocalDate.parse(dateStr, dateTimeFormatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException(dateStr + "日期转换" + Arrays.toString(pattern) + "失败");
    }

    /**
     * 转换时间字符串为时间
     *
     * @param dateStr 待转换的字符串
     * @return {@link LocalTime}
     */
    public static LocalTime parseTime(String dateStr) {
        return parseTime(dateStr, EMPTY_STRING_ARRAY);
    }

    /**
     * 转换时间字符串为时间
     *
     * @param dateStr 待转换的字符串
     * @param pattern 格式，如果不传会使用预置的格式尽可能解析
     * @return {@link LocalTime}
     */
    public static LocalTime parseTime(String dateStr, String... pattern) {
        if (pattern.length == 0) {
            switch (dateStr.length()) {
                case 8 -> pattern = TIME_PARSE_PATTERN_8;
                case 5 -> pattern = TIME_PARSE_PATTERN_5;
                case 6 -> pattern = TIME_PARSE_PATTERN_6;
                default -> throw new IllegalArgumentException("预置的时间格式中不支持解析该字符串：" + dateStr + "，请指定格式解析");
            }
        }
        for (String s : pattern) {
            DateTimeFormatter dateTimeFormatter = dateTimeFormatterCache.computeIfAbsent(s, DateTimeFormatter::ofPattern);
            try {
                return LocalTime.parse(dateStr, dateTimeFormatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException(dateStr + "时间转换" + Arrays.toString(pattern) + "失败");
    }

}
