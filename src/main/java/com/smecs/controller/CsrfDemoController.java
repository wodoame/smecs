package com.smecs.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CsrfDemoController {

    @GetMapping("/security/csrf-demo")
    public String showForm(Model model, CsrfToken csrfToken) {
        model.addAttribute("csrfToken", csrfToken.getToken());
        return "csrf-demo";
    }

    @PostMapping("/security/csrf-demo")
    public String submit(@RequestParam("note") String note, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", "Form submitted with CSRF token. Note: " + note);
        return "redirect:/security/csrf-demo";
    }
}
