package com.jack.jobportal.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("The username " + userDetails.getUsername() + " is logged in.");
        boolean hasRelevantRole = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("Job Seeker") || auth.getAuthority().equals("Recruiter"));

        if (hasRelevantRole) {
            response.sendRedirect("/dashboard/");
        }
    }
}
