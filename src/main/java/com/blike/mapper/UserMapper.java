package com.blike.mapper;

import com.blike.entity.user.Account;
import com.blike.entity.user.AccountUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("select * from account where username = #{text} or email = #{text}")
    Account findAccountByNameOrEmail(@Param("text") String text);

    @Insert("insert into account(email, username, password) values (#{email}, #{username}, #{password})")
    int createAccount(@Param("username") String username,
                      @Param("password") String password,
                      @Param("email") String email);

    @Update("update account set password = #{password} where email = #{email}")
    int setPasswordByEmail(@Param("password") String password,
                           @Param("email") String email);

    @Update("UPDATE account SET avatar = #{avatar} WHERE id = #{id}")
    int updateAvatar(@Param("id") Integer id, @Param("avatar") String avatar);

    // 查询用户信息（包含密码和头像）
    @Select("SELECT id, email, username, password, avatar FROM account WHERE id = #{id}")
    Account findAccountById(Integer id);

    @Update("UPDATE account SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    // 用于内部认证、修改密码等需要密码的场景


    // 如果需要保留不含密码的查询（用于前端展示），可以单独定义
    // 修改 findAccountUserById 查询，包含 bio 字段
    @Select("SELECT id, email, username, avatar, bio FROM account WHERE id = #{id}")
    AccountUser findAccountUserById(Integer id);

    @Update("UPDATE account SET bio = #{bio} WHERE id = #{id}")
    int updateBio(@Param("id") Integer id, @Param("bio") String bio);
}