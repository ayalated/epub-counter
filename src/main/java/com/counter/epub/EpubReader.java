package com.counter.epub;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubReader {
    private final Path epubPath;
    private final boolean isTemp;

    public EpubReader(Path epubPath, boolean isTemp) {
        this.epubPath = epubPath;
        this.isTemp = isTemp;
    }

    public CountResult count() throws Exception {
        CountResult result = new CountResult();
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


            for (String href : contentFiles) {
                String basePath = opfPath.substring(0, opfPath.lastIndexOf('/') + 1);
                ZipEntry entry = zipFile.getEntry(basePath + href);
                if (entry == null) continue;


                InputStream htmlStream = zipFile.getInputStream(entry);

                org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlStream, "UTF-8", "");

                String text = htmlDoc.body().text();

                if (Filters.shouldSkip(href, htmlDoc, text)) continue;
                int count = text.codePointCount(0, text.length());
                result.add(count);
            }
            return result;
        } finally {
            if (isTemp) {
                Files.deleteIfExists(epubPath);
            }
        }
    }


    public static EpubReader fromUpload(InputStream is) throws IOException {
        Path tempFile = Files.createTempFile("upload-", ".epub");
        Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

        return new EpubReader(tempFile, true);
    }
}
