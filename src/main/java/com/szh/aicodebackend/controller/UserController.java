package com.szh.aicodebackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.szh.aicodebackend.annotation.AuthCheck;
import com.szh.aicodebackend.common.BaseResponse;
import com.szh.aicodebackend.common.DeleteRequest;
import com.szh.aicodebackend.common.ResultUtils;
import com.szh.aicodebackend.constant.UserConstant;
import com.szh.aicodebackend.exception.ErrorCode;
import com.szh.aicodebackend.exception.ThrowUtils;
import com.szh.aicodebackend.model.dto.user.*;
import com.szh.aicodebackend.model.vo.LoginUserVO;
import com.szh.aicodebackend.model.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.szh.aicodebackend.model.entity.User;
import com.szh.aicodebackend.service.UserService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author Lenovo
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

   /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
   @PostMapping("/login")
   public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest , HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest , request);
        return ResultUtils.success(loginUserVO);
   }

   /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
   @GetMapping("/get/login")
   public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
       User loginUser = userService.getLoginUser(request);
       LoginUserVO loginUserVO = new LoginUserVO();
       BeanUtils.copyProperties(loginUser, loginUserVO);
       return ResultUtils.success(loginUserVO);
   }

   /**
     * 用户注销
     *
     * @param request
     * @return
     */
   @PostMapping("/logout")
   public BaseResponse<Boolean> logout(HttpServletRequest request) {
       boolean result = userService.userLogout(request);
       return ResultUtils.success(result);
   }

   /**
    * 创建用户（管理员）
    *
    * @param userAddRequest
    * @return
    */
   @PostMapping("/add")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
       ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
       User user = new User();
       BeanUtils.copyProperties(userAddRequest, user);
       //加密,默认密码：123456
       String encryptPassword = userService.getEncryptPassword("123456");
       user.setUserPassword(encryptPassword);
       boolean result = userService.save(user);
       ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"创建用户失败");
       return ResultUtils.success(user.getId());
   }

   /**
    * 根据ID查询用户（管理员）
    *
    * @param id
    * @return
    */
   @GetMapping("/get")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<User> getUserById(long id){
       ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR,"用户ID错误");
       User user = userService.getById(id);
       ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
       return ResultUtils.success(user);
   }

   /**
    * 根据ID查询用户包装类
    *
    * @param id
    * @return
    */
   @GetMapping("/get/vo")
   public BaseResponse<UserVO> getUserVOById(long id){
       BaseResponse<User> response = getUserById(id);
       return ResultUtils.success(userService.getUserVO(response.getData()));
   }

   /**
    * 删除用户（管理员）
    *
    * @param deleteRequest
    * @return
    */
   @PostMapping("/delete")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
       ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
       boolean result = userService.removeById(deleteRequest.getId());
       return ResultUtils.success(result);
   }

   /**
    * 更新用户（管理员）
    *
    * @param userUpdateRequest
    * @return
    */
   @PostMapping("/update")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
       ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
       User user = new User();
       BeanUtils.copyProperties(userUpdateRequest, user);
       boolean result = userService.updateById(user);
       ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"更新用户失败");
       return ResultUtils.success(true);
   }

   /**
    * 分页获取用户封装列表（管理员）
    *
    * @param userQueryRequest
    * @return
    */
   @GetMapping("/list/page/vo")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
       ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
       int pageNum = userQueryRequest.getPageNum();
       int pageSize = userQueryRequest.getPageSize();
       Page<User> userPage = userService.page(Page.of(pageNum, pageSize), userService.getQueryWrapper(userQueryRequest));
       //数据脱敏
       Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
       List<UserVO> userVoList = userService.getUserVoList(userPage.getRecords());
       userVOPage.setRecords(userVoList);
       return ResultUtils.success(userVOPage);
   }
}
