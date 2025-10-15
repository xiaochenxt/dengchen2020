package io.github.dengchen2020.core.rabbit;

import com.fasterxml.jackson.databind.JavaType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * 增强{@link org.springframework.amqp.support.converter.Jackson2JsonMessageConverter}，兼容byes[]和String和Object之间的互相转换
 *
 * @author xiaochen
 * @since 2024/8/21
 */
public class Jackson2JsonMessageConverter extends org.springframework.amqp.support.converter.Jackson2JsonMessageConverter {

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            try {
                JavaType targetJavaType = getJavaTypeMapper()
                        .toJavaType(properties);
                if (targetJavaType.isTypeOrSubTypeOf(byte[].class)) {
                    return message.getBody();
                } else if (targetJavaType.isTypeOrSubTypeOf(String.class)) {
                    return new String(message.getBody(), properties.getContentEncoding() != null ? properties.getContentEncoding() : getDefaultCharset());
                }
            } catch (Exception ignored) {

            }
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        }
        return super.fromMessage(message);

    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties, Type genericType) throws MessageConversionException {
        try {
            byte[] body = null;
            if (object instanceof byte[] bytes) {
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
                body = bytes;
            } else if (object instanceof String str) {
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
                messageProperties.setContentEncoding(getDefaultCharset());
                body = str.getBytes(getDefaultCharset());
            }
            if (body != null) {
                messageProperties.setContentLength(body.length);
                if (getClassMapper() == null) {
                    JavaType type = this.objectMapper.constructType(
                            genericType == null ? object.getClass() : genericType);
                    if (genericType != null && !type.isContainerType()
                            && Modifier.isAbstract(type.getRawClass().getModifiers())) {
                        type = this.objectMapper.constructType(object.getClass());
                    }
                    getJavaTypeMapper().fromJavaType(type, messageProperties);
                }
                else {
                    getClassMapper().fromClass(object.getClass(), messageProperties);
                }
                return new Message(body, messageProperties);
            }
        } catch (Exception ignored) {

        }
        return super.createMessage(object, messageProperties, genericType);
    }

}
