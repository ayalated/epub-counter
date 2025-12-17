package com.counter;


import org.jsoup.nodes.Document;

public class TextExtractor {

    public static String extractBodyText(Document doc) {
        doc.select("rt").remove();

        return doc.body().text();
    }
}
