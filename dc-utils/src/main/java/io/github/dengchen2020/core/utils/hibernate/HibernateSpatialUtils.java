package io.github.dengchen2020.core.utils.hibernate;

import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

/**
 * Hibernate工具类
 * @author xiaochen
 * @since 2025/12/26
 */
public abstract class HibernateSpatialUtils {

    /**
     * 创建{@link Point}
     * @param longitude 经度
     * @param latitude 纬度
     * @return
     */
    public static Point<?> point(Double longitude, Double latitude){
        return DSL.point(CoordinateReferenceSystems.WGS84, DSL.g(longitude, latitude));
    }

    /**
     * 创建{@link Point}
     * @param longitude 经度
     * @param latitude 纬度
     * @param epsg 坐标系编号，例如4326
     * @return
     */
    public static Point<?> point(Double longitude, Double latitude, int epsg){
        return DSL.point(CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(epsg), DSL.g(longitude, latitude));
    }

}
