package com.counter;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Main {
    public static void main(String[] args) {
        Path epubPath = Paths.get("C:", "Users", "lgh", "Downloads", "火花 (文春e-book) (又吉直樹) (Z-Library).epub");

        try (ZipFile zipFile = new ZipFile(epubPath.toFile(), StandardCharsets.UTF_8)) {
            ZipEntry containerEntry = zipFile.getEntry("META-INF/container.xml");
            InputStream is = zipFile.getInputStream(containerEntry);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);


            NodeList roots = doc.getElementsByTagName("rootfile");
            Element root = (Element) roots.item(0);
            String opfPath = root.getAttribute("full-path");

            ZipEntry opfEntry = zipFile.getEntry(opfPath);
            InputStream opfStream = zipFile.getInputStream(opfEntry);
            Document opfDoc = builder.parse(opfStream);

            Map<String, String> manifest = new HashMap<>();

            NodeList items = opfDoc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                manifest.put(
                        item.getAttribute("id"),
                        item.getAttribute("href")
                );
            }

            List<String> contentFiles = new ArrayList<>();
            NodeList itemrefs = opfDoc.getElementsByTagName("itemref");
            for (int i = 0; i < itemrefs.getLength(); i++) {
                Element ref = (Element) itemrefs.item(i);
                String idref = ref.getAttribute("idref");
                contentFiles.add(manifest.get(idref));
            }


            int totalChars = 0;
            for (String href : contentFiles) {
                String basePath = opfPath.substring(0, opfPath.lastIndexOf('/') + 1);
                ZipEntry entry = zipFile.getEntry(basePath + href);
                if (entry == null) continue;


                InputStream htmlStream = zipFile.getInputStream(entry);

                org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlStream, "UTF-8", "");


                if (shouldSkipByFileName(href)) continue;

                if (shouldSkipByEpubType(htmlDoc)) continue;

                String text = htmlDoc.body().text();
                if (text.codePointCount(0, text.length()) < 200) continue;

                totalChars += text.codePointCount(0, text.length());
            }

            System.out.println(zipFile.getName() + ": " + totalChars);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected static boolean shouldSkipByFileName(String href) {
        String name = href.toLowerCase();
        return name.contains("toc")
                || name.contains("nav")
                || name.contains("copyright")
                || name.contains("colophon")
                || name.contains("title")
                || name.contains("cover");
    }


    protected static boolean shouldSkipByEpubType(org.jsoup.nodes.Document doc) {
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

