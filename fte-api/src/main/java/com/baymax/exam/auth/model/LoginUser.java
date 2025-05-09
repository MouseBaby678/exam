package com.baymax.exam.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：登录用户信息
 * @modified By：
 * @version:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
        private Integer id;
        private String username;
        private String password;
        private Boolean enabled;
        private String clientId;
        private List<String> roles;
}
