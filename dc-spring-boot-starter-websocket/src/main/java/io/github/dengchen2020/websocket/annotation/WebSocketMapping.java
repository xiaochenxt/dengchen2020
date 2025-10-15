package io.github.dengchen2020.websocket.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * WebSocket端口映射
 * @author xiaochen
 * @since 2024/8/3
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface WebSocketMapping {

    /**
     * websocket端口映射路径
     */
    String[] value();

}
