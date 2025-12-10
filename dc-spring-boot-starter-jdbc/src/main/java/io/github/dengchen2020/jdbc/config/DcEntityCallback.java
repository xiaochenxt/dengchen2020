package io.github.dengchen2020.jdbc.config;

import io.github.dengchen2020.core.jdbc.BeforeInsertCallback;
import io.github.dengchen2020.core.jdbc.BeforeUpdateCallback;
import org.jspecify.annotations.NonNull;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

/**
 * 新增数据或更新数据之前回调
 * @author xiaochen
 * @since 2025/12/9
 */
class DcEntityCallback<T> implements BeforeConvertCallback<T> {

    private final RelationalMappingContext context;

    public DcEntityCallback(@NonNull RelationalMappingContext context) {
        this.context = context;
    }

    @NonNull
    @Override
    public T onBeforeConvert(T aggregate) {
        if (context.getRequiredPersistentEntity(aggregate.getClass()).isNew(aggregate)) {
            if (aggregate instanceof BeforeInsertCallback beforeInsertCallback) {
                beforeInsertCallback.beforeInsert();
            }
        } else {
            if (aggregate instanceof BeforeUpdateCallback beforeUpdateCallback) {
                beforeUpdateCallback.beforeUpdate();
            }
        }
        return aggregate;
    }

}
