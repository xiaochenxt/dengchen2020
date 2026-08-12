package io.github.dengchen2020.jpa.querydsl.mysql;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimplePath;
import org.geolatte.geom.Point;
import org.jspecify.annotations.NullMarked;

/**
 * 扩充querydsl的专用于mysql的sql表达式
 * @author xiaochen
 * @since 2024/4/2
 */
@NullMarked
public final class MysqlSpatialExpressions {

    private MysqlSpatialExpressions() {}

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
