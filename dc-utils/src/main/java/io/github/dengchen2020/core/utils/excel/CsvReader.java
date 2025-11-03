package io.github.dengchen2020.core.utils.excel;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * CSV读取器
 * @author xiaochen
 * @since 2025/10/30
 */
public class CsvReader implements AutoCloseable {
    private final BufferedReader reader;
    private final String[] fieldNames; // 字段名（与写入时的fieldNames对应）
    private boolean hasNext = true;     // 是否还有下一行数据

    private static final Pattern SCIENTIFIC_NOTATION_PATTERN = Pattern.compile("[+-]?\\d+(\\.\\d+)?[eE][+-]?\\d+");

    /**
     * 构造器（默认读取第一行为表头，与CsvWriter的headerMap对应），自动探测编码
     */
    public CsvReader(InputStream inputStream) throws IOException {
        this.reader = new BufferedReader(createInputStreamReaderWithAutoDetectCharset(inputStream));
        // 读取第一行作为表头
        String headerLine = readAndHandleBom();
        this.fieldNames = parseCsvLine(headerLine);
    }

    /**
     * 构造器（自定义表头，适用于无表头的CSV），自动探测编码
     */
    public CsvReader(InputStream inputStream, String[] customFieldNames) throws IOException {
        this.reader = new BufferedReader(createInputStreamReaderWithAutoDetectCharset(inputStream));
        this.fieldNames = customFieldNames;
    }

