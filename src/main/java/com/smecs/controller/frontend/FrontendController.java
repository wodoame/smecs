package com.smecs.controller.frontend;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("")
public class FrontendController {
    private final ResourceLoader resourceLoader;

    public FrontendController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = {"/", "/products", "/categories", "/about", "/contact", "/**/{path:[^\\.]*}"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource index() throws IOException {
        // Always serve index.html for frontend routes
        return resourceLoader.getResource("classpath:/static/index.html");
    }
}
