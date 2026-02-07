package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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

    /**
     * 校验文件名是否安全合规
     * @param filename 待校验的文件名
     * @return 校验通过的文件名
     * @throws IllegalArgumentException 当包含路径遍历和禁止的字符时抛出
     */
    public static String getSafeFilename(@Nullable String filename) {
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("文件名不能为空");
        var safeFilename = filename.trim();
        int len = safeFilename.length();
        // 检测路径遍历和禁止的字符
        for (int i = 0; i < len; i++) {
            char c = safeFilename.charAt(i);
            if (isForbiddenChar(c)) throw new IllegalArgumentException("文件名：" + safeFilename + "，包含禁止的字符: '" + c + "' (位置: " + i + ")");
            // 检查连续点号序列
            if (c == '.') {
                // 统计连续的点号数量
                int dotCount = 1;
                int startIndex = i;
                int nextIndex = i + 1;
                while (nextIndex < len && safeFilename.charAt(nextIndex) == '.') {
                    dotCount++;
                    nextIndex++;
                }
                // 拦截所有连续的点号序列（2个及以上）
                if (dotCount >= 2) {
                    String sequence = safeFilename.substring(startIndex, nextIndex);
                    throw new IllegalArgumentException("文件名：" + safeFilename + "，包含非法的连续点号序列: '" + sequence + "' (位置: " + startIndex + ")");
                }
                // 跳过已处理的点号
                i = nextIndex - 1;
            }
        }
        return safeFilename;
    }
    
    /**
     * 判断字符是否为禁止的字符
     */
    private static boolean isForbiddenChar(char c) {
        return c == '/' || c == '\\' || c == ':' || c == '*' || 
               c == '?' || c == '"' || c == '<' || c == '>' || 
               c == '|' || c == '\0';
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
