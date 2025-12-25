package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    /**
     * 复制文件夹
     * @param sourceDirectory 源文件夹
     * @param targetDirectory 目标文件夹
     */
    public static void copyDirectory(File sourceDirectory, File targetDirectory) throws IOException {
        if (!sourceDirectory.exists()) throw new IllegalArgumentException("源文件夹不存在");
        Path sourceDirectoryPath = sourceDirectory.toPath();
        Path targetDirectoryPath = targetDirectory.toPath();
        try (var stream = Files.walk(sourceDirectoryPath)) {
            stream.forEach(source -> {
                Path target = targetDirectoryPath.resolve(sourceDirectoryPath.relativize(source));
                if (Files.isDirectory(source)) {
                    try {
                        Files.createDirectories(target);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("文件夹复制失败", e);
                    }
                } else {
                    try {
                        Files.copy(source, target);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("文件夹复制失败", e);
                    }
                }
            });
        }
    }

}
