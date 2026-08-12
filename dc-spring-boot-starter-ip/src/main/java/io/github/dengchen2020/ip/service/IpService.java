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
     * @注意 各个厂商数据格式有差异，可能需要自行转换
     */
    IpInfo getInfo(String ip);

}
