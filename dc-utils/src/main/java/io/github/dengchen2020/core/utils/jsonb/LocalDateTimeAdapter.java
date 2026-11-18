package io.github.dengchen2020.core.utils.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.github.dengchen2020.core.utils.DateTimeUtils;

import java.time.LocalDateTime;

/**
 * LocalDateTime json adapter
 * @author xiaochen
 * @since 2026/11/18
 */
public final class LocalDateTimeAdapter implements JsonAdapter<LocalDateTime> {

    public static final LocalDateTimeAdapter INSTANCE = new LocalDateTimeAdapter();

    private final String dateTimeFormat;

    LocalDateTimeAdapter() {
        this.dateTimeFormat = null;
    }

    public static LocalDateTimeAdapter create(String dateTimeFormat) {
        if (dateTimeFormat == null) return INSTANCE;
        return new LocalDateTimeAdapter(dateTimeFormat);
    }

    LocalDateTimeAdapter(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public LocalDateTime fromJson(JsonReader reader) {
        return DateTimeUtils.parseDateTime(reader.readString());
    }

    public void toJson(JsonWriter writer, LocalDateTime value) {
        var str = dateTimeFormat != null ? DateTimeUtils.format(value, dateTimeFormat) : value.toString();
        writer.value(str);
    }

    public String toString() {
        return "JsonAdapter(LocalDateTime)";
    }

}
