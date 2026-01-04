package com.idea.filter;

import com.idea.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.io.IOException;
@Slf4j
//@WebFilter("/*")
public class TokenFIlter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/login"))
        {
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        String token = request.getHeader("token");
        if(token==null||token.isEmpty())
        {
            log.info("token为空");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return;
        }
        try{
            JwtUtils.parseJWT(token);
        }catch (Exception e){
            log.info("token解析失败");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return;
        }
        log.info("token验证成功");
        filterChain.doFilter(servletRequest,servletResponse);

    }
}
