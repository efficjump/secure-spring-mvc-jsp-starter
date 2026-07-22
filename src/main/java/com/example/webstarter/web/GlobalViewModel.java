package com.example.webstarter.web;

import jakarta.servlet.http.HttpServletRequest;

import com.example.webstarter.config.SecurityProperties;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewModel {

    private final SecurityProperties securityProperties;

    public GlobalViewModel(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @ModelAttribute("registrationEnabled")
    public boolean registrationEnabled() {
        return securityProperties.registration().enabled();
    }

    @ModelAttribute("passwordMaxLength")
    public int passwordMaxLength() {
        return securityProperties.password().maxLength();
    }

    @ModelAttribute("embeddedView")
    public boolean embeddedView(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getParameter("embedded"))
                || "iframe".equalsIgnoreCase(request.getHeader("Sec-Fetch-Dest"));
    }
}
