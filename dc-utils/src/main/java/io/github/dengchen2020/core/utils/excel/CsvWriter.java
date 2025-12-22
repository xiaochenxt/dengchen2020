package io.github.dengchen2020.core.utils.excel;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * CSV写入器
 * @author xiaochen
 * @since 2025/10/30
 */
@NullMarked
public class CsvWriter implements AutoCloseable {
    private final OutputStreamWriter writer;
    private final String[] fieldNames; // 字段名顺序
    private final String[] headers;
    private boolean bomWritten = false;

    /**
     * 构建标题行，默认写入编码UTF-8
     */
    public CsvWriter(OutputStream outputStream, Map<String, String> headerMap) {
        this.writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.fieldNames = headerMap.keySet().toArray(EMPTY_STRING_ARRAY);
        this.headers = headerMap.values().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * 构建标题行并指定写入编码
     */
    public CsvWriter(OutputStream outputStream, Map<String, String> headerMap, Charset charset) {
        this.writer = new OutputStreamWriter(outputStream, charset);
        this.fieldNames = headerMap.keySet().toArray(EMPTY_STRING_ARRAY);
        this.headers = headerMap.values().toArray(EMPTY_STRING_ARRAY);
    }

    private void writeBom() throws IOException {
        if (!bomWritten) {
            writer.write('\ufeff'); // UTF-8 BOM
            bomWritten = true;
        }
    }

    /**
     * 写入标题行
     */
    public void writeHeader() throws IOException {
        writeBom();
        writeHeader(headers);
    }

    /**
     * 写入标题行
     */
    public void writeHeader(String[] headers) throws IOException {
        if (headers.length == 0) return;
        writeBom();
        StringBuilder headerSb = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (i > 0) headerSb.append(',');
            // 处理标题中的特殊字符
            if (header.contains(",") || header.contains("\"") || header.contains("\n") || header.contains("\r")) {
                headerSb.append('"').append(header.replace("\"", "\"\"")).append('"');
            } else {
                headerSb.append(header);
            }
        }
        headerSb.append('\n');
        writer.write(headerSb.toString()); // 写入标题行
        flush();
    }

    /**
     * 单条写入
     */
    public void writeRow(Map<String, Object> data) throws IOException {
        writeBom();
        writeRow(data, new StringBuilder());
        flush();
    }

    /**
     * 单条写入
     */
    private void writeRow(Map<String, Object> data, StringBuilder sb) throws IOException {
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            Object value = data.get(fieldName);
            if (value == null) value = "";
            String strValue;
            if (value instanceof BigDecimal decimal) {
                strValue = decimal.toPlainString();
            } else {
                strValue = value.toString();
            }

            if (i > 0) sb.append(',');
            // 数字字段添加\t
            if ((value instanceof Long longValue && longValue > Integer.MAX_VALUE) || isNumeric(strValue)) {
                sb.append('\t');
            }

            // 处理特殊字符
            if (strValue.contains(",") || strValue.contains("\"") || strValue.contains("\n") || strValue.contains("\r")) {
                sb.append('"').append(strValue.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(strValue);
            }
        }
        sb.append('\n');
        writer.write(sb.toString());
        sb.setLength(0);
    }

    /**
     * 批量写入
     */
    public void writeBatch(Iterable<Map<String, Object>> dataList) throws IOException {
        writeBom();
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> data : dataList) {
            writeRow(data, sb);
        }
        flush();
    }

    /**
     * 数字校验
     */
    private boolean isNumeric(String str) {
        if (str.isEmpty()) return false;
        for (int i = 0, len = str.length(); i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
    }
}