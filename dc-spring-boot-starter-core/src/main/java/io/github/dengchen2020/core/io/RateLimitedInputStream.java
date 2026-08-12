package io.github.dengchen2020.core.io;

import org.jspecify.annotations.NullMarked;
import org.springframework.util.unit.DataSize;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 可限速读取的输入流
 *
 * @author xiaochen
 * @since 2025/4/17
 */
@NullMarked
public class RateLimitedInputStream extends FilterInputStream {

    private long rateLimit;
    private long startTime;
    private long bytesRead;

    public RateLimitedInputStream(InputStream in, DataSize rateLimit) {
        this(in, rateLimit.toBytes());
    }

    public RateLimitedInputStream(InputStream in, long rateLimit) {
        super(in);
        changeRateLimit(rateLimit);
    }

    public void changeRateLimit(DataSize rateLimit) {
        changeRateLimit(rateLimit.toBytes());
    }

    public void changeRateLimit(long rateLimit) {
        this.rateLimit = rateLimit < 16384 ? 16384 : rateLimit;
        this.startTime = System.currentTimeMillis();
        this.bytesRead = 0;
    }

    @Override
    public int read() throws IOException {
        rateLimit(1);
        int b = super.read();
        if (b != -1) bytesRead++;
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        rateLimit(len);
        int bytesRead = super.read(b, off, len);
        if (bytesRead != -1) this.bytesRead += bytesRead;
        return bytesRead;
    }

    private void rateLimit(int bytesToRead) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long expectedTime = (long) ((this.bytesRead + bytesToRead) * 1000.0 / rateLimit);
        if (elapsedTime < expectedTime) {
            try {
                long sleepTime = expectedTime - elapsedTime;
                if (Thread.currentThread().isVirtual()) {
                    Thread.sleep(Math.min(sleepTime, 1000));
                } else {
                    Thread.sleep(Math.min(sleepTime, 12));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
