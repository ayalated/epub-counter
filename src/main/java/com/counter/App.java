package com.counter;

import com.counter.config.RouteConfig;
import com.counter.config.WebConfig;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        // 启动web服务
        Javalin app = WebConfig.createApp();
        RouteConfig.register(app);
        app.start(8080);
    }
}
