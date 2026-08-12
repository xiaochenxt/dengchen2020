package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static io.github.dengchen2020.core.utils.FileUtils.getFilenameExtension;

/**
 * {@link MultipartFile}工具类
 * @author xiaochen
 * @since 2025/11/19
 */
@NullMarked
public abstract class MultipartFileUtils {

    /**
     * 写入临时文件，临时文件将在虚拟机退出时自动删除
     *
     * @param file   文件对象
     * @param prefix 文件名前缀，字符长度不能小于3
     * @param directory 临时文件夹 为null则为对应系统的临时文件所在文件夹
     * @return 临时文件对象
     */
    public static File writeTempFile(MultipartFile file, String prefix, @Nullable File directory) throws IOException {
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

}
