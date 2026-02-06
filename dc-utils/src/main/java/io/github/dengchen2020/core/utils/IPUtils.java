package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
     */
    public static long ipv4ToLong(@NonNull String ip) {
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("无效的IP地址: " + ip, e);
        }
        if (!(address instanceof Inet4Address)) throw new IllegalArgumentException("不是IPv4地址：" + ip);
        byte[] bytes = address.getAddress();
        long result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
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
    public static boolean isIpv4(@NonNull String ip) {
        try {
            return InetAddress.getByName(ip) instanceof Inet4Address;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("无效的IP地址：" + ip, e);
        }
    }

    /**
     * 判断字符串是否是ipv6
     * @param ip
     * @return true：是 false：不是
     */
    public static boolean isIpv6(@NonNull String ip) {
        try {
            return InetAddress.getByName(ip) instanceof Inet6Address;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("无效的IP地址：" + ip, e);
        }
    }

}

