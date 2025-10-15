package io.github.dengchen2020.jpa.config;

import io.github.dengchen2020.jpa.base.BaseJpaRepositoryFactoryBean;
import io.github.dengchen2020.jpa.base.JpaRepositoryTypeFilter;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.annotation.*;

/**
 * JPA仓库接口扫描，无缝替换@EnableJpaRepositories
 * @author xiaochen
 * @since 2025/8/15
 */
@EnableJpaRepositories(includeFilters = {@ComponentScan.Filter(type = FilterType.CUSTOM, value = {JpaRepositoryTypeFilter.class})}, repositoryFactoryBeanClass = BaseJpaRepositoryFactoryBean.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableXcJpaRepositories {

    /**
     * {@link #basePackages（）} 属性的别名。允许更简洁的注释声明，例如：
     * {@code @EnableJpaRepositories（“org.my.pkg”）} 而不是 {@code @EnableJpaRepositories（basePackages=“org.my.pkg”）}。
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    String[] value() default {};

    /**
     * 用于扫描带注释的组件的基本包。{@link #value（）} 是 this 的别名（并且与之互斥）
     * 属性。使用 {@link #basePackageClasses（）} 作为基于字符串的包名称的类型安全替代方案。
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    String[] basePackages() default {};

    /**
     * {@link #basePackages（）} 的类型安全替代方案，用于指定要扫描带注释的组件的包。这
     * 将扫描指定每个类别的包装。考虑在
     * 每个包除了被此属性引用之外没有其他用途。
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    Class<?>[] basePackageClasses() default {};

    /**
     * 指定哪些类型不符合组件扫描的条件。
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    ComponentScan.Filter[] excludeFilters() default {};

    /**
     * 返回查找自定义存储库实现时要使用的后缀。默认为 {@literal impl}。所以
     * 对于名为 {@code PersonRepository} 的存储库，将查找相应的实现类
     * 对于 {@code PersonRepositoryImpl}。
     *
     * @return
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    String repositoryImplementationPostfix() default "Impl";

    /**
     * 配置查找 Spring Data 命名查询属性文件的位置。将默认为
     * {@code META-INF/jpa-named-queries.properties}。
     *
     * @return
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    String namedQueriesLocation() default "";

    /**
     * 返回 {@link QueryLookupStrategy} 的键，用于查询方法的查找查询。默认为
     * {@link QueryLookupStrategy.Key#CREATE_IF_NOT_FOUND}。
     *
     * @return
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

    /**
     * 配置要用于为此特定配置创建存储库代理的存储库基类。
     *
     * @return
     * @since 1.9
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    /**
     * 配置一个特定的 {@link BeanNameGenerator} 在创建存储库 Bean 时使用。
     * @return要使用的 {@link BeanNameGenerator} 或基本 {@link BeanNameGenerator} 接口来指示上下文默认值。
     * @since 3.4
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    // JPA specific configuration

    /**
     * 配置用于创建存储库的 {@link EntityManagerFactory} bean 定义的名称
     * 通过此注释发现。默认为 {@code entityManagerFactory}。
     *
     * @return
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    String entityManagerFactoryRef() default "entityManagerFactory";

    /**
     * 配置要用于创建存储库的 {@link PlatformTransactionManager} bean 定义的名称
     * 通过此注释发现。默认为 {@code transactionManager}。
     *
     * @return
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    String transactionManagerRef() default "transactionManager";

    /**
     * 配置嵌套的存储库接口（例如，定义为内部类）是否应由
     * 存储库基础设施。
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    boolean considerNestedRepositories() default false;

    /**
     * 配置是否为 Spring Data JPA 存储库启用默认事务。默认为 {@literal true}。如果
     * disabled，则存储库必须在配置事务的外观后面使用（例如，使用 Spring 的注释
     * 驱动的事务设施）或存储库方法必须用于划分事务。
     *
     * @return是否启用默认事务，默认为 {@literal true}。
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    boolean enableDefaultTransactions() default true;

    /**
     * 配置在引导生命周期中初始化存储库的时间。{@link BootstrapMode#DEFAULT}
     *（默认）表示预切初始化，但所有用 {@link Lazy} 注释的存储库接口除外，
     * {@link BootstrapMode#LAZY} 表示默认延迟，包括将延迟初始化代理注入客户端
     * bean，以便可以实例化这些 bean，但只会在第一次使用存储库时触发初始化（即
     * 方法调用）。这意味着当应用程序上下文具有
     * 完成其引导。{@link BootstrapMode#DEFERRED} 与 {@link BootstrapMode#LAZY} 基本相同，
     * 但当应用程序上下文完成其引导时触发存储库初始化。
     *
     * @return
     * @since 2.1
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    BootstrapMode bootstrapMode() default BootstrapMode.DEFAULT;

    /**
     * 配置用于在派生查询中转义通配符 {@literal _} 和 {@literal %} 的字符，并使用
     * {@literal contains}、{@literal startsWith} 或 {@literal endsWith} 子句。
     *
     * @return用于转义的单个字符。
     */
    @AliasFor(annotation = EnableJpaRepositories.class)
    char escapeCharacter() default '\\';


}
