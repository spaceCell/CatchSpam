package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
//public class PatternLoaderService {
//    @Value("${app.patternsFilePath:/root/rm-del/list.yaml}")
//    private String patternsFilePath;
//
//    public List<String> loadPatterns() {
//        try (InputStream inputStream = Files.newInputStream(Paths.get(patternsFilePath))) {
//            Yaml yaml = new Yaml();
//            // Explicitly cast the result to List<String>
//            return (List<String>) yaml.load(inputStream);
//        } catch (IOException e) {
//            throw new RuntimeException("Error loading spam patterns", e);
//        }
//    }
//}

public class PatternLoaderService {
    @Value("classpath:list.yaml")
    private Resource patternsResource;

    public List<String> loadPatterns() {
        try (InputStream inputStream = patternsResource.getInputStream()) {
            System.out.println("Patterns Resource Path: " + patternsResource.getFile().getPath());
            Yaml yaml = new Yaml();
            // Explicitly cast the result to List<String>
            return (List<String>) yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading spam patterns", e);
        }
    }
}
