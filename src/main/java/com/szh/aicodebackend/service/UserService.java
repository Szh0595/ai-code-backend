package com.szh.aicodebackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.szh.aicodebackend.model.dto.user.UserLoginRequest;
import com.szh.aicodebackend.model.dto.user.UserQueryRequest;
import com.szh.aicodebackend.model.dto.user.UserRegisterRequest;
import com.szh.aicodebackend.model.entity.User;
import com.szh.aicodebackend.model.vo.LoginUserVO;
import com.szh.aicodebackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author Lenovo
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册参数
     * @return 注册用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录参数
     * @return 登录用户
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest , HttpServletRequest  request);

    /**
     * 用户注销（退出登录）
     *
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest  request);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 查询参数
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取用户封装类
     *
     * @param user 用户
     * @return 用户封装类
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户列表的封装类
     *
     * @param userList 用户列表
     * @return 用户列表的封装类
     */
    List<UserVO> getUserVoList(List<User> userList);

    /**
     * 获取加密密码
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);
}
