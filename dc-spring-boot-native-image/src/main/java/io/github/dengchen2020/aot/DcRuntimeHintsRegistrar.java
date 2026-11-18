package io.github.dengchen2020.aot;

import io.github.dengchen2020.aot.utils.AotUtils;
import org.springframework.aot.hint.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.dengchen2020.aot.utils.CollectUtils.EMPTY_STRING_ARRAY;

/**
 * 基本注册
 * @author xiaochen
 * @since 2025/5/23
 */
class DcRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        staticResource(aotUtils);
        aotUtils.registerReflection(LinkedHashMap.class, HashMap.class, ArrayList.class, List.class, Map.class, Set.class, ConcurrentMap.class, ConcurrentHashMap.class, HashSet.class);
        caffeine(aotUtils);
        captcha(aotUtils);
        phonenumbers(aotUtils);
        alipay(aotUtils);
        aliyuncs(aotUtils);
        springai(aotUtils);
        telegram(aotUtils);
        geom(aotUtils);
        jackson(aotUtils);
        mybatis(aotUtils);
        mybatisPlus(aotUtils);
    }

    /**
     * 静态资源注册
     * @param aotUtils
     */
    private void staticResource(AotUtils aotUtils) {
        aotUtils.registerPattern("static/*");
    }

    /**
     * caffine的基本反射注册，正常情况下足够了
     */
    private void caffeine(AotUtils aotUtils) {
        Class<?> nodeFactory = aotUtils.loadClass("com.github.benmanes.caffeine.cache.NodeFactory");
        if (nodeFactory != null) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerTypes(TypeReference.listOf(aotUtils.toClassArray(
                    "com.github.benmanes.caffeine.cache.PD","com.github.benmanes.caffeine.cache.PDA","com.github.benmanes.caffeine.cache.PDAMS",
                    "com.github.benmanes.caffeine.cache.PDW", "com.github.benmanes.caffeine.cache.PDWMS","com.github.benmanes.caffeine.cache.PS",
                    "com.github.benmanes.caffeine.cache.PSA","com.github.benmanes.caffeine.cache.PSAMS","com.github.benmanes.caffeine.cache.PSW",
                    "com.github.benmanes.caffeine.cache.PSWMS")), builder -> builder.onReachableType(nodeFactory)
                    .withField("FACTORY")
                    .withMethod("<init>",Collections.emptyList(), ExecutableMode.INVOKE)));
        }
        Class<?> localCacheFactory = aotUtils.loadClass("com.github.benmanes.caffeine.cache.LocalCacheFactory");
        if (localCacheFactory != null) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerTypes(TypeReference.listOf(aotUtils.toClassArray(
                    "com.github.benmanes.caffeine.cache.SIMSA","com.github.benmanes.caffeine.cache.SIMSW",
                    "com.github.benmanes.caffeine.cache.SSMSA","com.github.benmanes.caffeine.cache.SSMSW")), builder -> builder.onReachableType(localCacheFactory)
                    .withField("FACTORY")
                    .withMethod("<init>",Collections.emptyList(), ExecutableMode.INVOKE)));
        }
    }

    private void captcha(AotUtils aotUtils) {
        Class<?> captcha = aotUtils.loadClass("com.wf.captcha.base.Captcha");
        if (captcha == null) return;
        // 仅添加少量字体，需要其他的自行添加
        aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerPattern(TypeReference.of(captcha),"actionj.ttf","epilog.ttf"));
    }

    private void phonenumbers(AotUtils aotUtils) {
        Class<?> phoneNumberUtil =  aotUtils.loadClass("com.google.i18n.phonenumbers.PhoneNumberUtil");
        if (phoneNumberUtil == null) return;
        // 这里仅添加中国大陆、中国香港、中国澳门、中国台湾、俄罗斯、新加坡、美国、韩国的手机号元数据，需要其他的自行添加
        aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerPattern(TypeReference.of(phoneNumberUtil),"com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_CN",
                "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_HK",
                "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_MO",
                "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_TW",
                "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_RU",
                "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_SG",
                "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_US",
                "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_KR"));
    }

    private void alipay(AotUtils aotUtils) {
        Class<?> alipayResponse = aotUtils.loadClass("com.alipay.api.AlipayResponse");
        if (alipayResponse == null) return;
        aotUtils.collectClass(alipayResponse::isAssignableFrom,"com.alipay.api").forEach(response -> {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(response,
                    builder -> builder.onReachableType(response)
                            .withMembers(MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)
                            .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE)));
            var cs = response.getDeclaredClasses();
            for (var c : cs) {
                aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c,
                        builder -> builder.onReachableType(c)
                                .withMembers(MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)
                                .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE)));
            }
        });
    }

    private void aliyuncs(AotUtils aotUtils) {
        if (!aotUtils.isPresent("com.aliyuncs.http.HttpClientFactory")) return;
        Class<?> apacheHttpClient = aotUtils.loadClass("com.aliyuncs.http.clients.ApacheHttpClient");
        if (apacheHttpClient == null) return;
        aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(apacheHttpClient));
        aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerPattern(TypeReference.of(apacheHttpClient),"endpoints.json"));
        var acsResponse = aotUtils.loadClass("com.aliyuncs.AcsResponse");
        aotUtils.collectClass(acsResponse::isAssignableFrom,"com.aliyuncs").forEach(response -> {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(response,
                    builder -> builder.onReachableType(response)
                            .withMembers(MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)
                            .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE)));
            var cs = response.getDeclaredClasses();
            for (var c : cs) {
                aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c,
                        builder -> builder.onReachableType(c)
                                .withMembers(MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)
                                .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE)));
            }
        });
    }

    private void springai(AotUtils aotUtils) {
        var embeddingOptions = aotUtils.loadClass("org.springframework.ai.embedding.EmbeddingOptions");
        if (embeddingOptions != null) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerReflection(builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS), embeddingOptions));
        }
    }

    private void telegram(AotUtils aotUtils) {
        var apiResponse = aotUtils.loadClass("org.telegram.telegrambots.meta.api.objects.ApiResponse");
        if (apiResponse == null) return;
        var botApiObject = aotUtils.loadClass("org.telegram.telegrambots.meta.api.interfaces.BotApiObject");
        aotUtils.collectClass(botApiObject::isAssignableFrom,"org.telegram.telegrambots.meta.api").forEach(apiObject -> {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(apiObject,
                    builder -> builder.onReachableType(apiObject)
                            .withMembers(MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)
                            .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE)));
            var cs = apiObject.getDeclaredClasses();
            for (var c : cs) {
                aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c,
                        builder -> builder.onReachableType(c)
                                .withMembers(MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)
                                .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE)));
            }
        });
    }

    private void geom(AotUtils aotUtils) {
        var geometry = aotUtils.loadClass("org.geolatte.geom.Geometry");
        if (geometry == null) return;
        var classes = aotUtils.collectClass("org.geolatte.geom.codec");
        var wkbDecoder = aotUtils.loadClass("org.geolatte.geom.codec.WkbDecoder");
        if (wkbDecoder != null) {
            classes.stream().filter(wkbDecoder::isAssignableFrom).forEach(c -> aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection()
                    .registerType(c, builder -> builder.onReachableType(c)
                            .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE))));
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerPattern(TypeReference.of(wkbDecoder), "spatial_ref_sys.txt"));
        }
        var wktDecoder = aotUtils.loadClass("org.geolatte.geom.codec.WktEncoder");
        if (wktDecoder != null) {
            classes.stream().filter(wktDecoder::isAssignableFrom).forEach(c -> aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection()
                    .registerType(c, builder -> builder.onReachableType(c)
                            .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE))));
        }
        var wkbEncoder = aotUtils.loadClass("org.geolatte.geom.codec.WkbEncoder");
        if (wkbEncoder != null) {
            classes.stream().filter(wkbEncoder::isAssignableFrom).forEach(c -> aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection()
                    .registerType(c, builder -> builder.onReachableType(c)
                            .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE))));
        }
        var wktEncoder = aotUtils.loadClass("org.geolatte.geom.codec.WktEncoder");
        if (wktEncoder != null) {
            classes.stream().filter(wktEncoder::isAssignableFrom).forEach(c -> aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection()
                    .registerType(c, builder -> builder.onReachableType(c)
                            .withMethod("<init>", Collections.emptyList(), ExecutableMode.INVOKE))));
        }
    }

    private void jackson(AotUtils aotUtils) {
        var objectMapper = aotUtils.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
        if (objectMapper != null) {
            aotUtils.collectClass("com.fasterxml.jackson.databind.node").forEach(c -> {
                aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerSerializable(TypeReference.of(c), c));
            });
        }
        var toolsObjectMapper = aotUtils.loadClass("tools.jackson.databind.ObjectMapper");
        if (toolsObjectMapper != null) {
            aotUtils.collectClass("tools.jackson.databind.node").forEach(c -> {
                aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerSerializable(TypeReference.of(c), c));
            });
        }
    }

    private void mybatis(AotUtils aotUtils) {
        var sqlSessionFactory = aotUtils.loadClass("org.apache.ibatis.session.SqlSessionFactory");
        if (sqlSessionFactory == null) return;
        for (var c : aotUtils.toClassArray("org.apache.ibatis.scripting.defaults.RawLanguageDriver",
                "org.apache.ibatis.scripting.xmltags.XMLLanguageDriver", "org.apache.ibatis.javassist.util.proxy.RuntimeSupport",
                "org.apache.ibatis.javassist.util.proxy.ProxyFactory", "org.apache.ibatis.logging.slf4j.Slf4jImpl",
                "org.apache.ibatis.logging.Log", "org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl",
                "org.apache.ibatis.logging.log4j2.Log4j2Impl", "org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl",
                "org.apache.ibatis.logging.stdout.StdOutImpl", "org.apache.ibatis.logging.nologging.NoLoggingImpl",
                "org.apache.ibatis.session.SqlSessionFactory", "org.apache.ibatis.cache.impl.PerpetualCache",
                "org.apache.ibatis.cache.decorators.FifoCache", "org.apache.ibatis.cache.decorators.LruCache",
                "org.apache.ibatis.cache.decorators.SoftCache", "org.apache.ibatis.cache.decorators.WeakCache",
                "org.mybatis.spring.SqlSessionFactoryBean", "java.util.ArrayList", "java.util.HashMap", "java.util.TreeSet",
                "java.util.HashSet")) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c, builder -> builder
                    .withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)));
        }
        aotUtils.runWithIgnoreIllegalArgument(() -> {
            try {
                aotUtils.registerPattern(aotUtils.findResources("org/apache/ibatis/builder/xml",
                        name -> name.endsWith(".dtd") || name.endsWith(".xsd")).toArray(EMPTY_STRING_ARRAY));
            } catch (IOException ignored) {}
        });
    }

    private void mybatisPlus(AotUtils aotUtils) {
        var wrapper = aotUtils.loadClass("com.baomidou.mybatisplus.core.conditions.Wrapper");
        if (wrapper == null) return;
        var sqlSessionFactory = aotUtils.loadClass("org.apache.ibatis.session.SqlSessionFactory");
        if (sqlSessionFactory == null) return;
        for (var c : aotUtils.toClassArray("org.apache.ibatis.mapping.BoundSql")) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c, builder -> builder
                    .withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)));
        }
        var statementHandler = aotUtils.loadClass("org.apache.ibatis.executor.statement.StatementHandler");
        if (statementHandler != null) {
            aotUtils.collectClass(statementHandler::isAssignableFrom, "org.apache.ibatis.executor.statement").forEach(c -> {
                aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c, builder -> builder
                        .withMembers(MemberCategory.INVOKE_DECLARED_METHODS)));
            });
        }
        var baseStatementHandler = aotUtils.loadClass("org.apache.ibatis.executor.statement.BaseStatementHandler");
        if (baseStatementHandler != null) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(baseStatementHandler, builder -> builder
                    .withMembers(MemberCategory.DECLARED_FIELDS)));
        }
        var routingStatementHandler = aotUtils.loadClass("org.apache.ibatis.executor.statement.RoutingStatementHandler");
        if (routingStatementHandler != null) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(routingStatementHandler, builder -> builder
                    .withMembers(MemberCategory.DECLARED_FIELDS)));
        }
        for (Class<?> c : aotUtils.toClassArray("org.apache.ibatis.executor.Executor", "org.apache.ibatis.executor.statement.StatementHandler")) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().proxies().registerJdkProxy(builder -> builder
                    .proxiedInterfaces(c)));
        }
        aotUtils.registerSerializable(TypeReference.of(wrapper),"com.baomidou.mybatisplus.core.toolkit.support.SFunction");
        for (var c : aotUtils.toClassArray("com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver",
                "com.baomidou.mybatisplus.core.conditions.ISqlSegment")) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c, builder -> builder
                    .withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)));
        }
        for (var c : aotUtils.collectClass(wrapper::isAssignableFrom, "com.baomidou.mybatisplus")) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(c, builder -> builder
                    .withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)));
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.registerSerializable(TypeReference.of(c), c));
        }
        var mybatisMapperProxy = aotUtils.loadClass("com.baomidou.mybatisplus.core.override.MybatisMapperProxy");
        if (mybatisMapperProxy != null) {
            aotUtils.runWithIgnoreIllegalArgument(() -> aotUtils.hints().reflection().registerType(TypeReference.of(mybatisMapperProxy), builder -> builder
                    .withField("sqlSession")
                    .withField("mapperInterface")
                    .withMethod("getSqlSession", Collections.emptyList(), ExecutableMode.INVOKE)
                    .withMethod("getMapperInterface", Collections.emptyList(), ExecutableMode.INVOKE)));
        }
    }

}
