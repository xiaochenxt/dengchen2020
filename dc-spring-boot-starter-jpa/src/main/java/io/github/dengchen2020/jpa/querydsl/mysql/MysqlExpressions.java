package io.github.dengchen2020.jpa.querydsl.mysql;

import com.querydsl.core.types.dsl.*;
import io.github.dengchen2020.jpa.querydsl.JpaExpressions;
import org.geolatte.geom.Point;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

/**
 * 扩充querydsl的专用于mysql的sql表达式
 * @author xiaochen
 * @since 2024/4/2
 */
@NullMarked
public final class MysqlExpressions {

    private MysqlExpressions() {}

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param expr
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringExpression expr, String value) {
        return Expressions.numberTemplate(Integer.class, "find_in_set({0}, {1})", expr, value).gt(0);
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param expr
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringExpression expr, String... value) {
        return findInSet(expr, String.join(",", value));
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param expr
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringExpression expr, Collection<String> value) {
        return findInSet(expr, String.join(",", value));
    }

    /**
     * 提取json字段中的值，示例：{@code json_column ->> '$.name'}
     *
     * @param expr json字段
     * @param path 路径
     */
    public static StringExpression jsonExtract(StringExpression expr, String path) {
        return JpaExpressions.jsonValue(expr, path);
    }

    /**
     * 计算两个经纬度之间的距离，单位为公里
     *
     * @param point     geography类型字段
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    public static NumberExpression<Double> distancekm(SimplePath<Point<?>> point, double longitude, double latitude){
        return Expressions.numberTemplate(Double.class, "round(st_distance_sphere({0}, st_setsrid(point({1}, {2}), 4326))/1000,2)", point, longitude, latitude);
    }

    /**
     * 计算两个经纬度之间的距离，单位为公里
     *
     * @param point     geography类型字段
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    public static NumberExpression<Double> distancekm(SimplePath<Point<?>> point, double longitude, double latitude, int epsg){
        return Expressions.numberTemplate(Double.class, "round(st_distance_sphere({0}, st_setsrid(point({1}, {2}), {3}))/1000,2)", point, longitude, latitude, epsg);
    }

    /**
     * 计算两个经纬度之间的距离，单位为米
     *
     * @param point     geography类型字段
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    public static NumberExpression<Double> distancem(SimplePath<Point<?>> point, double longitude, double latitude){
        return Expressions.numberTemplate(Double.class, "round(st_distance_sphere({0}, st_setsrid(point({1}, {2}), 4326)),2)", point, longitude, latitude);
    }

    /**
     * 计算两个经纬度之间的距离，单位为米
     *
     * @param point     geography类型字段
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    public static NumberExpression<Double> distancem(SimplePath<Point<?>> point, double longitude, double latitude, int epsg){
        return Expressions.numberTemplate(Double.class, "round(st_distance_sphere({0}, st_setsrid(point({1}, {2}), {3})),2)", point, longitude, latitude, epsg);
    }

    /**
     * 获取经纬度附近距离的数据，单位米
     * @param point geography类型字段
     * @param latitude 纬度
     * @param longitude 经度
     * @param maxDistance 最大距离，单位为米
     * @return
     */
    public static BooleanExpression within(SimplePath<Point<?>> point, double longitude, double latitude, int maxDistance){
        return Expressions.booleanTemplate("st_within({0}, st_buffer(st_setsrid(st_point({1},{2}),4326), {3}))", point, longitude, latitude, maxDistance);
    }

    /**
     * 获取经纬度附近距离的数据，单位米
     * @param point geography类型字段
     * @param latitude 纬度
     * @param longitude 经度
     * @param epsg 坐标系编号，例如4326
     * @param maxDistance 最大距离，单位为米
     * @return
     */
    public static BooleanExpression within(SimplePath<Point<?>> point, double longitude, double latitude, int maxDistance, int epsg){
        return Expressions.booleanTemplate("st_within({0}, st_buffer(st_setsrid(st_point({1},{2}),{3}), {4}))", point, longitude, latitude, epsg, maxDistance);
    }

}
