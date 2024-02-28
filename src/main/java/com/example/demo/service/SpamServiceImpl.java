package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SpamServiceImpl implements SpamService {
    @Autowired
    private PatternLoaderService patternLoaderService;

    @Override
    public boolean isSpam(String message) {
        List<String> regexPatterns = patternLoaderService.loadPatterns();
        String cleanedMessage = message.replaceAll("[^\\p{L}\\p{N}\\s]", "").toLowerCase();

        log.info("cleanedMessage {}", cleanedMessage);

        boolean spamFound = regexPatterns.stream()
                .anyMatch(pattern -> {
                    if (pattern != null) {
                        boolean match = Pattern.compile(pattern).matcher(cleanedMessage).find();
                        if (match) {
                            log.info("Spam pattern found: {}", pattern);
                        }
                        return match;
                    } else {
                        return false;
                    }
                });

        return spamFound;
    }
}
