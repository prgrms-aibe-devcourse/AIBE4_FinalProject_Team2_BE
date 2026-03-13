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

    // [리뷰 반영] 공통 크롤링 로직 (Document 객체 반환)
    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get();
    }

    // [리뷰 반영] 전체 텍스트만 필요한 경우 사용하는 메서드 (외부 서비스용)
    public String crawlFullText(String url) {
        try {
            Document doc = getDocument(url);
            return doc.body().text();
        } catch (IOException e) {
            log.error("웹 페이지 텍스트 크롤링 실패: {}", url, e);
            return null;
        }
    }

    public Map<String, String> crawlAndParse(String url) {
        Map<String, String> parsedData = new HashMap<>();

        try {
            // 중복된 Jsoup.connect 로직 대신 내부 메서드 재사용
            Document doc = getDocument(url);

            parsedData.put("title", doc.title());
            parsedData.put("fullDescription", doc.body().text());

            // 구조적 파싱 메서드 호출 (이전 답변에서 반영한 DOM 탐색 로직)
            parsedData.put("mainTasks", extractSectionByDOM(doc, "주요업무", "주요 업무", "주요 업무 내용"));
            parsedData.put("qualifications", extractSectionByDOM(doc, "자격요건", "자격 요건", "지원자격"));
            parsedData.put("preferred", extractSectionByDOM(doc, "우대사항", "우대 사항", "우대조건"));
            parsedData.put("benefits", extractSectionByDOM(doc, "복리후생", "혜택 및 복지", "혜택"));

        } catch (IOException e) {
            log.error("Failed to crawl URL: {}", url, e);
        }

        return parsedData;
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

                if (sectionText.isEmpty() && header.parent() != null) {
                    String parentText = header.parent().text();
                    return parentText.replace(header.text(), "").trim();
                }

                if (!sectionText.isEmpty()) {
                    return sectionText.toString().trim();
                }
            }
        }
        return null;
    }
}