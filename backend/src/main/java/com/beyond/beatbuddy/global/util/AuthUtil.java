package com.beyond.beatbuddy.global.util;

import com.beyond.beatbuddy.global.entity.User;
import com.beyond.beatbuddy.global.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    public static Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return userPrincipal.getUserId();
    }

    public static String getCurrentUserEmail() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return userPrincipal.getEmail();
    }
}