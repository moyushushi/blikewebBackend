package com.blike.service;

import com.blike.entity.user.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthorizeService extends UserDetailsService {
    String sendValidateEmail(String email, String sessionId, boolean hasAccount);
    String validateAndRegister(String username, String password, String email, String code, String sessionId);
    String validateOnly(String email, String code, String sessionId);
    boolean resetPassword(String email, String password);
}