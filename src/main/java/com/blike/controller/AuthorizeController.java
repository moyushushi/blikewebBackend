package com.blike.controller;


import com.blike.entity.RestBean;
import com.blike.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/api")
@RestController
public class AuthorizeController {
    private final String USERNAME="^[a-zA-Z0-9\\u4e00-\\u9fa5]+$";

    @Resource
    AuthorizeService service;

    @PostMapping("/vali-register-email")
    public RestBean<String> validateRegisterEmail(@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                                              @RequestParam("email")  String email, HttpSession session) {
        String s= service.sendValidateEmail(email,session.getId(),false);
        if (s==null){
            return RestBean.success("邮件已发送");
        }else
            return RestBean.failure(400,s);
    }

    @PostMapping("/vali-reset-email")
    public RestBean<String> validateResetEmail(@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                                          @RequestParam("email")  String email, HttpSession session) {
        String s= service.sendValidateEmail(email,session.getId(),true);
        if (s==null){
            return RestBean.success("邮件已发送");
        }else
            return RestBean.failure(400,s);
    }

    @PostMapping("/register")
    public RestBean<String> registerUser(@Pattern(regexp =USERNAME)@Length(min=3,max=14) @RequestParam("username") String username,
                                         @Length(min=3,max=14) @RequestParam("password") String password,
                                         @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")@RequestParam("email") String email,
                                         @Length(min=6,max=6) @RequestParam("code")  String code,
                                         HttpSession session) {
        String s=service.validateAndRegister(username,password,email,code,session.getId());
        if(s==null){
            return RestBean.success("注册成功");
        }else{
            return RestBean.failure(400,s);
        }
    }
    @PostMapping("/start-reset")
    public RestBean<String>  startReset(@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")@RequestParam("email") String email,
                                        @Length(min=6,max=6) @RequestParam("code")  String code,
                                        HttpSession session){
        String s=service.validateOnly(email,code,session.getId());
        if (s==null){
            session.setAttribute("reset-password",email);
            return RestBean.success();
        }else{
            return RestBean.failure(400,s);
        }
    }

    @PostMapping("/do-password")
    public RestBean<String> resetPassword(@Length(min=3,max=14) @RequestParam("password") String password,
                                          @RequestParam("email") String email) {
        if (service.resetPassword(email, password)) {
            return RestBean.success("密码重置成功");
        } else {
            return RestBean.failure(500, "内部错误");
        }
    }



}

