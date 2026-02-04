package com.smecs.controller.frontend;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserFrontendController {
    private final ResourceLoader resourceLoader;

    public UserFrontendController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = {"/categories", "/categories/{id}"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource categoriesPage() {
        // Serve index.html for /categories and /categories/{id}
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping(value = {"/products", "/products/{id}"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource productsPage() {
        // Serve index.html for /products and /products/{id}
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping(value = "/about", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource aboutPage() {
        // Serve index.html for /about
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping(value = "/contact", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource contactPage() {
        // Serve index.html for /contact
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping(value = "/signup", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource signupPage() {
        // Serve index.html for /contact
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource loginPage() {
        // Serve index.html for /contact
        return resourceLoader.getResource("classpath:/static/index.html");
    }
}
