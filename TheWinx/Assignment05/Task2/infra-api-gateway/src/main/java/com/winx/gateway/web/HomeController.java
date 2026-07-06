package com.winx.gateway.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        Object role = session.getAttribute("role");
        if ("PROVIDER".equals(role)) {
            return "redirect:/provider/dashboard";
        }
        if ("USER".equals(role)) {
            return "redirect:/dashboard";
        }
        return "landing";
    }
}
