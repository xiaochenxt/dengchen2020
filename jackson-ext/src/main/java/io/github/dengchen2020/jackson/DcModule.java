package io.github.dengchen2020.jackson;

import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;

/**
 * 全局模块
 * @author xiaochen
 * @since 2026/3/23
 */
public class DcModule extends JacksonModule {

    @Override
    public String getModuleName() {
        return "dc";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.insertAnnotationIntrospector(JsonRawValueInputAnnotationIntrospector.INSTANCE);
    }

}
