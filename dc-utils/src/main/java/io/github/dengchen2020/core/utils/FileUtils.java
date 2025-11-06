package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件工具类
 *
 * @author xiaochen
 * @since 2024/6/13
 */
public abstract class FileUtils {

    /**
     * 写入临时文件，临时文件将在虚拟机退出时自动删除
     *
     * @param file   文件对象
     * @param prefix 文件名前缀，字符长度不能小于3
     * @param directory 临时文件夹 为null则为对应系统的临时文件所在文件夹
     * @return 临时文件对象
     */
    public static File writeTempFile(MultipartFile file, String prefix, File directory) throws IOException {
        String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        File tempFile = File.createTempFile(prefix, filenameExtension == null ? "" : "." + filenameExtension, directory);
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
    public static String getBaseName(String filename) {
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
    public static String addSuffixToFilename(String filename) {
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
    public static String addSuffixToFilename(String filename, String suffix) {
        String baseName = getBaseName(filename);
        String extension = getFilenameExtension(filename, true);
        return baseName + "_" + suffix + extension;
    }

}
