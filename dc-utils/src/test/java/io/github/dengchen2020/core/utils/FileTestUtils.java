package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author xiaochen
 * @since 2025/12/25
 */
public abstract class FileTestUtils {

    /**
     * 删除文件，如果是文件夹则递归删除里面的所有文件，最后删除文件夹
     * @param file
     * @return 删除的文件列表
     */
    public static FileDeleteResult delete(File file, @Nullable FileFilter fileFilter) {
        if (!file.exists()) return new FileDeleteResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        if (file.isDirectory()) {
            var result = new FileDeleteResult();
            deleteDir(file, fileFilter, result);
            return result;
        } else {
            if (fileFilter == null || fileFilter.accept(file)) {
                if (file.delete()) {
                    return FileDeleteResult.recordSuccessFile(List.of(file.getAbsolutePath()));
                } else {
                    return FileDeleteResult.recordFailFile(List.of(file.getAbsolutePath()));
                }
            }
        }
        return FileDeleteResult.empty();
    }

    private static void deleteDir(File file, @Nullable FileFilter fileFilter, FileDeleteResult deleteResult) {
        if (file.isDirectory()) {
            try (var stream = Files.walk(file.toPath())) {
                stream.sorted((p1, p2) -> -p1.compareTo(p2)).forEach(p -> {
                    var f = p.toFile();
                    if (fileFilter == null || fileFilter.accept(f)) {
                        if (f.isDirectory()) {
                            if (f.delete()) {
                                deleteResult.successDir.add(f.getAbsolutePath());
                            } else {
                                deleteResult.failDir.add(f.getAbsolutePath());
                            }
                        } else {
                            if (f.delete()) {
                                deleteResult.successFile.add(f.getAbsolutePath());
                            } else {
                                deleteResult.failFile.add(f.getAbsolutePath());
                            }
                        }
                    }
                });
            } catch (IOException e) {
                throw new FileDeleteException(deleteResult, "删除文件夹"+file+"失败", e);
            }
        }
    }

    public record FileDeleteResult(List<String> successFile, List<String> failFile, List<String> successDir, List<String> failDir) {

        public FileDeleteResult() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        private FileDeleteResult(List<String> failFile, List<String> successFile) {
            this(successFile, failFile, Collections.emptyList(), Collections.emptyList());
        }

        public static FileDeleteResult empty() {
            return new FileDeleteResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        public static FileDeleteResult recordSuccessFile(List<String> successFile) {
            return new FileDeleteResult(successFile, Collections.emptyList());
        }

        public static FileDeleteResult recordFailFile(List<String> failFile) {
            return new FileDeleteResult(Collections.emptyList(), failFile);
        }

    }

    public static class FileDeleteException extends RuntimeException {
        private final FileDeleteResult fileDeleteResult;
        public FileDeleteException(FileDeleteResult fileDeleteResult, String message, Throwable cause) {
            super(message, cause);
            this.fileDeleteResult = fileDeleteResult;
        }

        public FileDeleteResult getFileDeleteResult() {
            return fileDeleteResult;
        }
    }

    public static FileDeleteResult deleteWithPrint(File file, @Nullable FileFilter fileFilter) {
        var result = delete(file, fileFilter);
        if (!result.successFile().isEmpty()) result.successFile().forEach(f -> System.out.println("删除的文件：" + f));
        if (!result.successDir().isEmpty()) result.successDir().forEach(f -> System.out.println("删除的文件夹：" + f));
        if (!result.failFile().isEmpty()) result.failFile().forEach(f -> System.out.println("删除失败的文件：" + f));
        if (!result.failDir().isEmpty()) result.failDir().forEach(f -> System.out.println("删除失败的文件夹：" + f));
        return result;
    }

}
