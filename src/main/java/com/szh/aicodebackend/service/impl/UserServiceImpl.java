package com.szh.aicodebackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szh.aicodebackend.exception.BusinessException;
import com.szh.aicodebackend.exception.ErrorCode;
import com.szh.aicodebackend.exception.ThrowUtils;
import com.szh.aicodebackend.model.dto.user.UserLoginRequest;
import com.szh.aicodebackend.model.dto.user.UserQueryRequest;
import com.szh.aicodebackend.model.dto.user.UserRegisterRequest;
import com.szh.aicodebackend.model.entity.User;
import com.szh.aicodebackend.mapper.UserMapper;
import com.szh.aicodebackend.model.enums.UserRoleEnum;
import com.szh.aicodebackend.model.vo.LoginUserVO;
import com.szh.aicodebackend.model.vo.UserVO;
import com.szh.aicodebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

import static com.szh.aicodebackend.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户 服务层实现。
 *
 * @author Lenovo
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {


    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        //参数校验
        ThrowUtils.throwIf(userAccount == null || password == null || checkPassword == null, ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户名过短");
        ThrowUtils.throwIf(password.length() < 6, ErrorCode.PARAMS_ERROR, "密码过短");
        ThrowUtils.throwIf(!password.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        //去重
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        //密码加密
        String encryptPassword = getEncryptPassword(password);
        //入库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户："+ userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean result = this.save(user);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest , HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        //参数校验
        ThrowUtils.throwIf(userAccount == null, ErrorCode.PARAMS_ERROR, "用户名不能为空");
        ThrowUtils.throwIf(password == null, ErrorCode.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户名错误");
        ThrowUtils.throwIf(password.length() < 6, ErrorCode.PARAMS_ERROR, "密码错误");
        //密码加密
        String encryptPassword = getEncryptPassword(password);
        //查询数据库
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        //如果不存在
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //保存用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否登录
        User user = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVoList(List<User> userList) {
        if (userList == null){
            return null;
        }
        return userList.stream().map(this::getUserVO).toList();
    }


    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "szh";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

}
