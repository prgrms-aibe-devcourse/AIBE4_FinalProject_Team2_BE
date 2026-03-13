package com.aibe.team2.domain.jobposting.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JobPostingCrawlerService {

    // [리뷰 반영] Jsoup 연결 공통 로직을 별도 메서드로 분리
    private Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get();
    }

    public Map<String, String> crawlAndExtract(String url) {
        Map<String, String> extractedData = new HashMap<>();

        try {
            // 분리된 메서드를 호출하여 Document 획득
            Document doc = getDocument(url);

            extractedData.put("title", doc.title());
            extractedData.put("fullDescription", doc.body().text());

            extractedData.put("mainTasks", extractSectionByDOM(doc, "주요업무", "주요 업무", "주요 업무 내용"));
            extractedData.put("qualifications", extractSectionByDOM(doc, "자격요건", "자격 요건", "지원자격"));
            extractedData.put("preferred", extractSectionByDOM(doc, "우대사항", "우대 사항", "우대조건"));
            extractedData.put("benefits", extractSectionByDOM(doc, "복리후생", "혜택 및 복지", "혜택"));

        } catch (IOException e) {
            log.error("Failed to crawl URL: {}", url, e);
        }

        return extractedData;
    }

    private String extractSectionByDOM(Document doc, String... keywords) {
        for (String keyword : keywords) {
            String selector = String.format("h2:contains(%s), h3:contains(%s), h4:contains(%s), h5:contains(%s), strong:contains(%s), b:contains(%s), dt:contains(%s)",
                    keyword, keyword, keyword, keyword, keyword, keyword, keyword);

            Elements headers = doc.select(selector);

            if (!headers.isEmpty()) {
                Element header = headers.first();
                StringBuilder sectionText = new StringBuilder();
                Element nextElement = header.nextElementSibling();

                while (nextElement != null) {
                    if (nextElement.tagName().matches("h[1-6]|dt") ||
                            nextElement.text().matches(".*(주요업무|자격요건|우대사항|복리후생|마감일|근무지|접수).*")) {
                        break;
                    }
                    if (!nextElement.text().isBlank()) {
                        sectionText.append(nextElement.text()).append("\n");
                    }
                    nextElement = nextElement.nextElementSibling();
                }

                if (sectionText.length() == 0 && header.parent() != null) {
                    return header.parent().text().replace(header.text(), "").trim();
                }

                if (sectionText.length() > 0) {
                    return sectionText.toString().trim();
                }
            }
        }
        return null;
    }
}