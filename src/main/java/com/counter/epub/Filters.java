package com.counter.epub;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Filters {
    public static boolean shouldSkip(String href, Document doc, String text) {
        return skipByName(href)
                || skipByEpubType(doc)
                || text.codePointCount(0, text.length()) < 200;
    }

    private static boolean skipByName(String href) {
        String name = href.toLowerCase();
        return name.contains("toc")
                || name.contains("nav")
                || name.contains("copyright")
                || name.contains("colophon")
                || name.contains("title")
                || name.contains("cover");
    }


    private static boolean skipByEpubType(Document doc) {
        Elements elems = doc.select("[epub\\:type]");
        for (org.jsoup.nodes.Element e : elems) {
            String type = e.attr("epub:type");
            if (type.contains("toc")
                    || type.contains("frontmatter")
                    || type.contains("copyright")
                    || type.contains("titlepage")
                    || type.contains("nav")) {
                return true;
            }
        }
        return false;
    }
}
