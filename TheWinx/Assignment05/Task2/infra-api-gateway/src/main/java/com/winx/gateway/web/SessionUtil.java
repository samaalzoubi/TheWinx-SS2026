package com.winx.gateway.web;

import jakarta.servlet.http.HttpSession;

final class SessionUtil {

    private SessionUtil() {
    }

    static Long requireRole(HttpSession session, String role) {
        if (!role.equals(session.getAttribute("role"))) {
            return null;
        }
        return (Long) session.getAttribute("principalId");
    }
}
