package io.github.dengchen2020.jackson;

import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson模块-dc
 * @author xiaochen
 * @since 2026/3/23
 */
public class DcModule extends SimpleModule {

    @Override
    public void setupModule(SetupContext context) {
        context.insertAnnotationIntrospector(JsonRawValueInputAnnotationIntrospector.INSTANCE);
    }

}
