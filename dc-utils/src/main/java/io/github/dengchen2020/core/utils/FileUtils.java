package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具类
 *
 * @author xiaochen
 * @since 2024/6/13
 */
@NullMarked
public abstract class FileUtils {

    /**
     * 写入临时文件，临时文件将在虚拟机退出时自动删除
     *
     * @param file   文件对象
     * @param prefix 文件名前缀，字符长度不能小于3
     * @param directory 临时文件夹 为null则为对应系统的临时文件所在文件夹
     * @return 临时文件对象
     */
    public static File writeTempFile(MultipartFile file, String prefix,@Nullable File directory) throws IOException {
        String filenameExtension = getFilenameExtension(file.getOriginalFilename());
        File tempFile = File.createTempFile(prefix, "." + filenameExtension, directory);
        file.transferTo(tempFile);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * 写入临时文件
     *
     * @param file 文件对象
     * @param prefix 文件名前缀，字符长度不能小于3
     * @return 临时文件对象
     */
    public static File writeTempFile(MultipartFile file, String prefix) throws IOException {
        return writeTempFile(file, prefix, null);
    }

    /**
     * 写入临时文件
     *
     * @param file 文件对象
     * @return 临时文件对象
     */
    public static File writeTempFile(MultipartFile file) throws IOException {
        return writeTempFile(file, "tmp-");
    }

    /**
     * 返回文件名称不包含扩展名，例如 a.jpg -> a
     * <p>注意：如果文件名中包含目录，不会去除</p>
     * @param filename 文件名
     * @return 不包含扩展名的文件名
     */
    public static String getBaseName(@Nullable String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return i != -1 ? filename.substring(0, i) : filename;
    }

    /**
     * 返回文件名中的扩展名，例如：a.jpg -> jpg
     */
    public static String getFilenameExtension(@Nullable String filename) {
        return getFilenameExtension(filename, false);
    }

    /**
     * 返回文件名中的扩展名，例如：a.jpg -> jpg
     */
    private static String getFilenameExtension(@Nullable String filename, boolean withExtensionSeparator) {
        if (filename == null) return "";
        int extIndex = filename.lastIndexOf(".");
        if (extIndex == -1) return "";
        return withExtensionSeparator ? filename.substring(extIndex) : filename.substring(extIndex + 1);
    }

    /**
     * 文件名添加后缀，例如：a.jpg -> a_123456.jpg
     * @param filename 文件名
     * @return 新文件名
     */
    public static String addSuffixToFilename(@Nullable String filename) {
        String baseName = getBaseName(filename);
        String extension = getFilenameExtension(filename, true);
        return baseName + "_" + UUID.randomUUID() + extension;
    }

    /**
     * 文件名添加后缀，例如：a.jpg -> a_123456.jpg
     * @param filename 文件名
     * @param suffix 字符串
     * @return 新文件名
     */
    public static String addSuffixToFilename(@Nullable String filename, String suffix) {
        String baseName = getBaseName(filename);
        String extension = getFilenameExtension(filename, true);
        return baseName + "_" + suffix + extension;
    }

    // 只允许字母、数字、小数点、下划线、&、逗号、(、)、[、]、（、）、【、】（禁止其他所有特殊字符和路径符号）
    private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._&,=$@+%#()（）\\[\\]【】]");

    /**
     * 校验文件名是否合法（仅含字母、数字、小数点、下划线、&、逗号、(、)、[、]、（、）、【、】），如果合法则正常返回，否则抛出异常
     * @param filename 待校验的文件名
     * @return 校验通过的文件名
     * @throws IllegalArgumentException 当包含非法字符时抛出
     */
    public static String getSafeFilename(@Nullable String filename) {
        if (filename == null || filename.isEmpty()) throw new IllegalArgumentException("文件名不能为空");
        Matcher matcher = INVALID_FILENAME_PATTERN.matcher(filename);
        if (matcher.find()) throw new IllegalArgumentException("文件名不合规，包含不允许的字符：" + matcher.group());
        return filename;
    }

    /**
     * 真实路径是否安全
     * @param basePathStr，例如 /upload/img，调用者必须保证安全（非用户输入）
     * @param filePathStr，例如 20251112/a.jpg
     * @return 真实路径在{@code basePathStr}目录内说明安全，否则不安全
     */
    public static boolean isSafePath(String basePathStr, String filePathStr) {
        Path basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
        Path realPath = basePath.resolve(filePathStr).normalize();
        return realPath.startsWith(basePath);
    }

    /**
     * 获取安全的真实路径，如果不安全则抛出异常
     * @param basePathStr，例如 /upload/img，调用者必须保证安全（非用户输入）
     * @param filePathStr，例如 20251112/a.jpg
     * @return 真实路径在{@code basePathStr}目录内则有效，否则无效
     * @throws IllegalArgumentException 当真实路径不安全时抛出
     */
    public static Path getSafePath(String basePathStr, String filePathStr) {
        Path basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
        Path realPath = basePath.resolve(filePathStr).normalize();
        if (!realPath.startsWith(basePath)) throw new IllegalArgumentException("非法路径");
        return realPath;
    }

}
