package io.github.dengchen2020.core.condition;

import io.github.dengchen2020.core.utils.VersionUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * SpringBoot版本匹配
 * @author xiaochen
 * @since 2026/6/22
 */
final class OnSpringBootVersionCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnSpringBootVersion.class.getName());
        String equalOrNewer = (String) attributes.get("equalOrNewer");
        String olderThan = (String) attributes.get("olderThan");
        boolean hasLower = !equalOrNewer.isBlank();
        boolean hasUpper = !olderThan.isBlank();
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnSpringBootVersion.class);
        if (!hasLower && !hasUpper) throw new IllegalArgumentException("@ConditionalOnSpringBootVersion 必须配置 equalOrNewer 或 olderThan 至少其中一个");
        boolean match = true;
        StringBuilder descBuilder = new StringBuilder("版本规则：");
        String currentVersion = SpringBootVersion.getVersion();
        if (hasLower) {
            if (VersionUtils.compareVersion(currentVersion, equalOrNewer) < 0) match = false;
            descBuilder.append(String.format("%s >= %s ", currentVersion, equalOrNewer));
        }
        if (hasUpper) {
            if (VersionUtils.compareVersion(currentVersion, olderThan) >= 0) match = false;
            if (hasLower) descBuilder.append("且 ");
            descBuilder.append(String.format("%s < %s", currentVersion, olderThan));
        }
        String reason = descBuilder.toString();
        return match ? ConditionOutcome.match(message.because(reason)) : ConditionOutcome.noMatch(message.because(reason));
    }

}
