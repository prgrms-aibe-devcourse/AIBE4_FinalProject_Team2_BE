package com.aibe.team2.domain.jobposting.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JobPostingCrawlerService {

    public Map<String, String> crawlAndParse(String url) {
        Map<String, String> parsedData = new HashMap<>();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .get();

            String title = doc.title();
            String fullText = doc.body().text();

            parsedData.put("title", title);
            parsedData.put("fullDescription", fullText);

            // 주요 항목 파싱
            parsedData.put("mainTasks", extractSection(fullText, new String[]{"주요업무", "주요 업무"}, "자격요건", "자격 요건", "우대사항", "복리후생"));
            parsedData.put("qualifications", extractSection(fullText, new String[]{"자격요건", "자격 요건"}, "우대사항", "우대 사항", "복리후생", "마감일"));
            parsedData.put("preferred", extractSection(fullText, new String[]{"우대사항", "우대 사항"}, "복리후생", "혜택", "마감일", "접수"));
            parsedData.put("benefits", extractSection(fullText, new String[]{"복리후생", "혜택 및 복지", "복지"}, "마감일", "근무지", "접수", "유의사항"));

        } catch (IOException e) {
            log.error("Failed to crawl URL: {}", url, e);
        }

        return parsedData;
    }

    private String extractSection(String fullText, String[] startKeywords, String... endKeywords) {
        int startIndex = -1;
        String matchedStartKeyword = "";

        for (String keyword : startKeywords) {
            startIndex = fullText.indexOf(keyword);
            if (startIndex != -1) {
                matchedStartKeyword = keyword;
                break;
            }
        }

        if (startIndex == -1) return null;

        int contentStart = startIndex + matchedStartKeyword.length();
        int endIndex = fullText.length();

        for (String end : endKeywords) {
            int index = fullText.indexOf(end, contentStart);
            if (index != -1 && index < endIndex) {
                endIndex = index;
            }
        }

        return fullText.substring(contentStart, endIndex).trim();
    }
}