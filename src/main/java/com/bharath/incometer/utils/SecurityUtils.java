package com.bharath.incometer.utils;

import com.bharath.incometer.models.user.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class SecurityUtils {

    /**
     * Get current JWT if principal is OAuth2 Jwt
     */
    public static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            return (Jwt) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get current authenticated user's email from either JWT or UserDetails
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Handle OAuth2 JWT
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        // Handle UserDetails (custom JWT)
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return null;
    }

    /**
     * Get current authenticated user's UUID from UserPrincipal
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Handle UserPrincipal (custom JWT)
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        // Handle OAuth2 JWT - extract UUID from claims
        if (principal instanceof Jwt jwt) {
            String uuidStr = jwt.getClaimAsString("uuid");
            if (uuidStr != null) {
                try {
                    return UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }

        return null;
    }
}
