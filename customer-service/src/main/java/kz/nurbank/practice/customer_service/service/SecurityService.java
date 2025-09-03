package kz.nurbank.practice.customer_service.service;

import kz.nurbank.practice.customer_service.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    public boolean isOwnCustomer(Long customerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return false;
        }
        return user.getCustomerId() != null && user.getCustomerId().equals(customerId);
    }
}