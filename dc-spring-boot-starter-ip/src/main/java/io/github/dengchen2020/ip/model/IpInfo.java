package io.github.dengchen2020.ip.model;

/**
 * ip信息
 *
 * @param ip                    ip
 * @param continent             所属的七大洲
 * @param country               国家
 * @param province              省份
 * @param city                  城市
 * @param district              区县
 * @param isp                   互联网供应商
 * @param longitude             经度
 * @param latitude              纬度
 * @param areaCode              行政区码
 * @param cityCode              电话和区号
 * @param zipCode               邮政编码
 * @param timeZone              时区
 * @param currency              国家英文缩写
 * @param elevation             海拔
 * @param weatherStation        气象站
 * @param alpha2Code            国家简称
 * @author xiaochen
 * @since 2026/6/22
 */
public record IpInfo(String ip, String continent, String country, String province, String city, String district,
                     String isp, String longitude, String latitude, String areaCode, String cityCode, String zipCode, String timeZone,
                     String currency, String elevation, String weatherStation, String alpha2Code) {

    public IpInfo(String ip) {
        this(ip, null, null, null, null);
    }

    public IpInfo(String ip, String country, String province, String city,
                  String isp) {
        this(ip, country, province, city, null, isp);
    }

    public IpInfo(String ip, String country, String province, String city, String district,
                  String isp) {
        this(ip, null, country, province, city, district, isp, null, null, null,
                null, null, null, null, null, null, null);
    }

}
