package io.github.dengchen2020.core.io.resource;

import io.github.dengchen2020.core.io.RateLimitedInputStream;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 扩展 {@link FileSystemResource} 实现限速
 *
 * @author xiaochen
 * @since 2025/4/17
 */
@NullMarked
public class RateLimitedFileSystemResource extends FileSystemResource {

    private final long rateLimit;

    public RateLimitedFileSystemResource(String path, DataSize rateLimit) {
        this(path, rateLimit.toBytes());
    }

    public RateLimitedFileSystemResource(String path, long rateLimit) {
        super(path);
        this.rateLimit = rateLimit;
    }

    public RateLimitedFileSystemResource(File file, DataSize rateLimit) {
        this(file, rateLimit.toBytes());
    }

    public RateLimitedFileSystemResource(File file, long rateLimit) {
        super(file);
        this.rateLimit = rateLimit;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new RateLimitedInputStream(super.getInputStream(), rateLimit);
    }

}    