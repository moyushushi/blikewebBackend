package com.blike.service.impl;

import com.blike.config.CustomUserDetails;
import com.blike.entity.user.Account;
import com.blike.mapper.UserMapper;
import com.blike.service.AuthorizeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthorizeServiceImpl implements AuthorizeService {

    @Value("${spring.mail.username}")
    String from;

    @Resource
    UserMapper mapper;

    @Resource
    MailSender mailSender;

    @Resource
    StringRedisTemplate template;


    BCryptPasswordEncoder  encoder=new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = mapper.findAccountByNameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return new CustomUserDetails(account, account.getPassword());
    }
    /*
              1.生成验证码
              2.放redis里 (3分钟过期,60s可发送一次)
              3.发送到指定邮箱
              4.失败，redis中删除
              5.用户注册是从redis中取出，验证
             */
    @Override
    public String sendValidateEmail(String email,String sessionId,boolean hasAccount) {
        String key = "email:verify:" +email+":"+hasAccount;
        System.out.println("【sendValidateEmail】key = " + key);
        if (template.hasKey(key)){
            Long expire= Optional.of(template.getExpire(key,TimeUnit.SECONDS)).orElse(0L);
            if (expire>120)
                return "请求频繁，请稍后再试";
        }
        Account account = mapper.findAccountByNameOrEmail(email);
        if (account == null && hasAccount)return "没有此邮件地址的账户";
        if (account != null&& !hasAccount) return "此邮箱已被其他用户注册";
        Random random = new Random();
        int code= random.nextInt(899999)+100000;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("邮箱验证");
        message.setText("验证码是："+code);
        try {
            mailSender.send(message);
            template.opsForValue().set(key,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }catch (MailException e){
            e.printStackTrace();
            return "邮件发送失败，请检查邮箱地址是否正确或联系管理员";
        }
    }

    @Override
    public String validateAndRegister(String username, String password, String email, String code, String sessionId) {
        String key = "email:verify:" + email+":false";
        if(template.hasKey(key)){
            String s= template.opsForValue().get(key);
            if (s==null) return "验证码失效";
            if (code != null && code.equals(s)){
                Account account=mapper.findAccountByNameOrEmail(username);
                password = encoder.encode(password);
                template.delete(key);
                if (account != null)return "此用户名已被注册!";
                if(mapper.createAccount(username,password,email)>0){
                    return null;
                }else return "内部错误，联系管理员";

            }else return "验证码错误";
        }else {
            return "请先完成邮箱验证！";
        }
    }

    @Override
    public String validateOnly(String email, String code, String sessionId) {
        String key = "email:verify:" + email+":true";
        if(template.hasKey(key)) {
            String s = template.opsForValue().get(key);
            if (s == null) return "验证码失效";
            if (code != null && code.equals(s)) {
                template.delete(key);
                return null;
            } else return "验证码错误";
        }else {
            return "请先完成邮箱验证！";
        }
    }

    @Override
    public boolean resetPassword(String email ,String password) {
        password = encoder.encode(password);
        return mapper.setPasswordByEmail(password,email)>0;
    }
}
