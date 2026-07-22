package com.example.webstarter.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ErrorPageController implements ErrorController {

    @RequestMapping("/error")
    public String error(HttpServletRequest request, HttpServletResponse response, Model model) {
        int status = resolveStatus(request);
        response.setStatus(status);
        model.addAttribute("status", status);
        model.addAttribute("requestId", request.getAttribute(RequestContext.REQUEST_ID_ATTRIBUTE));
        return status == HttpStatus.NOT_FOUND.value() ? "errors/404" : "errors/error";
    }

    @RequestMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied(HttpServletRequest request, Model model) {
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        model.addAttribute("requestId", request.getAttribute(RequestContext.REQUEST_ID_ATTRIBUTE));
        return "errors/403";
    }

    private int resolveStatus(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status instanceof Integer statusCode && statusCode >= 400 && statusCode <= 599) {
            return statusCode;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
