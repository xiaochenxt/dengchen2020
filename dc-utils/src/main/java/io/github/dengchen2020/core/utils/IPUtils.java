package io.github.dengchen2020.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * 获取客户端IP
 *
 * @author xiaochen
 * @since 2022/7/13
 */
public abstract class IPUtils {

    private static final Logger log = LoggerFactory.getLogger(IPUtils.class);

    public static final String UNKNOWN = "unknown";
    public static final String LOCALHOST_IP1 = "127.0.0.1";
    private static String localHostIp;
    private static final String REAL_CLIENT_IP_HEADER = System.getProperty("dc.real.client.ip.header", "X-Real-IP");
    // IPv4正则表达式
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    // IPv6正则表达式
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,7}:$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}$|" +
                    "^[0-9a-fA-F]{1,4}:(?::[0-9a-fA-F]{1,4}){1,6}$|" +
                    "^:(?::[0-9a-fA-F]{1,4}){1,7}$|" +
                    "^::(?:ffff(?::0{1,4})?:)?" +
                    "((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}" +
                    "(25[0-5]|(2[0-4]|1?[0-9])?[0-9])$"
    );

    static {
        try {
            localHostIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            if(localHostIp == null){
                localHostIp = LOCALHOST_IP1;
                if (log.isWarnEnabled()) log.warn("获取本机IP失败：{}，回退为：{}", e, localHostIp);
            }
        }
    }

    /**
     * 获取远程客户端ip地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     * </p>
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String ip = null;
        try {
            //获取nginx等代理服务器配置的自定义ip请求头的ip
            ip = request.getHeader(REAL_CLIENT_IP_HEADER);
            if (StringUtils.hasText(ip) && !UNKNOWN.equals(ip)) return ip;
            ip = request.getRemoteAddr();
        } catch (Exception e) {
            log.error("获取客户端IP失败: {}", e.toString());
        }
        return ip;
    }

    /**
     * 获取远程客户端ip地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     * </p>
     */
    public static String getRemoteAddr() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return "";
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return getRemoteAddr(request);
    }

    /**
     * 获取本机IP地址，在云服务器或静态ip等场景中使用
     *
     * @return 本机IP
     */
    public static String getLocalAddr() {
        return localHostIp;
    }

    /**
     * 获取最新的本机IP地址
     * <p>（在自动获取ip的局域网场景中ip可能变化，如果必须获取到最新的ip的话使用，否则使用 {@link IPUtils#getLocalAddr()}），有性能损耗，不过{@link InetAddress#getLocalHost()}自身有缓存</p>
     *
     * @return 本机IP
     */
    public static String getLocalAddrLatest() {
        try {
            String localhostIp = InetAddress.getLocalHost().getHostAddress();
            localHostIp = localhostIp;
            return localhostIp;
        } catch (UnknownHostException e) {
            return localHostIp;
        }
    }

    /**
     * 将 IPv4 地址转换为对应的长整型数字
     * @param ip IPv4 地址字符串，格式为 "xxx.xxx.xxx.xxx"
     * @return 转换后的长整型数字
     * @throws IllegalArgumentException
     */
    public static long ipv4ToLong(String ip) throws IllegalArgumentException {
        String[] ipParts = ip.split("\\.");
        if (ipParts.length != 4) {
            throw new IllegalArgumentException("输入的 IP 地址格式不正确");
        }
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int part = Integer.parseInt(ipParts[i]);
            if (part < 0 || part > 255) {
                throw new IllegalArgumentException("IP 地址的每个部分必须在 0 到 255 之间");
            }
            result = (result << 8) | part;
        }
        return result;
    }

    /**
     * 将长整型数字转换为对应的 IPv4 地址
     * @param ipNumber 长整型数字
     * @return 转换后的 IPv4 地址字符串，格式为 "xxx.xxx.xxx.xxx"
     */
    public static String longToIpv4(long ipNumber) {
        StringBuilder ip = new StringBuilder();
        for (int i = 3; i >= 0; i--) {
            int part = (int) ((ipNumber >> (i * 8)) & 255);
            ip.append(part);
            if (i > 0) ip.append(".");
        }
        return ip.toString();
    }

    /**
     * 判断字符串是否是ipv4
     * @param ip
     * @return true：是 false：不是
     */
    public static boolean isIpv4(String ip) {
        return IPV4_PATTERN.matcher(ip).matches();
    }

    /**
     * 判断字符串是否是ipv6
     * @param ip
     * @return true：是 false：不是
     */
    public static boolean isIpv6(String ip) {
        return IPV6_PATTERN.matcher(ip).matches();
    }

}

