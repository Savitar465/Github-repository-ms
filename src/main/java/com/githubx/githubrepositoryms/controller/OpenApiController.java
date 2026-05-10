package com.githubx.githubrepositoryms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Home redirection to OpenAPI / Swagger UI
 */
@Controller
public class OpenApiController {

    @RequestMapping("/")
    public String index() {
        // Redirect to the Swagger UI page (keeps same behavior as generated controllers)
        return "redirect:swagger-ui.html";
    }

}

