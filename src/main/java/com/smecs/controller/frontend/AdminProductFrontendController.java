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
@RequestMapping("/admin/products")
public class AdminProductFrontendController  {
    private final ResourceLoader resourceLoader;

    public AdminProductFrontendController (ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = {"", "/{id}"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource index() throws IOException {
        // Serve index.html for /categories and /categories/{id}
        return resourceLoader.getResource("classpath:/static/index.html");
    }
}
