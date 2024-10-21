package org.example.lowcodekg.controller;

import org.example.lowcodekg.service.YamlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class YamlController {

    @Autowired
    private YamlService yamlService;

    @GetMapping("/api/yaml")
    public Map<String, Object> getYamlData() {
        return yamlService.loadYaml();
    }
}

