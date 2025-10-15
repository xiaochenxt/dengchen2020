package io.github.dengchen2020.core.utils;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * sql元数据解析工具类
 * @author xiaochen
 * @since 2024/6/22
 */
public abstract class SqlMetaUtils {

    private static final Logger log = LoggerFactory.getLogger(SqlMetaUtils.class);

    private static final ConcurrentMap<String, TableInfo> tableInfoCache = new ConcurrentReferenceHashMap<>();

    public record TableInfo(String tableName, String schema, List<String> columns, String idColumn, Method getIdMethod,
                            Method getVersionMethod, Method prePersist, Method postPersist, Method preUpdate,
                            Method postUpdate, Method preRemove, Method postRemove, Method postLoad,
                            String allColumnFragment) {

    }

    /**
     * 添加父类方法
     */
    public static void addSuperMethods(Class<?> entityClass, List<Method> methods) {
        if (entityClass.getSuperclass() != null) addSuperMethods(entityClass.getSuperclass(), methods);
        if (entityClass.getAnnotation(MappedSuperclass.class) != null) {
            methods.addAll(new ArrayList<>(Arrays.asList(ReflectionUtils.getDeclaredMethods(entityClass))));
        }
    }

    /**
     * 添加父类字段
     */
    public static void addSuperFields(Class<?> entityClass, List<Field> fields) {
        if (entityClass.getSuperclass() != null) addSuperFields(entityClass.getSuperclass(), fields);
        if (entityClass.getAnnotation(MappedSuperclass.class) != null) {
            fields.addAll(new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields())));
        }
    }

    /**
     * 解析@Column指定的字段名
     *
     * @return 字段名
     */
    private static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column == null || !StringUtils.hasText(column.name())) {
            return convertToSnakeCase(field.getName());
        }
        return column.name();
    }

    /**
     * 转化成蛇形下划线命名格式
     *
     * @param name 原字段名
     * @return 新字段名
     */
    public static String convertToSnakeCase(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0,len = name.length(); i < len; i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append("_");
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    public static <T> TableInfo init(Class<T> entityClass) {
        String entityName = entityClass.getName();
        return tableInfoCache.computeIfAbsent(entityName, key -> {
            Table table = entityClass.getAnnotation(Table.class);
            String tableName;
            String schema = null;
            if (table == null) {
                tableName = convertToSnakeCase(key);
            } else {
                tableName = table.name();
                schema = table.schema();
            }
            List<String> columns = new ArrayList<>();
            String idColumn = null;
            List<Method> methods = new ArrayList<>();
            addSuperMethods(entityClass, methods);
            methods.addAll(new ArrayList<>(Arrays.asList(ReflectionUtils.getDeclaredMethods(entityClass))));
            List<Field> fields = new ArrayList<>();
            addSuperFields(entityClass, fields);
            fields.addAll(new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields())));
            String idFieldName = null;
            String versionFieldName = null;
            for (Field field : fields) {
                if(!Modifier.isStatic(field.getModifiers()) && field.getAnnotation(Transient.class) == null){
                    columns.add(getColumnName(field));
                }
                if (idFieldName == null && field.getAnnotation(Id.class) != null) {
                    idFieldName = field.getName();
                    Column column = field.getAnnotation(Column.class);
                    if(column != null && StringUtils.hasText(column.name())){
                        idColumn = column.name();
                    }else {
                        idColumn = convertToSnakeCase(idFieldName);
                    }
                }
                if (versionFieldName == null && field.getAnnotation(Version.class) != null) {
                    versionFieldName = field.getName();
                }
            }
            String allColumnFragment = String.join(",", columns);
            String getIdMethodName = getMethodName(idFieldName);
            String getVersionMethodName = getMethodName(versionFieldName);
            boolean getId = true;
            boolean getVersion = true;
            Method persist = null;
            Method postPersist = null;
            Method preUpdate = null;
            Method postUpdate = null;
            Method preRemove = null;
            Method postRemove = null;
            Method postLoad = null;
            Method getIdMethod = null;
            Method getVersionMethod = null;
            for (Method method : methods) {
                if (persist == null && method.getAnnotation(PrePersist.class) != null) {
                    ReflectionUtils.makeAccessible(method);
                    persist = method;
                }
                if (postPersist == null && method.getAnnotation(PostPersist.class) != null) {
                    ReflectionUtils.makeAccessible(method);
                    postPersist = method;
                }
                if (preUpdate == null && method.getAnnotation(PreUpdate.class) != null) {
                    ReflectionUtils.makeAccessible(method);
                    preUpdate = method;
                }
                if (postUpdate == null && method.getAnnotation(PostUpdate.class) != null) {
                    ReflectionUtils.makeAccessible(method);
                    postUpdate = method;
                }
                if (preRemove == null && method.getAnnotation(PreRemove.class) != null) {
                    ReflectionUtils.makeAccessible(method);
                    preRemove = method;
                }
                if (postRemove == null && method.getAnnotation(PostRemove.class) != null) {
                    ReflectionUtils.makeAccessible(method);
                    postRemove = method;
                }
                if (postLoad == null && method.getAnnotation(PostLoad.class) != null) {
                    ReflectionUtils.makeAccessible(method);
                    postLoad = method;
                }
                if (getId && getIdMethodName != null) {
                    if (method.getName().equals(getIdMethodName) && method.getParameterCount() == 0 && method.getReturnType() != Void.class) {
                        ReflectionUtils.makeAccessible(method);
                        getId = false;
                        getIdMethod = method;
                    }
                    if (getId) {
                        if (method.getName().equals(idFieldName) && method.getParameterCount() == 0 && method.getReturnType() != Void.class) {
                            ReflectionUtils.makeAccessible(method);
                            getId = false;
                            getIdMethod = method;
                        }
                    }
                }
                if (getVersion && getVersionMethodName != null) {
                    if (method.getName().equals(getVersionMethodName) && method.getParameterCount() == 0 && method.getReturnType() != Void.class) {
                        ReflectionUtils.makeAccessible(method);
                        getVersion = false;
                        getVersionMethod = method;
                    }
                    if (getVersion) {
                        if (method.getName().equals(versionFieldName) && method.getParameterCount() == 0 && method.getReturnType() != Void.class) {
                            ReflectionUtils.makeAccessible(method);
                            getVersion = false;
                            getVersionMethod = method;
                        }
                    }
                }
            }
            return new TableInfo(tableName, schema, columns, idColumn, getIdMethod, getVersionMethod,
                    persist, postPersist, preUpdate, postUpdate, preRemove, postRemove, postLoad, allColumnFragment);
        });
    }

    private static String getMethodName(String fieldName) {
        if (fieldName != null) {
            if (fieldName.length() > 1) {
                return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            } else {
                return "get" + fieldName.toUpperCase();
            }
        }
        return null;
    }

    public static <T> boolean isNew(T entity) {
        String entityClassName = entity.getClass().getName();
        TableInfo tableInfo = tableInfoCache.get(entityClassName);
        Method getIdMethod = tableInfo.getIdMethod();
        Method getVersionMethod = tableInfo.getVersionMethod();
        if (getIdMethod != null) {
            return ReflectionUtils.invokeMethod(getIdMethod, entity) == null || (getVersionMethod != null && ReflectionUtils.invokeMethod(getVersionMethod, entity) == null);
        }
        return false;
    }

    public static <T> void onBeforeConvert(T entity) {
        String entityClassName = entity.getClass().getName();
        TableInfo tableInfo = tableInfoCache.get(entityClassName);
        if(tableInfo.prePersist() == null && tableInfo.preUpdate() == null) return;
        Method getIdMethod = tableInfo.getIdMethod();
        Method getVersionMethod = tableInfo.getVersionMethod();
        if (getIdMethod != null) {
            Object id = ReflectionUtils.invokeMethod(getIdMethod, entity);
            if (id != null && (getVersionMethod == null || ReflectionUtils.invokeMethod(getVersionMethod, entity) != null)) {
                Method preUpdate = tableInfo.preUpdate();
                if (preUpdate != null) {
                    if (log.isDebugEnabled())
                        log.debug("onBeforeConvert forward to {} @preUpdate method", entity.getClass().getName());
                    ReflectionUtils.invokeMethod(preUpdate, entity);
                }
            } else {
                Method prePersist = tableInfo.prePersist();
                if (prePersist != null) {
                    if (log.isDebugEnabled())
                        log.debug("onBeforeConvert forward to {} @prePersist method", entity.getClass().getName());
                    ReflectionUtils.invokeMethod(prePersist, entity);
                }
            }
        }
    }

    public static <T> void onAfterConvert(T entity) {
        String entityClassName = entity.getClass().getName();
        TableInfo tableInfo = tableInfoCache.get(entityClassName);
        if(tableInfo.postPersist() == null && tableInfo.postUpdate() == null) return;
        Method getIdMethod = tableInfo.getIdMethod();
        Method getVersionMethod = tableInfo.getVersionMethod();
        if (getIdMethod != null) {
            Object id = ReflectionUtils.invokeMethod(getIdMethod, entity);
            if (id != null && (getVersionMethod == null || ReflectionUtils.invokeMethod(getVersionMethod, entity) != null)) {
                Method postUpdate = tableInfo.postUpdate();
                if (postUpdate != null) {
                    if (log.isDebugEnabled())
                        log.debug("onAfterConvert forward to {} @postUpdate method", entity.getClass().getName());
                    ReflectionUtils.invokeMethod(postUpdate, entity);
                }
            } else {
                Method postPersist = tableInfo.postPersist();
                if (postPersist != null) {
                    if (log.isDebugEnabled())
                        log.debug("onAfterConvert forward to {} @postPersist method", entity.getClass().getName());
                    ReflectionUtils.invokeMethod(postPersist, entity);
                }
            }
        }
    }

    public static <T> void onBeforeDelete(T entity) {
        String entityClassName = entity.getClass().getName();
        TableInfo tableInfo = tableInfoCache.get(entityClassName);
        Method preRemove = tableInfo.preRemove();
        if (preRemove != null) {
            if (log.isDebugEnabled())
                log.debug("onBeforeDelete forward to {} @preRemove method", entity.getClass().getName());
            ReflectionUtils.invokeMethod(preRemove, entity);
        }
    }

    public static <T> void onAfterDelete(T entity) {
        String entityClassName = entity.getClass().getName();
        TableInfo tableInfo = tableInfoCache.get(entityClassName);
        Method postRemove = tableInfo.postRemove();
        if (postRemove != null) {
            if (log.isDebugEnabled())
                log.debug("onAfterDelete forward to {} @postRemove method", entity.getClass().getName());
            ReflectionUtils.invokeMethod(postRemove, entity);
        }
    }

}
