package io.github.dengchen2020.ip.service.impl.xdb;

import io.github.dengchen2020.ip.model.IpInfo;
import io.github.dengchen2020.ip.service.IpService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Util;
import org.lionsoul.ip2region.xdb.Version;
import org.lionsoul.ip2region.xdb.XdbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;

import static io.github.dengchen2020.core.utils.StrUtils.getValue;

/**
 * xdb读取实现，缓存了整个xdb数据用于安全的并发查询，因此会增加内存占用（数据包越大占用越多）
 * <p>详见：<a href="https://github.com/lionsoul2014/ip2region/tree/master/binding/java">ip2region xdb java 查询客户端实现</a></p>
 * @author xiaochen
 * @since 2023/5/6
 */
@NullMarked
public class IpXdbServiceImpl implements IpService, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(IpXdbServiceImpl.class);

    private Searcher searcherIpv4;

    @Nullable private Searcher searcherIpv6;

    public IpXdbServiceImpl(String ipv4DataPath,@Nullable String ipv6DataPath, boolean verify) {
        loadData(ipv4DataPath, ipv6DataPath, verify);
    }

    public synchronized void loadData(String ipv4DataPath,@Nullable String ipv6DataPath, boolean verify) {
        File ipv4File = new File(ipv4DataPath);
        if (ipv4File.isFile() && ipv4File.exists()) {
            try {
                Searcher newSearcherIpv4 = Searcher.newWithBuffer(Version.IPv4, Searcher.loadContentFromFile(ipv4DataPath));
                if (verify) Searcher.verifyFromFile(ipv4DataPath);
                log.info("ipv4数据包加载完成，占用{}MB", DataSize.ofBytes(ipv4File.length()).toMegabytes());
                var oldSearcherIpv4 = searcherIpv4;
                searcherIpv4 = newSearcherIpv4;
                if (oldSearcherIpv4 != null) {
                    try {
                        oldSearcherIpv4.close();
                    } catch (IOException e) {
                        log.error("关闭旧的ipv4查询对象失败：", e);
                    }
                }
            } catch (IOException | XdbException e) {
                throw new IllegalArgumentException("ipv4数据包加载失败", e);
            }
        } else {
            throw new IllegalArgumentException("未找到ipv4数据包，请将其放置在" + ipv4File.getAbsolutePath());
        }
        if (ipv6DataPath == null) return;
        File ipv6File = new File(ipv6DataPath);
        if (ipv6File.isFile() && ipv6File.exists()) {
            try {
                Searcher newSearcherIpv6 = Searcher.newWithBuffer(Version.IPv6, Searcher.loadContentFromFile(ipv6DataPath));
                if (verify) Searcher.verifyFromFile(ipv6DataPath);
                log.info("ipv6数据包加载完成，占用{}MB", DataSize.ofBytes(ipv6File.length()).toMegabytes());
                var oldSearcherIpv6 = searcherIpv6;
                searcherIpv6 = newSearcherIpv6;
                if (oldSearcherIpv6 != null) {
                    try {
                        oldSearcherIpv6.close();
                    } catch (IOException e) {
                        log.error("关闭旧的ipv6查询对象失败：", e);
                    }
                }
            } catch (IOException | XdbException e) {
                throw new IllegalArgumentException("ipv6数据包加载失败", e);
            }
        } else {
            if (log.isDebugEnabled()) log.debug("未找到ipv6数据包，如需查询ipv6信息，请将其放置在{}，如不需要可忽略", ipv6File.getAbsolutePath());
        }
    }

    @Override
    public IpInfo getInfo(String ip) {
        if (!StringUtils.hasText(ip)) return defaultInfo(ip);
        try {
            String[] ipInfo;
            var data = Util.parseIP(ip);
            if (data.length == 16) {
                if (searcherIpv6 == null) {
                    log.error("未加载ipv6数据包，无法查询ipv6信息");
                    return defaultInfo(ip);
                }
                ipInfo = searcherIpv6.search(data).split("\\|");
            } else {
                ipInfo = searcherIpv4.search(data).split("\\|");
            }
            if (ipInfo.length > 0) {
                return new IpInfo(ip, getValue(ipInfo, 0), getValue(ipInfo, 1),
                        getValue(ipInfo, 2), getValue(ipInfo, 3), getValue(ipInfo, 4),
                        getValue(ipInfo, 5), getValue(ipInfo, 6), getValue(ipInfo, 7),
                        getValue(ipInfo, 8), getValue(ipInfo, 9), getValue(ipInfo, 10),
                        getValue(ipInfo, 11), getValue(ipInfo, 12), getValue(ipInfo, 13))
                        ;
            }
        } catch (Exception e) {
            log.error("获取ip信息失败：{}", e.toString());
        }
        return defaultInfo(ip);
    }

    @Override
    public void destroy() {
        try {
            searcherIpv4.close();
        } catch (IOException e) {
            log.error("关闭ipv4查询对象失败：", e);
        }
        if (searcherIpv6 != null) {
            try {
                searcherIpv6.close();
            } catch (IOException e) {
                log.error("关闭ipv6查询对象失败：", e);
            }
        }
    }

    /**
     * 查询不到时的默认返回信息
     */
    private IpInfo defaultInfo(String ip){return new IpInfo(ip);}

}
