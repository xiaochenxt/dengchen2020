package io.github.dengchen2020.core.utils;

/**
 * 用于计算两人之间的近似距离和方位
 * <p>基于WGS84椭球体。代码来源于谷歌的AOSP项目：<a href="https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-14.0.0_r1/location/java/android/location/Location.java">Location</a></p>
 * @author xiaochen
 * @since 2026/4/3
 */
public abstract class LocationUtils {

    /**
     * 存储一个点的经度和纬度，更便捷易懂的使用该工具类
     * @param longitude 经度
     * @param latitude 纬度
     * @return 存储经度和纬度的Point对象
     */
    public static Point point(double longitude, double latitude) {
        return new Point(longitude, latitude);
    }

    /**
     * 存储一个点的经度和纬度，更便捷易懂的使用该工具类
     * @param longitude 经度
     * @param latitude 纬度
     * @return 存储经度和纬度的Point对象
     */
    public record Point(double longitude, double latitude) {

        /**
         * 计算两点之间的距离和方位
         * @param point 目标点
         * @return {@link BearingDistance}
         */
        public BearingDistance distanceBetween(Point point) {
            return distanceBetween(point.longitude, point.latitude);
        }

        /**
         * 计算两点之间的距离和方位
         * @param longitude 目标点经度
         * @param latitude 目标点纬度
         * @return {@link BearingDistance}
         */
        public BearingDistance distanceBetween(double longitude, double latitude) {
            var results = new float[3];
            LocationUtils.distanceBetween(this.latitude, this.longitude, latitude, longitude, results);
            return new BearingDistance(results[0], results[1], results[2]);
        }

    }

    /**
     * 方位和距离
     * @param distance 距离（单位米）
     * @param initialBearing 初始方位
     * @param finalBearing 最终方位
     */
    public record BearingDistance(float distance, float initialBearing, float finalBearing) {}

    // 缓存数据以提高方位/距离计算的效率
    // 其中 distanceTo 和 bearingTo 依次调用。 假设这种情况通常会发生
    // 为了缓存目的，在同一个线程上。
    private static final ThreadLocal<BearingDistanceCache> sBearingDistanceCache =
            ThreadLocal.withInitial(BearingDistanceCache::new);

    private static void computeDistanceAndBearing(double lat1, double lon1,
                                                  double lat2, double lon2, BearingDistanceCache results) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)
        //将纬度/经度转换为弧度
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;
        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);
        double l = lon2 - lon1;
        double aA = 0.0;
        double u1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double u2 = Math.atan((1.0 - f) * Math.tan(lat2));
        double cosU1 = Math.cos(u1);
        double cosU2 = Math.cos(u2);
        double sinU1 = Math.sin(u1);
        double sinU2 = Math.sin(u2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;
        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha;
        double cos2SM;
        double cosSigma;
        double sinSigma;
        double cosLambda = 0.0;
        double sinLambda = 0.0;
        double lambda = l; // initial guess
        for (int iter = 0; iter < 20; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2;
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha;
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq;
            aA = 1 + (uSquared / 16384.0) * (4096.0 + uSquared * (-768 + uSquared * (320.0
                    - 175.0 * uSquared)));
            double bB = (uSquared / 1024.0) * (256.0 + uSquared * (-128.0 + uSquared * (74.0
                    - 47.0 * uSquared)));
            double cC = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha));
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = bB * sinSigma * (cos2SM + (bB / 4.0) * (cosSigma * (-1.0 + 2.0 * cos2SMSq)
                    - (bB / 6.0) * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0
                    + 4.0 * cos2SMSq)));
            lambda = l + (1.0 - cC) * f * sinAlpha * (sigma + cC * sinSigma * (cos2SM
                    + cC * cosSigma * (-1.0 + 2.0 * cos2SM * cos2SM)));
            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }
        results.mDistance = (float) (b * aA * (sigma - deltaSigma));
        float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
                cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
        initialBearing = (float) (initialBearing * (180.0 / Math.PI));
        results.mInitialBearing = initialBearing;
        float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
                -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
        finalBearing = (float) (finalBearing * (180.0 / Math.PI));
        results.mFinalBearing = finalBearing;
        results.mLat1 = lat1;
        results.mLat2 = lat2;
        results.mLon1 = lon1;
        results.mLon2 = lon2;
    }

    /**
     * 计算两人之间的近似距离（米）位置，以及可选的初始和最终方位
     * 它们之间的最短路径。 距离和方位定义为WGS84椭球体。
     *
     * <p> 计算出的距离存储在results[0]。 如果结果有长度2 或更大时，初始方位会存储在results[1]。
     * 如果结果有长度3或更大，最终方位数据存储在results[2]。
     *
     * @param startLatitude 起始纬度
     * @param startLongitude 起始经度
     * @param endLatitude 终点纬度
     * @param endLongitude 终点经度
     * @param results 一个用于保存结果的float数组
     *
     * @throws IllegalArgumentException if results is null or has length < 1
     */
    public static void distanceBetween(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude,
            float[] results) {
        if (startLatitude < -90.0 || startLatitude > 90.0) {
            throw new IllegalArgumentException("startLatitude out of range: " + startLatitude);
        }
        if (endLatitude < -90.0 || endLatitude > 90.0) {
            throw new IllegalArgumentException("endLatitude out of range: " + endLatitude);
        }
        if (startLongitude < -180.0 || startLongitude > 180.0) {
            throw new IllegalArgumentException("startLongitude out of range: " + startLongitude);
        }
        if (endLongitude < -180.0 || endLongitude > 180.0) {
            throw new IllegalArgumentException("endLongitude out of range: " + endLongitude);
        }
        if (results == null || results.length < 1) throw new IllegalArgumentException("results is null or has length < 1");
        BearingDistanceCache cache = sBearingDistanceCache.get();
        computeDistanceAndBearing(startLatitude, startLongitude,
                endLatitude, endLongitude, cache);
        results[0] = cache.mDistance;
        if (results.length > 1) {
            results[1] = cache.mInitialBearing;
            if (results.length > 2) {
                results[2] = cache.mFinalBearing;
            }
        }
    }

    private static class BearingDistanceCache {
        double mLat1 = 0.0;
        double mLon1 = 0.0;
        double mLat2 = 0.0;
        double mLon2 = 0.0;
        float mDistance = 0.0f;
        float mInitialBearing = 0.0f;
        float mFinalBearing = 0.0f;
    }

}
