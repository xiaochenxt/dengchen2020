package io.github.dengchen2020.jdbc.querydsl.postgres;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimplePath;
import org.jspecify.annotations.NullMarked;

/**
 * Postgis表达式
 * @author xiaochen
 * @since 2025/12/26
 */
@NullMarked
public final class PostgisExpressions {

    private PostgisExpressions() {}

    /**
     * 计算两个经纬度之间的距离，单位为公里
     *
     * @param point     geography类型字段
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    public static NumberExpression<Double> distancekm(SimplePath<?> point, double longitude, double latitude){
        return Expressions.numberTemplate(Double.class, "round((st_distance({0}, st_setsrid(st_point({1}, {2}), 4326))/1000)::numeric,2)", point, longitude, latitude);
    }

    /**
     * 获取两个经纬度之间的距离，单位为公里
     * @param point geography类型字段
     * @param latitude 纬度
     * @param longitude 经度
     * @param epsg 坐标系编号，例如4326
     * @return
     */
    public static NumberExpression<Double> distancekm(SimplePath<?> point, double longitude, double latitude, int epsg){
        return Expressions.numberTemplate(Double.class, "round((st_distance({0}, st_setsrid(st_point({1}, {2}), {3}))/1000)::numeric,2)", point, longitude, latitude, epsg);
    }

    /**
     * 计算两个经纬度之间的距离，单位为米
     *
     * @param point     geography类型字段
     * @param latitude  纬度
     * @param longitude 经度
     * @return
     */
    public static NumberExpression<Double> distancem(SimplePath<?> point, double longitude, double latitude){
        return Expressions.numberTemplate(Double.class, "round(st_distance({0}, st_setsrid(st_point({1}, {2}), 4326))::numeric,2)", point, longitude, latitude);
    }

    /**
     * 获取两个经纬度之间的距离，单位为米
     * @param point geography类型字段
     * @param latitude 纬度
     * @param longitude 经度
     * @param epsg 坐标系编号，例如4326
     * @return
     */
    public static NumberExpression<Double> distancem(SimplePath<?> point, double longitude, double latitude, int epsg){
        return Expressions.numberTemplate(Double.class, "round(st_distance({0}, st_setsrid(st_point({1}, {2}), {3}))::numeric,2)", point, longitude, latitude, epsg);
    }

    /**
     * 获取经纬度附近距离的数据，单位米
     * @param point geography类型字段
     * @param latitude 纬度
     * @param longitude 经度
     * @param maxDistance 最大距离，单位为米
     * @return
     */
    public static BooleanExpression within(SimplePath<?> point, double longitude, double latitude, int maxDistance){
        return Expressions.booleanTemplate("st_dwithin({0},st_setsrid(st_point({1}, {2}), 4326),{3})", point, longitude, latitude, maxDistance).isTrue();
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
    public static BooleanExpression within(SimplePath<?> point, double longitude, double latitude, int maxDistance, int epsg){
        return Expressions.booleanTemplate("st_dwithin({0},st_setsrid(st_point({1}, {2}), {3}),{4})", point, longitude, latitude, epsg, maxDistance).isTrue();
    }

}
