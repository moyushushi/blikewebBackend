package com.blike.config;


import com.blike.entity.RestBean;
import com.blike.entity.user.Account;
import com.blike.entity.user.AccountUser;
import com.blike.filter.JsonLoginFilter;
import com.blike.filter.JwtAuthenticationFilter;
import com.blike.service.AuthorizeService;
import com.blike.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Resource
    private AuthorizeService authorizeService;

    @Resource
    private DataSource dataSource;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        tokenRepository.setCreateTableOnStartup(false);
        return tokenRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager) throws Exception {
        // 1. 创建 JSON 登录过滤器（登录成功时返回 JWT，不再依赖 Session）
        JsonLoginFilter jsonLoginFilter = createJsonLoginFilter(authenticationManager);

        // 2. 创建 JWT 认证过滤器（从请求头解析 Token 并设置 SecurityContext）
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter();

        http
                .csrf(AbstractHttpConfigurer::disable)
                // 3. 禁用 Session 管理（改为无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口（无需 Token）
                        .requestMatchers("/api/login", "/api/register",
                                "/api/vali-register-email", "/api/vali-reset-email",
                                "/api/start-reset", "/api/do-password",
                                "/api/video/list", "/api/video/*",
                                "/upload/**","api/article").permitAll()
                        // 需要认证的接口
                        .requestMatchers("/api/video/upload", "/api/video/comment", "/api/user/me",
                                "/api/user/follow/**",      // 关注/取消关注/检查关注
                                "/api/user/like/**",        // 点赞/取消点赞/检查点赞
                                "/api/user/following",      // 获取关注列表
                                "/api/user/liked-videos",
                                "/api/user/is-liked").authenticated()
                        .anyRequest().authenticated()
                )
                .userDetailsService(authorizeService)
                // 4. 移除 .rememberMe()（JWT 自带有效期）
                // 5. 添加自定义过滤器
                .addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private JsonLoginFilter createJsonLoginFilter(AuthenticationManager authenticationManager) {
        JsonLoginFilter filter = new JsonLoginFilter();
        filter.setFilterProcessesUrl("/api/login");
        filter.setAuthenticationManager(authenticationManager);

        // 登录成功处理器：返回 JWT Token
        filter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Account account = userDetails.getAccount();
            // 生成 JWT Token（有效期7天）
            String token = JwtUtils.generateToken(account.getId(), account.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", account);
            String json = new ObjectMapper().writeValueAsString(RestBean.success(result));
            response.getWriter().write(json);
        });

        // 登录失败处理器
        filter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            String json = new ObjectMapper().writeValueAsString(RestBean.failure(401, "用户名或密码错误"));
            response.getWriter().write(json);
        });

        return filter;
    }
}