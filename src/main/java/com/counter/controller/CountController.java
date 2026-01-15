package com.counter.controller;

import com.counter.epub.CountResult;
import com.counter.epub.EpubReader;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.eclipse.jetty.http.HttpStatus;

import java.io.InputStream;
import java.util.Map;

public class CountController {
    public static void count(Context ctx) {
        UploadedFile file = ctx.uploadedFile("file");

        if (file == null) {
            ctx.status(HttpStatus.BAD_REQUEST_400).json(Map.of("error", "no files uploaded"));
            return;
        }


        String name = file.filename();

        if (!name.toLowerCase().endsWith(".epub")) {
            ctx.status(HttpStatus.BAD_REQUEST_400).json(Map.of("error", "must upload EPUB files", "filename", name));
            return;
        }

        try (InputStream is = file.content()) {
            CountResult r = EpubReader.fromUpload(is).count();
            ctx.status(HttpStatus.OK_200).json(Map.of("filename", name, "characters", r.getTotal()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).json(Map.of(
                    "error", "failed to parse epub",
                    "filename", name
            ));
        }
    }

}
