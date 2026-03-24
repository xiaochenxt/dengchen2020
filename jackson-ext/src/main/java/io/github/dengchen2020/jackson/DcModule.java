package io.github.dengchen2020.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

/**
 * 全局模块
 * @author xiaochen
 * @since 2026/3/23
 */
public class DcModule extends Module {

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
