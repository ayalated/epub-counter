package com.counter;

import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.UploadedFile;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.eclipse.jetty.http.HttpStatus;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        // 启动web服务
        Javalin app = Javalin.create(config -> {
            config.jetty.multipartConfig.maxFileSize(10 * 1024 * 1024, SizeUnit.BYTES);
            config.jetty.multipartConfig.maxTotalRequestSize(20 * 1024 * 1024, SizeUnit.BYTES);
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
        }).start(13824);

        // 异常处理
        app.exception(IllegalStateException.class, ((e, ctx) -> {
            if (e.getMessage() != null && e.getMessage().contains("max filesize")) {
                ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413).json(Map.of("error", "file too large"));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).json(Map.of("error", "server error"));
            }
        }));

        app.post("/", ctx -> {
            List<UploadedFile> files = ctx.uploadedFiles("file");
            if (files.isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST_400).json(Map.of("error", "no files uploaded"));
            }

            List<Map<String, Object>> results = new ArrayList<>();

            for (UploadedFile file : files) {
                String name = file.filename();

                // 校验
                if (!name.endsWith(".epub")) {
                    ctx.status(HttpStatus.BAD_REQUEST_400).json(Map.of(
                            "error", "must upload EPUB files",
                            "filename", name
                    ));
                    return;
                }

                // 统计
                CountResult countResult;

                try (InputStream is = file.content()) {
                    countResult = EpubReader.fromUpload(is).count(false);
                } catch (Exception e) {
                    ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).json(Map.of(
                            "error", "failed to parse epub",
                            "filename", name
                    ));
                    return;
                }

                results.add(Map.of(
                        "filename", name,
                        "characters", countResult.getTotal()
                ));
            }

            ctx.json(Map.of(
                    "totalFiles", results.size(),
                    "results", results
            ));
        });
    }
}
