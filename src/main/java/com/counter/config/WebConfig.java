package com.counter.config;

import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.plugin.bundled.CorsPluginConfig;

public class WebConfig {
    public static Javalin createApp() {
        return Javalin.create(config -> {
            config.jetty.multipartConfig.maxFileSize(10 * 1024 * 1024, SizeUnit.BYTES);
            config.jetty.multipartConfig.maxTotalRequestSize(20 * 1024 * 1024, SizeUnit.BYTES);
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
        });
    }
}
