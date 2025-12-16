package io.github.dengchen2020.ip.service.impl.xdb;

import io.github.dengchen2020.ip.model.IpInfo;
import io.github.dengchen2020.ip.service.IpService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.InvalidConfigException;
import org.lionsoul.ip2region.service.Ip2Region;
import org.lionsoul.ip2region.xdb.Searcher;
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

    private Ip2Region ip2Region;

    public IpXdbV2ServiceImpl(String ipv4DataPath, @Nullable String ipv6DataPath, boolean verify) throws IOException, XdbException, InterruptedException, InvalidConfigException {
        loadDataWithBufferCache(ipv4DataPath, ipv6DataPath, verify);
    }

    public IpXdbV2ServiceImpl(String ipv4DataPath, @Nullable String ipv6DataPath, int v4Searchers, int v6Searchers, boolean verify) throws IOException, XdbException, InterruptedException, InvalidConfigException {
        loadDataWithVIndexCache(ipv4DataPath, ipv6DataPath, v4Searchers, v6Searchers, verify);
    }

    public synchronized void loadDataWithBufferCache(String ipv4DataPath, @Nullable String ipv6DataPath, boolean verify) throws XdbException, IOException, InterruptedException, InvalidConfigException {
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
        load(Config.custom().setXdbFile(ipv4File).setCachePolicy(Config.BufferCache).asV4(), ipv6Config);
    }

    public synchronized void loadDataWithVIndexCache(String ipv4DataPath,@Nullable String ipv6DataPath, int v4Searchers, int v6Searchers, boolean verify) throws XdbException, IOException, InterruptedException, InvalidConfigException {
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
        load(Config.custom().setXdbFile(ipv4File).setSearchers(v4Searchers).asV4(), ipv6Config);
    }

    private void load(Config v4,@Nullable Config v6) throws IOException, InterruptedException {
        Ip2Region ip2Region = Ip2Region.create(v4, v6);
        if (this.ip2Region != null) {
            this.ip2Region.close();
            this.ip2Region = ip2Region;
        } else {
            this.ip2Region = ip2Region;
        }
    }

    @Override
    public IpInfo getInfo(String ip) {
        if (!StringUtils.hasText(ip)) return defaultInfo(ip);
        try {
            String[] ipInfo = ip2Region.search(ip).split("\\|");
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
    public void destroy() throws InterruptedException {
        ip2Region.close();
    }

    /**
     * 查询不到时的默认返回信息
     */
    private IpInfo defaultInfo(String ip){return new IpInfo(ip);}

}
