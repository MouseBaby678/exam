package com.baymax.exam.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baymax.exam.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;


@Getter
@Setter
@TableName("es_user")
@Schema(name = "User", description = "用户信息")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @Size(min = 5,max = 30,message = "用户名不符合规则",groups = {LoginRequestValid.class,RegisterRequestValid.class,UpdateUserRequestValid.class})
    @NotBlank(message = "用户名不能为空",groups = {LoginRequestValid.class,RegisterRequestValid.class})
    @Schema(description = "用户名")
    private String username;

    @Length(min = 5,max = 25,message = "昵称应在5~25字符",groups = {RegisterRequestValid.class})
    @Schema(description = "别名")
    private String nickname;

    @NotBlank(message = "密码不能为空",groups = {LoginRequestValid.class,RegisterRequestValid.class,ForgetPassRequestValid.class})
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,18}$",message = "密码不符合规则",groups = {LoginRequestValid.class,RegisterRequestValid.class,ForgetPassRequestValid.class})
    @Schema(description = "密码")
    private String password;

    @URL(message = "头像图像地址不正确",groups = {UpdateUserRequestValid.class})
    @Schema(description = "头像")
    private String picture;

    @URL(message = "背景图像地址不正确",groups = {UpdateUserRequestValid.class})
    @Schema(description = "背景图像")
    private String bgPicture;

    @Length(min = 0,max = 50,message = "签名长度不能超过50字符",groups = {UpdateUserRequestValid.class})
    @Schema(description = "签名")

    private String autograph;
    @Pattern(regexp = "^(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$",message = "手机格式不正确",groups = {UpdateUserRequestValid.class})
    @Schema(description = "手机号")
    private String phone;

    @Email(message = "邮箱地址不正确",groups = {RegisterRequestValid.class,ForgetPassRequestValid.class,UpdateUserRequestValid.class})
    @NotBlank(message = "邮箱地址不能为空",groups = {RegisterRequestValid.class,ForgetPassRequestValid.class})
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "认证Id")
    private Integer authId;

    @Schema(description = "0：学生 1：教师")
    private String role;

    @Schema(description = "状态，0：启用，>0:封禁")
    private Boolean enable;

    /**
     * 编码密码
     *
     * @param password 密码
     */
    public void setEncodePassword(String password) {
        BCryptPasswordEncoder bcr=new BCryptPasswordEncoder();
        this.password =bcr.encode(password);
    }
    @JsonIgnore
    public User getBaseInfo(){
        User user = new User();
        user.setId(id);
        user.setPicture(picture);
        user.setUsername(username);
        user.setNickname(nickname);
        return user;
    }

    //    登录请求
    public interface LoginRequestValid{ }
    //    注册请求
    public interface RegisterRequestValid{}
    //    找回密码
    public interface ForgetPassRequestValid{}
    //     更新用户信息
    public interface UpdateUserRequestValid{}
}

