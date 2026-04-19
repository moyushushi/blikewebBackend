package com.blike.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.BufferedReader;

/**
 * JSON 登录过滤器，支持 application/json 格式的登录请求
 * 请求体示例：{"username":"xxx","password":"xxx","remember":true}
 */
public class JsonLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonLoginFilter() {
        super(new AntPathRequestMatcher("/api/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            // 读取 JSON 请求体
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JsonNode jsonNode = objectMapper.readTree(sb.toString());

            String username = jsonNode.has("username") ? jsonNode.get("username").asText() : null;
            String password = jsonNode.has("password") ? jsonNode.get("password").asText() : null;

            if (username == null || password == null) {
                throw new IllegalArgumentException("用户名或密码不能为空");
            }

            // 记住我参数（可选）
            boolean remember = jsonNode.has("remember") && jsonNode.get("remember").asBoolean();
            request.setAttribute("remember", remember);

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            throw new RuntimeException("解析登录请求失败", e);
        }
    }
}