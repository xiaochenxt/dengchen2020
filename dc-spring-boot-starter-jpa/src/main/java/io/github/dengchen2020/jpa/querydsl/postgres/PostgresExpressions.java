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
     * sql：{@code a % b}，筛选文本字符串整体相似度高的数据（默认阈值为0.3），主要用于where语句
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link BooleanExpression}
     */
    public static BooleanExpression bsimilarity(StringExpression expr, String str){
        return Expressions.booleanTemplate("bsimilarity({0},{1})", expr, str);
    }

    /**
     * sql：{@code similarity(a,b)}，计算两个文本字符串的整体相似度，返回一个 0 到 1 之间的浮点数
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> similarity(StringExpression expr, String str){
        return Expressions.numberTemplate(Double.class, "similarity({0},{1})", expr, str);
    }

    /**
     * sql：{@code a %> b}，筛选连续片段相似度高的数据（默认阈值为0.6）
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link BooleanExpression}
     */
    public static BooleanExpression rightWordSimilarity(StringExpression expr, String str){
        return Expressions.booleanTemplate("right_word_similarity({0},{1})", expr, str);
    }

    /**
     * sql：{@code word_similarity(a,b)}，计算两个文本字符串的连续片段相似度
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> wordSimilarity(StringExpression expr, String str){
        return Expressions.numberTemplate(Double.class,"word_similarity({0},{1})", expr, str);
    }

    /**
     * sql：{@code a %>> b}，筛选更接近独立单词的连续片段相似度高的数据（默认阈值为0.5）
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link BooleanExpression}
     */
    public static BooleanExpression rightStrictWordSimilarity(StringExpression expr, String str){
        return Expressions.booleanTemplate("right_strict_word_similarity({0},{1})", expr, str);
    }

    /**
     * sql：{@code strict_word_similarity(a,b)}，计算两个文本字符串的更接近独立单词的连续片段相似度
     * @param expr 要匹配的文本字符串
     * @param str 要匹配的文本字符串
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> strictWordSimilarity(StringExpression expr, String str){
        return Expressions.numberTemplate(Double.class,"strict_word_similarity({0},{1})", expr, str);
    }

}
