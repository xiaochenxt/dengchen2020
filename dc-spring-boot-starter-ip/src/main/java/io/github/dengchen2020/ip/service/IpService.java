package io.github.dengchen2020.ip.service;

import io.github.dengchen2020.ip.model.IpInfo;
import org.jspecify.annotations.NullMarked;

/**
 * ip地址查询
 * @author xiaochen
 * @since 2023/5/5
 */
@NullMarked
public interface IpService {

    /**
     * 获取ip信息
     */
    IpInfo getInfo(String ip);

    /**
     * 获取ipv4信息
     */
    IpInfo getInfoForIpv4(String ip);

    /**
     * 获取ipv6信息
     */
    IpInfo getInfoForIpv6(String ip);

}
