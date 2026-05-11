package cn.dreamtof.content.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WordCountService {

    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final Pattern ENGLISH_WORD_PATTERN = Pattern.compile("[a-zA-Z]+");

    public int countWords(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        String plain = content.replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("`[^`]*`", "")
                .replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", "")
                .replaceAll("\\[[^\\]]*\\]\\([^)]*\\)", "")
                .replaceAll("[#*`>\\-\\[\\]()!|~]", "")
                .trim();
        int chineseCount = 0;
        Matcher chineseMatcher = CHINESE_PATTERN.matcher(plain);
        while (chineseMatcher.find()) {
            chineseCount++;
        }
        int englishCount = 0;
        Matcher englishMatcher = ENGLISH_WORD_PATTERN.matcher(plain);
        while (englishMatcher.find()) {
            englishCount++;
        }
        return chineseCount + englishCount;
    }
}
