package io.github.dengchen2020.core.validation;

import jakarta.validation.groups.Default;

/**
 * 更新校验分组
 *
 * <p>用于承载「仅在更新场景生效」的字段校验规则。同一字段在新增与更新场景下的限制可能不同，
 * 这类规则若放进 {@link Default} 中，会连带在新增时触发，因此需要
 * 单独成组。</p>
 *
 * <p>该分组继承 {@link Default}，因此按 {@code Update} 分组校验时，
 * 属于 {@code Default} 分组的约束（即未声明 {@code groups}、或显式声明为 {@code Default.class} 的）
 * 也会一并生效。换言之，通用约束不必重复标注 {@code groups}，只有更新专属的约束才需要标注
 * {@code groups = Update.class}。</p>
 *
 * <p>使用示例——在实体字段上声明约束：</p>
 * <pre>{@code
 * @Data
 * public class User {
 *
 *     // 更新时必须指定要修改的记录
 *     @NotNull(groups = Update.class)
 *     private Long id;
 *
 *     // 未声明 groups，属于 Default 分组
 *     @Size(min = 2, max = 20)
 *     private String name;
 *
 *     // 未声明 groups，属于 Default 分组
 *     @Email
 *     private String email;
 * }
 * }</pre>
 *
 * <p>使用示例——在接口上按分组触发校验：</p>
 * <pre>{@code
 * @PutMapping
 * public void update(@Validated(Update.class) @RequestBody User user) {
 *     // 此处触发 Update 分组及其父组 Default 的校验
 * }
 * }</pre>
 *
 * <p>注意：{@code @Valid} 与未指定分组的 {@code @Validated} 都只校验 {@code Default} 分组，
 * 此时标注了 {@code groups = Update.class} 的约束不会生效，需显式写成
 * {@code @Validated(Update.class)}。</p>
 *
 * @author xiaochen
 * @since 2026/10/31
 */
public interface Update extends Default {

}
