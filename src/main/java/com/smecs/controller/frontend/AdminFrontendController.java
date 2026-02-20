package com.smecs.controller.frontend;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminFrontendController {
    private final ResourceLoader resourceLoader;

    public AdminFrontendController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = { "/admin/inventories", "/admin/inventories/{id}" }, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource inventoriesPage() {
        // Serve index.html for /admin/inventories and /admin/inventories/{id}
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping(value = { "/admin/categories", "/admin/categories/{id}" }, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource categoriesPage() {
        // Serve index.html for /admin/categories and /admin/categories/{id}
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping(value = { "/admin/orders", "/admin/orders/{id}" }, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource ordersPage() {
        // Serve index.html for /admin/categories and /admin/categories/{id}
        return resourceLoader.getResource("classpath:/static/index.html");
    }
}
