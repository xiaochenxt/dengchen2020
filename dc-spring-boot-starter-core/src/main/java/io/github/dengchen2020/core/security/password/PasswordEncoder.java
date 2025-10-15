package io.github.dengchen2020.core.security.password;

/**
 * 用于对密码进行编码的服务接口。
 * 首选实现是 {@code BCryptPasswordEncoder}.
 *
 * @author xiaochen
 * @since 2023/3/31
 */
public interface PasswordEncoder {

    /**
     * 对原始密码进行编码。通常，一个好的编码算法会应用 SHA-1 或
     * 更大的哈希值与 8 字节或更大的随机生成的盐相结合。
     */
    String encode(CharSequence rawPassword);

    /**
     * 验证从存储中获取的编码密码是否与提交的原始密码匹配
     * 密码也被编码后。如果密码匹配，则返回 true，如果密码匹配，则返回 false。
     * 他们没有。存储的密码本身永远不会被解码。
     * @param rawPassword 要编码和匹配的原始密码
     * @param encodedPassword 存储中要比较的编码密码
     * @return 如果编码后的原始密码与存储
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);

    /**
     * 如果应再次对编码的密码进行编码以提高安全性，则返回 true，
     * 否则为假。默认实现始终返回 false。
     * @param encodedPassword 要检查的编码密码
     * @return 如果应再次编码密码以提高安全性，则为 true，否则为假。
     */
    default boolean upgradeEncoding(String encodedPassword) {
        return false;
    }

}
