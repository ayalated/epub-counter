package com.counter.controller;

import com.counter.epub.CountResult;
import com.counter.epub.EpubReader;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.eclipse.jetty.http.HttpStatus;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountController {
    public static void count(Context ctx) {
        List<UploadedFile> files = ctx.uploadedFiles("file");
        if (files.isEmpty()) {
            ctx.status(HttpStatus.BAD_REQUEST_400).json(Map.of("error", "no files uploaded"));
            return;
        }

        List<Map<String, Object>> results = new ArrayList<>();

        for (UploadedFile file : files) {
            handleOneFile(file, results, ctx);
        }

        ctx.json(Map.of(
                "totalFiles", results.size(),
                "results", results
        ));
    }


    private static void handleOneFile(UploadedFile file, List<Map<String, Object>> results, Context ctx) {
        String name = file.filename();

        if (!name.toLowerCase().endsWith(".epub")) {
            ctx.status(HttpStatus.BAD_REQUEST_400).json(Map.of("error", "must upload EPUB files", "filename", name));
            return;
        }

        try (InputStream is = file.content()) {
            CountResult result = EpubReader.fromUpload(is).count();
            results.add(Map.of("filename", name, "characters", result.getTotal()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).json(Map.of(
                    "error", "failed to parse epub",
                    "filename", name
            ));
        }
    }
}
