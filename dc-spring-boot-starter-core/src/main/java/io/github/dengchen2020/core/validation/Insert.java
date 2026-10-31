package io.github.dengchen2020.core.validation;

import jakarta.validation.groups.Default;

/**
 * 新增校验分组
 *
 * <p>用于承载「仅在新增场景生效」的字段校验规则。同一实体在不同场景下往往需要不同的约束，
 * 例如主键在新增时由数据库生成、不应由前端传入，需要一条「必须为空」的规则，而这条规则
 * 在其它场景并不适用，因此必须从 {@link jakarta.validation.groups.Default} 中分离出来。</p>
 *
 * <p>该分组继承 {@link jakarta.validation.groups.Default}，因此按 {@code Insert} 分组校验时，
 * 属于 {@code Default} 分组的约束（即未声明 {@code groups}、或显式声明为 {@code Default.class} 的）
 * 也会一并生效。换言之，通用约束不必重复标注 {@code groups}，只有新增专属的约束才需要标注
 * {@code groups = Insert.class}。</p>
 *
 * <p>使用示例——在实体字段上声明约束：</p>
 * <pre>{@code
 * @Data
 * public class User {
 *
 *     // 新增时不允许传入主键，交给数据库生成
 *     @Null(groups = Insert.class)
 *     private Long id;
 *
 *     // 新增时必填
 *     @NotBlank(groups = Insert.class)
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
 * @PostMapping
 * public void insert(@Validated(Insert.class) @RequestBody User user) {
 *     // 此处触发 Insert 分组及其父组 Default 的校验
 * }
 * }</pre>
 *
 * <p>注意：{@code @Valid} 与未指定分组的 {@code @Validated} 都只校验 {@code Default} 分组，
 * 此时标注了 {@code groups = Insert.class} 的约束不会生效，需显式写成
 * {@code @Validated(Insert.class)}。</p>
 *
 * @author xiaochen
 * @since 2026/10/31
 */
public interface Insert extends Default {

}
