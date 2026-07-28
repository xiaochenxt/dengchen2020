package io.github.dengchen2020.jpa.querydsl.vector;

import com.querydsl.core.types.dsl.ArrayExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import org.jspecify.annotations.NullMarked;

/**
 * Vector表达式
 * @author xiaochen
 * @since 2025/12/29
 */
@NullMarked
public final class VectorExpressions {

    private VectorExpressions(){}

    /**
     * sql：{@code a <=> b}，余弦距离
     * @param expr 要匹配的向量
     * @param vector 要匹配的向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> cosineDistance(ArrayExpression<float[], Float> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"cosine_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code a <-> b}，欧式距离
     * @param expr 要匹配的向量
     * @param vector 要匹配的向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> l2Distance(ArrayExpression<float[], Float> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"l2_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code (a <-> b)^2}，平方欧氏距离
     * @param expr 要匹配的向量
     * @param vector 要匹配的向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> l2SquaredDistance(ArrayExpression<float[], Float> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"l2_squared_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code l1_distance(vector, vector)}，曼哈顿距离
     * @param expr 要匹配的向量
     * @param vector 要匹配的向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> l1Distance(ArrayExpression<float[], Float> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"l1_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code (a <#> b) *-1}，内积
     * @param expr 要匹配的向量
     * @param vector 要匹配的向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> innerProduct(ArrayExpression<float[], Float> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"inner_product({0},{1})", expr, vector);
    }

    /**
     * sql：{@code a <#> b}，负内积
     * @param expr 要匹配的向量
     * @param vector 要匹配的向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> negativeInnerProduct(ArrayExpression<float[], Float> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"negative_inner_product({0},{1})", expr, vector);
    }

    /**
     * sql：{@code vector_dims(vector)}, 获取向量的维度数量
     * @param expr 要匹配的向量
     * @return {@link NumberExpression<Integer>}
     */
    public static NumberExpression<Integer> vectorDims(ArrayExpression<float[], Float> expr){
        return Expressions.numberTemplate(Integer.class,"vector_dims({0})", expr);
    }

    /**
     * sql：{@code vector_norm(vector)}，计算向量的欧几里得 L2 模长（L2 范数）
     * @param expr 要匹配的向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> vectorNorm(ArrayExpression<float[], Float> expr){
        return Expressions.numberTemplate(Double.class,"vector_norm({0})", expr);
    }

}
