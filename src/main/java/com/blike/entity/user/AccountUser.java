package com.blike.entity.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class AccountUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String email;
    private String username;
    private String avatar;// 新增
    private String password;
    private String bio;
}