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
@RequestMapping("/contact")
public class ContactFrontendController {
    private final ResourceLoader resourceLoader;

    public ContactFrontendController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource index() throws IOException {
        // Serve index.html for /contact
        return resourceLoader.getResource("classpath:/static/index.html");
    }
}
