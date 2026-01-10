package com.idea.interceptor;

import com.idea.utils.CurrentHolder;
import com.idea.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
@Slf4j
@Component

public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("===== TokenInterceptor 进来了，请求路径：{}", request.getRequestURI());
        String token = request.getHeader("token");
        if(token==null||token.isEmpty())
        {
            log.info("token为空");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        try{
            Claims claims=JwtUtils.parseJWT(token);
            Integer id= (Integer)(claims.get("id"));
            CurrentHolder.setCurrentId(id);
            log.info("当前用户id为：{}",id);
        }catch (Exception e){
            log.info("token解析失败");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        log.info("token验证成功");
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CurrentHolder.remove();
    }
}
