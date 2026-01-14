package com.counter.exception;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class ExceptionHandler {
    public static void handle(IllegalStateException e, Context ctx) {
        if (e.getMessage() != null && e.getMessage().contains("max filesize")) {
            ctx.status(HttpStatus.CONTENT_TOO_LARGE);
        } else {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
