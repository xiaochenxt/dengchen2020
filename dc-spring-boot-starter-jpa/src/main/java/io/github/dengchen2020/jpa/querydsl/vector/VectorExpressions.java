package io.github.dengchen2020.jpa.querydsl.vector;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.jspecify.annotations.NullMarked;

/**
 * Vector表达式
 * <p>
 * 需要引入hibernate-vector依赖才可使用
 * <pre>
 * {@code
 * <dependency>
 *     <groupId>org.hibernate.orm</groupId>
 *     <artifactId>hibernate-vector</artifactId>
 * </dependency>}
 * </pre>
 * </p>
 * @author xiaochen
 * @since 2025/12/29
 */
@NullMarked
public final class VectorExpressions {

    private VectorExpressions(){}

    /**
     * 将 {@code float[]} 转换为 {@code vector} 类型
     */
    public static SimpleExpression<float[]> vector(float[] vector) {
        return Expressions.simpleTemplate(float[].class, "cast({0} as vector)", (Object) vector);
    }

    /**
     * sql：{@code a <=> b}，余弦距离
     * <p>相似度规则：范围：[0.0 , 2.0]，数值越小，向量越相似</p>
     * @param expr 向量列表达式
     * @param vector 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> cosineDistance(Expression<float[]> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"cosine_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code a <-> b}，欧氏距离
     * <p>相似度规则：范围：[0.0 , +∞)，数值越小，向量越相似</p>
     * @param expr 向量列表达式
     * @param vector 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> l2Distance(Expression<float[]> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"l2_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code (a <-> b)^2}，平方欧氏距离
     * <p>相似度规则：范围：[0.0 , +∞)，数值越小，向量越相似</p>
     * @param expr 向量列表达式
     * @param vector 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> l2SquaredDistance(Expression<float[]> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"l2_squared_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code l1_distance(vector, vector)}，曼哈顿距离
     * <p>相似度规则：范围：[0.0 , +∞)，数值越小，向量越相似</p>
     * @param expr 向量列表达式
     * @param vector 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> l1Distance(Expression<float[]> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"l1_distance({0},{1})", expr, vector);
    }

    /**
     * sql：{@code (a <#> b) *-1}，内积（所有向量必须预先L2归一化，否则相似度结果失真）
     * <p>相似度规则：范围：[-1.0 , 1.0]，数值越大，向量越相似</p>
     * @param expr 向量列表达式
     * @param vector 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> innerProduct(Expression<float[]> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"inner_product({0},{1})", expr, vector);
    }

    /**
     * sql：{@code a <#> b}，负内积（所有向量必须预先L2归一化，否则相似度结果失真）
     * <p>相似度规则：范围：[-1.0 , 1.0]，数值越小，向量越相似</p>
     * @param expr 向量列表达式
     * @param vector 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> negativeInnerProduct(Expression<float[]> expr, float[] vector){
        return Expressions.numberTemplate(Double.class,"negative_inner_product({0},{1})", expr, vector);
    }

    /**
     * sql：{@code binary_vector <~> binary_vector)}，汉明距离（仅用于二进制向量）
     * <p>相似度规则：范围：[0.0 , +∞)，数值越小，向量越相似</p>
     * @param expr 向量列表达式
     * @param b 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> hammingDistance(NumberExpression<Byte> expr, NumberExpression<Byte> b){
        return Expressions.numberTemplate(Double.class,"hamming_distance({0},{1})", expr, b);
    }

    /**
     * sql：{@code binary_vector <%> binary_vector}，杰卡德距离（仅用于二进制向量）
     * <p>相似度规则：范围：[0.0 , 1.0]，数值越小，向量越相似</p>
     * @param expr 向量列表达式
     * @param b 查询向量
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> jaccardDistance(NumberExpression<Byte> expr, NumberExpression<Byte> b){
        return Expressions.numberTemplate(Double.class,"jaccard_distance({0},{1})", expr, b);
    }

    /**
     * sql：{@code vector_dims(vector)}, 获取向量的维度数量（仅用于校验、数据巡检）
     * <p>范围：[1 , +∞)</p>
     * @param expr 向量列表达式
     * @return {@link NumberExpression<Integer>}
     */
    public static NumberExpression<Integer> vectorDims(Expression<float[]> expr){
        return Expressions.numberTemplate(Integer.class,"vector_dims({0})", expr);
    }

    /**
     * sql：{@code vector_norm(vector)}，计算向量的欧几里得 L2 模长（L2 范数）
     * <p>用途：向量归一化前置计算、校验单位向量、识别零向量</p>
     * <p>范围：[0.0 , +∞)，0 表示零向量</p>
     * @param expr 向量列表达式
     * @return {@link NumberExpression<Double>}
     */
    public static NumberExpression<Double> vectorNorm(Expression<float[]> expr){
        return Expressions.numberTemplate(Double.class,"vector_norm({0})", expr);
    }

    /**
     * sql：{@code binary_quantize(vector)}，二进制量化，将浮点 vector 按维度正负转为 bit 向量；维度 > 0 置 1，≤0 置 0。用于构建二进制粗召回索引，搭配汉明距离<~>检索，一般需要原始向量二次精排弥补精度损失
     * @param expr 向量列表达式
     * @return {@link NumberExpression<Byte>}
     */
    public static NumberExpression<Byte> binaryQuantize(Expression<float[]> expr){
        return Expressions.numberTemplate(Byte.class,"binary_quantize({0})", expr);
    }

    /**
     * sql：{@code binary_quantize(vector)}，二进制量化，将浮点 vector 按维度正负转为 bit 向量；维度 > 0 置 1，≤0 置 0。用于构建二进制粗召回索引，搭配汉明距离<~>检索，一般需要原始向量二次精排弥补精度损失
     * @param vector 向量列表达式
     * @return {@link NumberExpression<Byte>}
     */
    public static NumberExpression<Byte> binaryQuantize(float[] vector){
        return Expressions.numberTemplate(Byte.class,"binary_quantize({0})", vector(vector));
    }

    /**
     * sql：{@code subvector(vector, offset, length)}，截取向量连续维度，start 从 1 开始；多用于高维向量两段式检索：低维子向量粗召回，原始完整向量精排。仅适合大规模向量库性能优化，常规 RAG 无需使用
     *
     * @param expr   向量列表达式
     * @param offset 起始偏移量，从 1 开始计数
     * @param length 长度
     * @return {@link SimpleExpression}
     */
    public static SimpleExpression<float[]> subvector(Expression<float[]> expr, int offset, int length){
        return Expressions.template(float[].class,"subvector({0},{1},{2})", expr, offset, length);
    }

    /**
     * sql：{@code l2_normalize(vector)}，L2 归一化（欧几里得归一化）；向量除以自身 L2 模长 (vector_norm) 生成单位向量。归一化后内积等价余弦相似度，适配负内积<#>检索。注意：禁止查询时实时归一化，会导致向量索引失效；零向量需要防止除零异常
     * @param expr 向量列表达式
     * @return {@link SimpleExpression}
     */
    public static SimpleExpression<float[]> l2Normalize(Expression<float[]> expr){
        return Expressions.template(float[].class,"l2_normalize({0})", expr);
    }

}
