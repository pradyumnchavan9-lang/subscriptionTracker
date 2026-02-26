package com.subtracker.SubTracker.ratelimiter;


import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserRateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitService rateLimitService;


    private String getUserEmailFromJwt(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        return email.split("@")[0];
    }



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String userId = getUserEmailFromJwt(request);

        if(userId != null){

            Bucket bucket = rateLimitService.getUserBucket(userId);

            if(bucket.tryConsume(1)){
                filterChain.doFilter(request,response);
            }else{
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate Limit Exceeded");
                return;
            }

        }else{
            filterChain.doFilter(request,response);
        }

    }

}
