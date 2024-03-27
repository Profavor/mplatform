package com.favorsoft.mplatform.config.security;

import com.favorsoft.mplatform.cdn.domain.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("TEST");
        }else if(authentication == null){
            return Optional.of("TEST");
        }
        User user = (User) authentication.getPrincipal();
        return Optional.ofNullable(user.getUserId());
    }
}
