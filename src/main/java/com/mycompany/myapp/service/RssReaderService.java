package com.mycompany.myapp.service;

import java.io.StringReader;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

@Service
public class RssReaderService {

    private final HttpClient httpClient;

    public RssReaderService() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    }

    public List<RssItem> read(String rssUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(rssUrl))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "CIjhipster-RSS-Reader/1.0")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("RSS source returned HTTP " + response.statusCode());
            }

            return parseFeed(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read RSS source: " + rssUrl + ". " + e.getMessage(), e);
        }
    }

    private List<RssItem> parseFeed(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            document.getDocumentElement().normalize();

            List<RssItem> items = new ArrayList<>();

            NodeList rssItems = document.getElementsByTagName("item");

            for (int i = 0; i < rssItems.getLength(); i++) {
                Node node = rssItems.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element element = (Element) node;

                String title = clean(getTagValue(element, "title"));
                String link = clean(getTagValue(element, "link"));
                String description = clean(getTagValue(element, "description"));
                String pubDate = clean(getTagValue(element, "pubDate"));
                String guid = clean(getTagValue(element, "guid"));

                if (!isBlank(title) && !isBlank(link)) {
                    items.add(new RssItem(title, link, description, parseDate(pubDate), guid));
                }
            }

            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse RSS XML: " + e.getMessage(), e);
        }
    }

    private String getTagValue(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            return null;
        }

        Node node = nodes.item(0);

        if (node == null) {
            return null;
        }

        return node.getTextContent();
    }

    private Instant parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (Exception ignored) {}

        try {
            return Instant.parse(value);
        } catch (Exception ignored) {}

        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        return value
            .replaceAll("<[^>]*>", " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record RssItem(String title, String link, String description, Instant publishedAt, String guid) {}
}
