package io.github.dengchen2020.ip.service.impl.xdb;

import io.github.dengchen2020.ip.model.IpInfo;
import io.github.dengchen2020.ip.service.IpService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.InvalidConfigException;
import org.lionsoul.ip2region.service.Ip2Region;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Util;
import org.lionsoul.ip2region.xdb.XdbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

import static io.github.dengchen2020.core.utils.StrUtils.getValue;

/**
 * xdb读取实现，与旧版相比优化很多，需要2025年10月后生成的新的xdb数据包（新的xdb数据包体积大幅瘦身但不影响数据质量）
 * <p>详见：<a href="https://github.com/lionsoul2014/ip2region/tree/master/binding/java">ip2region xdb java 查询客户端实现</a></p>
 * @author xiaochen
 * @since 2025/12/2
 */
@NullMarked
public class IpXdbV2ServiceImpl implements IpService, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(IpXdbV2ServiceImpl.class);

    private final Ip2Region ip2Region;
    private final boolean supportIpv6;

    public IpXdbV2ServiceImpl(String ipv4DataPath, @Nullable String ipv6DataPath, boolean verify) throws IOException, XdbException, InterruptedException, InvalidConfigException {
        File ipv4File = new File(ipv4DataPath);
        if (!ipv4File.isFile() || !ipv4File.exists()) throw new IllegalArgumentException("未找到ipv4数据包，请将其放置在" + ipv4File.getAbsolutePath());
        if (verify) Searcher.verifyFromFile(ipv4File);
        Config ipv6Config = null;
        if (ipv6DataPath != null) {
            File ipv6File = new File(ipv6DataPath);
            if (ipv6File.isFile() && ipv6File.exists()) {
                ipv6Config = Config.custom().setXdbFile(ipv6File).setCachePolicy(Config.BufferCache).asV6();
                if (verify) Searcher.verifyFromFile(ipv6File);
            }
        }
        supportIpv6 = ipv6Config != null;
        this.ip2Region = Ip2Region.create(Config.custom().setXdbFile(ipv4File).setCachePolicy(Config.BufferCache).asV4(), ipv6Config);
    }

    public IpXdbV2ServiceImpl(String ipv4DataPath, @Nullable String ipv6DataPath, int v4Searchers, int v6Searchers, boolean verify) throws IOException, XdbException, InterruptedException, InvalidConfigException {
        File ipv4File = new File(ipv4DataPath);
        if (!ipv4File.isFile() || !ipv4File.exists()) throw new IllegalArgumentException("未找到ipv4数据包，请将其放置在" + ipv4File.getAbsolutePath());
        if (verify) Searcher.verifyFromFile(ipv4File);
        Config ipv6Config = null;
        if (ipv6DataPath != null) {
            File ipv6File = new File(ipv6DataPath);
            if (ipv6File.isFile() && ipv6File.exists()) {
                ipv6Config = Config.custom().setXdbFile(ipv6File).setSearchers(v6Searchers).asV6();
                if (verify) Searcher.verifyFromFile(ipv6File);
            }
        }
        supportIpv6 = ipv6Config != null;
        this.ip2Region = Ip2Region.create(Config.custom().setXdbFile(ipv4File).setSearchers(v4Searchers).asV4(), ipv6Config);
    }

    @Override
    public IpInfo getInfo(String ip) {
        if (!StringUtils.hasText(ip)) return defaultInfo(ip);
        try {
            var data = Util.parseIP(ip);
            if (data.length == 16 && !supportIpv6) {
                log.error("未加载ipv6数据包，无法查询ipv6信息");
                return defaultInfo(ip);
            }
            String[] ipData = ip2Region.search(data).split("\\|");
            return convert(ip, ipData);
        } catch (Exception e) {
            log.error("获取ip信息失败：{}", e.toString());
        }
        return defaultInfo(ip);
    }

    protected IpInfo convert(String ip, String[] data) {
        if (data.length == 4) { // 简化版
            return new IpInfo(ip, getValue(data, 0), getValue(data, 1),
                    getValue(data, 2), getValue(data, 3));
        } else if (data.length == 5) { // 标准版
            return new IpInfo(ip, getValue(data, 0), getValue(data, 1),
                    getValue(data, 2), getValue(data, 3), getValue(data, 4));
        } else { // 满载版
            return new IpInfo(ip, getValue(data, 0), getValue(data, 1),
                    getValue(data, 2), getValue(data, 3), getValue(data, 4),
                    getValue(data, 5), getValue(data, 6), getValue(data, 7),
                    getValue(data, 8), getValue(data, 9), getValue(data, 10),
                    getValue(data, 11), getValue(data, 12), getValue(data, 13),
                    getValue(data, 14), getValue(data, 15));
        }
    }

    @Override
    public void destroy() throws InterruptedException {
        ip2Region.close();
    }

    /**
     * 查询不到时的默认返回信息
     */
    private IpInfo defaultInfo(String ip){return new IpInfo(ip);}

}
