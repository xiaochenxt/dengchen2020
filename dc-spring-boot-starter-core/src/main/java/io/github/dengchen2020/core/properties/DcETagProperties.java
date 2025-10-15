package io.github.dengchen2020.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.HashSet;
import java.util.Set;

/**
 * ETag配置
 * @author xiaochen
 * @since 2024/8/6
 */
@ConfigurationProperties("dc.etag")
public class DcETagProperties {

    /**
     * 是否启用ETag
     */
    private boolean enabled = false;

    /**
     * 根据 RFC 7232，设置写入响应的 ETag 值是否应为弱值。
     * <p>应使用 {@code <init-param>} 对于参数名称
     * "writeWeakETag" 在过滤器定义中 {@code web.xml}.
     * @since 4.3
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">RFC 7232 section 2.3</a>
     */
    private boolean writeWeakETag = false;

    /**
     * 文件下载等大数据量场景下不适合生成etag
     */
    private Set<String> ignorePath = new HashSet<>();

    /**
     * 数据长度超过{@code maxLength}时不生成etag
     */
    private DataSize maxLength = DataSize.ofMegabytes(1);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isWriteWeakETag() {
        return writeWeakETag;
    }

    public void setWriteWeakETag(boolean writeWeakETag) {
        this.writeWeakETag = writeWeakETag;
    }

    public Set<String> getIgnorePath() {
        return ignorePath;
    }

    public void setIgnorePath(Set<String> ignorePath) {
        this.ignorePath = ignorePath;
    }

    public DataSize getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(DataSize maxLength) {
        this.maxLength = maxLength;
    }
}
