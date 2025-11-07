package io.github.dengchen2020.ip.service.impl.dat;

import io.github.dengchen2020.ip.model.IpInfo;
import io.github.dengchen2020.ip.service.IpService;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import static io.github.dengchen2020.core.utils.StrUtils.getValue;

/**
 * dat查询实现，不支持ipv6查询
 *
 * @author xiaochen
 * @since 2023/5/5
 */
@NullMarked
public class IpDatServiceImpl implements IpService {

    private static final Logger log = LoggerFactory.getLogger(IpDatServiceImpl.class);

    private IPLocation ipLocation;

    public IpDatServiceImpl(String ipv4DataPath) {
        loadData(ipv4DataPath);
    }

    public synchronized void loadData(String ipv4DataPath) {
        File ipv4File = new File(ipv4DataPath);
        if (ipv4File.isFile() && ipv4File.exists()) {
            try {
                ipLocation = new IPLocation(Files.readAllBytes(ipv4File.toPath()));
                log.info("ipv4数据包加载完成，占用{}MB", DataSize.ofBytes(ipv4File.length()).toMegabytes());
            } catch (Exception e) {
                throw new IllegalArgumentException("ipv4数据包加载失败", e);
            }
            return;
        }
        try (InputStream inputStream = getClass().getResourceAsStream("/ip.dat")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("未找到ipv4数据包，请将ipv4数据包ip.dat放置在resources目录下或" + ipv4File.getAbsolutePath());
            }
            byte[] data = inputStream.readAllBytes();
            ipLocation = new IPLocation(data);
            log.info("ipv4数据包加载完成，占用{}MB", DataSize.ofBytes(data.length).toMegabytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("ipv4数据包加载失败", e);
        }
    }

    @Override
    public IpInfo getInfo(String ip) {
        if (StringUtils.hasText(ip)) {
            Location location = ipLocation.fetchIPLocation(ip);
            if (location == null) return new IpInfo(ip);
            String[] ipInfo1 = location.country.split("\\|");
            String[] ipInfo2 = location.area.split("\\|");
            return new IpInfo(ip, getValue(ipInfo1, 0), getValue(ipInfo1, 1),
                    getValue(ipInfo2, 0), getValue(ipInfo2, 1), getValue(ipInfo2, 2),
                    getValue(ipInfo2, 3), getValue(ipInfo2, 4), getValue(ipInfo2, 5),
                    getValue(ipInfo1, 2), getValue(ipInfo1, 3), getValue(ipInfo1, 4),
                    getValue(ipInfo1, 5), getValue(ipInfo2, 6), getValue(ipInfo2, 7))
                    ;
        }
        return new IpInfo(ip);
    }

    @Override
    public IpInfo getInfoForIpv4(String ip) {
        return getInfo(ip);
    }

    @Override
    public IpInfo getInfoForIpv6(String ip) {
        throw new UnsupportedOperationException("不支持ipv6查询");
    }
}
