package io.github.dengchen2020.core.utils;

/**
 * 版本工具类
 * @author xiaochen
 * @since 2026/6/22
 */
public final class VersionUtils {

    private VersionUtils() {}

    /**
     * 比较两个版本号
     * @param v1 版本1
     * @param v2 版本2
     * @return 如果 v1 大于 v2 返回 1，等于返回 0，小于返回 -1
     */
    public static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) return 0;
        String[] arr1 = v1.split("\\.");
        String[] arr2 = v2.split("\\.");
        int maxLen = Math.max(arr1.length, arr2.length);
        for (int i = 0; i < maxLen; i++) {
            int num1 = i < arr1.length ? Integer.parseInt(arr1[i]) : 0;
            int num2 = i < arr2.length ? Integer.parseInt(arr2[i]) : 0;
            if (num1 != num2) return Integer.compare(num1, num2);
        }
        return 0;
    }

}
