package io.github.dengchen2020.core.security.password;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* 使用 BCrypt 强哈希函数的 PasswordEncoder 的实现。客户
 * 可以选择提供“版本”（$2a、$2b、$2y）和“强度”（a.k.a. log轮
 * 在 BCrypt 中）和 SecureRandom 实例。强度参数越大，功越多
 * 必须（指数级）完成才能对密码进行哈希处理。默认值为 10。
 */
public class BCryptPasswordEncoder implements PasswordEncoder {

    private final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2(a|y|b)?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}");

    private final Log logger = LogFactory.getLog(getClass());

    private final int strength;

    private final BCryptVersion version;

    private final SecureRandom random;

    public BCryptPasswordEncoder() {
        this(-1);
    }

    /**
     * @param strength 要使用的日志舍入，介于 4 到 31 之间
     */
    public BCryptPasswordEncoder(int strength) {
        this(strength, null);
    }

    /**
     * @param version bcrypt 的版本，可以是 2a，2b，2y
     */
    public BCryptPasswordEncoder(BCryptVersion version) {
        this(version, null);
    }

    /**
     * @param version bcrypt 的版本，可以是 2a，2b，2y
     * @param random 要使用的安全随机实例
     */
    public BCryptPasswordEncoder(BCryptVersion version, SecureRandom random) {
        this(version, -1, random);
    }

    /**
     * @param strength 要使用的日志舍入，介于 4 到 31 之间
     * @param random 要使用的安全随机实例
     */
    public BCryptPasswordEncoder(int strength, SecureRandom random) {
        this(BCryptVersion.$2A, strength, random);
    }

    /**
     * @param version bcrypt 的版本，可以是 2a，2b，2y
     * @param strength 要使用的日志舍入，介于 4 到 31 之间
     */
    public BCryptPasswordEncoder(BCryptVersion version, int strength) {
        this(version, strength, null);
    }

    /**
     * @param version bcrypt 的版本，可以是 2a，2b，2y
     * @param strength 要使用的日志舍入，介于 4 到 31 之间
     * @param random 要使用的安全随机实例
     */
    public BCryptPasswordEncoder(BCryptVersion version, int strength, SecureRandom random) {
        if (strength != -1 && (strength < BCrypt.MIN_LOG_ROUNDS || strength > BCrypt.MAX_LOG_ROUNDS)) {
            throw new IllegalArgumentException("Bad strength");
        }
        this.version = version;
        this.strength = (strength == -1) ? 10 : strength;
        this.random = random;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        String salt = getSalt();
        return BCrypt.hashpw(rawPassword.toString(), salt);
    }

    private String getSalt() {
        if (this.random != null) {
            return BCrypt.gensalt(this.version.getVersion(), this.strength, this.random);
        }
        return BCrypt.gensalt(this.version.getVersion(), this.strength);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            this.logger.warn("Empty encoded password");
            return false;
        }
        if (!this.BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
            this.logger.warn("Encoded password does not look like BCrypt");
            return false;
        }
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            this.logger.warn("Empty encoded password");
            return false;
        }
        Matcher matcher = this.BCRYPT_PATTERN.matcher(encodedPassword);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Encoded password does not look like BCrypt: " + encodedPassword);
        }
        int strength = Integer.parseInt(matcher.group(2));
        return strength < this.strength;
    }

    /**
     * 存储默认的 bcrypt 版本以用于配置。
     */
    public enum BCryptVersion {

        $2A("$2a"),

        $2Y("$2y"),

        $2B("$2b");

        private final String version;

        BCryptVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return this.version;
        }

    }

}
