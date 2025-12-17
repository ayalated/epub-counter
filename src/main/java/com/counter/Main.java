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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: epub-count <file.epub> [--chapters]");
            System.exit(1);
        }

        Path epubPath = Paths.get(args[0]);
        boolean byChapter = Arrays.asList(args).contains("--chapters");

        if (!Files.exists(epubPath)) {
            System.err.println("File not found: " + epubPath);
            System.exit(1);
        }

        try {
            EpubReader reader = new EpubReader(epubPath);
            CountResult result = reader.count(byChapter);
            result.print();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


}

