package com.counter.config;

import com.counter.controller.CountController;
import com.counter.exception.ExceptionHandler;
import io.javalin.Javalin;

public class RouteConfig {
    public static void register(Javalin app) {
        app.exception(IllegalStateException.class, ExceptionHandler::handle);
        app.post("/count", CountController::count);
    }
}
