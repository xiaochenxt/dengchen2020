package io.github.dengchen2020.jpa.querydsl.postgres;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.jspecify.annotations.NullMarked;

/**
 * 扩充querydsl的专用于postgres的sql表达式
 * @author xiaochen
 * @since 2026/7/23
 */
@NullMarked
public final class PostgresExpressions {

    private PostgresExpressions(){}

    /**
     * 筛选相似的数据（默认阈值为0.3），主要用于where语句
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link BooleanExpression}
     */
    public static BooleanExpression bsimilarity(StringExpression expr, String str){
        return Expressions.booleanTemplate("bsimilarity({0},{1})", expr, str);
    }

    /**
     * 计算两个文本字符串的相似度，返回一个 0 到 1 之间的浮点数
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> similarity(StringExpression expr, String str){
        return Expressions.numberTemplate(Double.class, "similarity({0},{1})", expr, str);
    }

}
