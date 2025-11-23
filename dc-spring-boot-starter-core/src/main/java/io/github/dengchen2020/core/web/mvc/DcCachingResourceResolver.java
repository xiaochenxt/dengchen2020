package io.github.dengchen2020.core.web.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.CachingResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.io.IOException;
import java.util.List;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_BYTE_ARRAY;

/**
 * 缓存{@link ByteArrayResource}而不是原始{@link Resource}，避免重复的磁盘IO读取开销
 * @author xiaochen
 * @since 2025/11/23
 */
public class DcCachingResourceResolver extends CachingResourceResolver {

    private final long maxContentLength;

    public DcCachingResourceResolver(Cache cache, long maxContentLength) {
        super(cache);
        this.maxContentLength = maxContentLength;
    }

    @Override
    protected @Nullable Resource resolveResourceInternal(@Nullable HttpServletRequest request,@NonNull String requestPath,@NonNull List<? extends Resource> locations,@NonNull ResourceResolverChain chain) {
        String key = computeKey(request, requestPath);
        Resource resource = this.getCache().get(key, Resource.class);

        if (resource != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Resource resolved from cache");
            }
            try {
                var data = resource.getContentAsByteArray();
                if (data == EMPTY_BYTE_ARRAY) return null;
            } catch (IOException _) {}
            return resource;
        }

        resource = chain.resolveResource(request, requestPath, locations);
        if (resource != null) {
            try {
                if (resource instanceof FileSystemResource fileSystemResource) {
                    // 如果是文件系统资源，且文件大小超过maxContentLength，则不缓存
                    if (fileSystemResource.contentLength() > maxContentLength) return fileSystemResource;
                }
                ByteArrayResource data = new ByteArrayResource(resource.getContentAsByteArray(), resource.getFilename(), resource.lastModified());
                this.getCache().put(key, data);
                return data;
            } catch (IOException e) {
                this.getCache().put(key, resource);
            }
        } else { // 缓存404
            ByteArrayResource data = new ByteArrayResource(EMPTY_BYTE_ARRAY, "404.html", -1);
            this.getCache().put(key, data);
        }

        return resource;
    }

    /**
     * 实现filename和lastModified以兼容Spring静态资源处理的需要
     */
    private static class ByteArrayResource extends org.springframework.core.io.ByteArrayResource {
        private final String filename;
        private final long lastModified;
        public ByteArrayResource(byte[] content, String filename, long lastModified) {
            super(content);
            this.filename = filename;
            this.lastModified = lastModified;
        }

        public ByteArrayResource(byte[] content, String filename, long lastModified, @Nullable String description) {
            super(content, description);
            this.filename = filename;
            this.lastModified = lastModified;
        }

        @Override
        public byte @NonNull[] getContentAsByteArray() {
            return getByteArray(); // 返回原始内容
        }

        @Override
        public @Nullable String getFilename() {
            return filename;
        }

        @Override
        public long lastModified() {
            return lastModified;
        }
    }

}
