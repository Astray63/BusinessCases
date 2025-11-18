package com.electriccharge.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Facade for accessing authentication information
 * Centralizes SecurityContextHolder access and improves testability
 */
@Component
public class AuthenticationFacade {

    /**
     * Gets the current authentication object
     *
     * @return Optional containing the Authentication or empty if not authenticated
     */
    public Optional<Authentication> getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /**
     * Gets the username of the currently authenticated user
     *
     * @return Optional containing the username or empty if not authenticated
     */
    public Optional<String> getCurrentUsername() {
        return getAuthentication()
                .map(Authentication::getName);
    }

    /**
     * Gets the UserDetails of the currently authenticated user
     *
     * @return Optional containing UserDetails or empty if not authenticated
     */
    public Optional<UserDetails> getCurrentUserDetails() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> (UserDetails) principal);
    }

    /**
     * Checks if a user is currently authenticated
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return getAuthentication()
                .map(auth -> auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                .orElse(false);
    }

    /**
     * Gets the principal object (typically username or UserDetails)
     *
     * @return Optional containing the principal or empty if not authenticated
     */
    public Optional<Object> getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal);
    }

    /**
     * Checks if the current user has a specific authority/role
     *
     * @param authority The authority to check for
     * @return true if user has the authority, false otherwise
     */
    public boolean hasAuthority(String authority) {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority)))
                .orElse(false);
    }

    /**
     * Checks if the current user has any of the specified authorities/roles
     *
     * @param authorities The authorities to check for
     * @return true if user has at least one authority, false otherwise
     */
    public boolean hasAnyAuthority(String... authorities) {
        return getAuthentication()
                .map(auth -> {
                    for (String authority : authorities) {
                        if (auth.getAuthorities().stream()
                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority))) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }
}
