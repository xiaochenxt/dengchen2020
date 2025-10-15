package io.github.dengchen2020.ip.model;

/**
 * ip信息
 *
 * @param ip                    ip
 * @param continent             洲
 * @param country               国家
 * @param province              省份
 * @param city                  城市
 * @param district              地区
 * @param isp                   互联网服务提供商
 * @param zoningCode1           行政区划代码1
 * @param zoningCode2           行政区划代码2
 * @param zoningCode3           行政区划代码3
 * @param nationalEnglish       国家英文
 * @param countryAbbreviations  国家英文缩写
 * @param internationalAreaCode 国际区号
 * @param longitude             经度
 * @param latitude              纬度
 * @author xiaochen
 * @since 2023/5/6
 */
public record IpInfo(String ip, String continent, String country, String province, String city, String district,
                     String isp, String zoningCode1, String zoningCode2, String zoningCode3, String nationalEnglish,
                     String countryAbbreviations, String internationalAreaCode, String longitude, String latitude) {

    public IpInfo(String ip) {
        this(ip, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
    }

}