    /**
     * 构造器，使用指定编码的读取
     */
    public CsvReader(InputStream inputStream, Charset charset) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(inputStream, charset));
        // 读取第一行作为表头
        String headerLine = readAndHandleBom();
        this.fieldNames = parseCsvLine(headerLine);
    }

    /**
     * 读取表头（返回写入时的headerMap中的value，即列标题）
     */
    public String[] getHeaders() {
        return fieldNames.clone(); // 返回副本，避免外部修改
    }

    /**
     * 单行读取（返回“字段名→值”的Map，自动处理\t前缀和特殊字符）
     */
    @Nullable
    public Map<String, String> readRow() throws IOException {
        if (!hasNext) return null;

        String dataLine = reader.readLine();
        if (dataLine == null) {
            hasNext = false;
            return null;
        }

        // 解析CSV行并匹配字段名
        String[] dataValues = parseCsvLine(dataLine);
        Map<String, String> rowMap = new LinkedHashMap<>(fieldNames.length);
        for (int i = 0; i < fieldNames.length; i++) {
            String value = (i < dataValues.length) ? dataValues[i] : "";
            rowMap.put(fieldNames[i], removeTabPrefix(value));
        }
        return rowMap;
    }

    /**
     * 回调处理，推荐使用
     */
    public void forEach(Consumer<Map<String, String>> consumer) throws IOException {
        while (hasNext) {
            String dataLine = reader.readLine();
            if (dataLine == null) {
                hasNext = false;
                return;
            }
            // 解析CSV行并匹配字段名
            String[] dataValues = parseCsvLine(dataLine);
            Map<String, String> rowMap = new LinkedHashMap<>(fieldNames.length);
            for (int i = 0; i < fieldNames.length; i++) {
                String value = (i < dataValues.length) ? dataValues[i] : "";
                rowMap.put(fieldNames[i], removeTabPrefix(value));
            }
            consumer.accept(rowMap);
        }
    }

    /**
     * 批量读取
     */
    @NonNull
    public List<Map<String, String>> readBatch(int batchSize) throws IOException {
        List<Map<String, String>> batchList = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            Map<String, String> row = readRow();
            if (row == null) break;
            batchList.add(row);
        }
        return batchList;
    }

    /**
     * 跳过指定行数（适用于跳过表头或错误行）
     */
    public void skipLines(int lineCount) throws IOException {
        for (int i = 0; i < lineCount && hasNext; i++) {
            if (reader.readLine() == null) hasNext = false;
        }
    }

    /**
     * 是否还有下一行数据
     */
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * 读取第一行并处理UTF-8 BOM头
     */
    private String readAndHandleBom() throws IOException {
        String line = reader.readLine();
        if (line == null) return "";
        // 移除BOM头（\uFEFF）
        return line.startsWith("\uFEFF") ? line.substring(1) : line;
    }

    /**
     * 解析CSV行（处理双引号包裹、逗号分隔等规则）
     */
    private String[] parseCsvLine(String line) {
        if (line == null || line.isEmpty()) return EMPTY_STRING_ARRAY;

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (inQuotes) {
                if (c == '"') {
                    inQuotes = false;
                } else {
                    currentValue.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    values.add(currentValue.toString());
                    currentValue.setLength(0);
                } else {
                    currentValue.append(c);
                }
            }
        }

        // 添加最后一个字段并处理双引号转义
        values.add(currentValue.toString());
        for (int i = 0; i < values.size(); i++) {
            values.set(i, values.get(i).replace("\"\"", "\""));
        }

        return values.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * 移除写入时添加的\t前缀
     */
    private String removeTabPrefix(String value) {
        if (value == null || value.isEmpty()) return value;
        // 先移除可能的\t前缀
        String processedValue = value;
        if (value.charAt(0) == '\t') processedValue = value.substring(1);

        // 判断是否为科学计数法，若是则转为原始数字字符串
        if (SCIENTIFIC_NOTATION_PATTERN.matcher(processedValue).matches()) {
            return convertScientificToNumber(processedValue);
        }
        return processedValue;
    }

    /**
     * 将科学计数法字符串转为原始数字字符串（如4.21701E+14→421701000000000）
     */
    private String convertScientificToNumber(String scientificStr) {
        try {
            // 先转为BigDecimal避免精度丢失
            BigDecimal bigDecimal = new BigDecimal(scientificStr);
            // 转为纯数字字符串（去掉末尾的.0）
            return bigDecimal.toPlainString().replace(".0", "");
        } catch (NumberFormatException e) {
            // 非数字格式则返回原始值
            return scientificStr;
        }
    }

    /**
     * 数字校验（复用CsvWriter的逻辑）
     */
    private boolean isNumeric(String str) {
        if (str.isEmpty()) return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    /**
     * 长数字校验（判断是否为超过int范围的Long值）
     */
    private boolean isLongValue(String str) {
        if (!isNumeric(str)) return false;
        try {
            long value = Long.parseLong(str);
            return value > Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * 自动探测CSV文件编码（无需手动指定，兼容UTF-8/GBK）
     */
    private InputStreamReader createInputStreamReaderWithAutoDetectCharset(InputStream inputStream) throws IOException {
        // 标记流位置，探测后可重置（避免流被耗尽）
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bis.mark(1024); // 标记前1024字节（足够探测编码）

        // 调用编码探测方法，获取实际编码
        Charset detectedCharset = detectCsvCharset(bis);

        // 重置流到初始位置，避免后续读取丢失数据
        bis.reset();

        // 用探测到的编码创建CsvReader
        return new InputStreamReader(bis, detectedCharset);
    }

    /**
     * 探测CSV编码（优先判断UTF-8 BOM，再试UTF-8，最后默认GBK）
     */
    private Charset detectCsvCharset(BufferedInputStream inputStream) throws IOException {
        byte[] buffer = new byte[3];
        int readLen = inputStream.read(buffer);

        // 1. 判断是否为UTF-8 BOM（EF BB BF）
        if (readLen >= 3 && buffer[0] == (byte)0xEF && buffer[1] == (byte)0xBB && buffer[2] == (byte)0xBF) {
            return StandardCharsets.UTF_8;
        }

        // 2. 尝试用UTF-8解码（无BOM的UTF-8文件）
        if (isUtf8(buffer, readLen)) {
            return StandardCharsets.UTF_8;
        }

        // 3. 默认GBK（WPS常用编码，避免乱码）
        return Charset.forName("GBK");
    }

    /**
     * 辅助判断是不是UTF-8
     */
    private boolean isUtf8(byte[] buffer, int readLen) {
        // UTF-8编码规则：首字节高位0→单字节；110→双字节；1110→三字节，后续字节10开头
        for (int i = 0; i < readLen; ) {
            if ((buffer[i] & 0x80) == 0) { // 单字节（0xxxxxxx）
                i++;
            } else if ((buffer[i] & 0xE0) == 0xC0) { // 双字节（110xxxxx）
                if (i + 1 >= readLen || (buffer[i+1] & 0xC0) != 0x80) {
                    return false;
                }
                i += 2;
            } else if ((buffer[i] & 0xF0) == 0xE0) { // 三字节（1110xxxx）
                if (i + 2 >= readLen || (buffer[i+1] & 0xC0) != 0x80 || (buffer[i+2] & 0xC0) != 0x80) {
                    return false;
                }
                i += 3;
            } else {
                return false; // 不符合UTF-8规则
            }
        }
        return true;
    }

}