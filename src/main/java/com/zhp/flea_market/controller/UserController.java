package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.JwtKit;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.model.dto.request.*;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.UserService;
import com.zhp.flea_market.model.vo.LoginUserVO;
import com.zhp.flea_market.model.vo.UserVO;
import com.zhp.flea_market.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static com.zhp.flea_market.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@Slf4j
public class UserController extends BaseController {

    @Autowired
    private JwtKit jwtKit;

    @Resource
    private UserService userService;
    
    @Autowired
    private ImageStorageService imageStorageService;
    
    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册信息
     * @return 注册成功的用户ID
     */
    @Operation(summary = "用户注册", description = "新用户注册账号")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(
            @Parameter(description = "用户注册信息") @RequestBody UserRegisterRequest userRegisterRequest) {
        // 参数校验
        validateNotNull(userRegisterRequest, "注册信息");
        validateNotBlank(userRegisterRequest.getUserAccount(), "账号");
        validateNotBlank(userRegisterRequest.getUserPassword(), "密码");
        validateNotBlank(userRegisterRequest.getUserName(), "用户名");
        validateNotBlank(userRegisterRequest.getUserPhone(), "手机号");

        // 注册用户
        long result = userService.register(
                userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getUserName(),
                userRegisterRequest.getUserPhone()
        );

        logOperation("用户注册", null, "账号", userRegisterRequest.getUserAccount());
        BaseResponse<Long> response = ResultUtils.success(result);
        response.setMessage("注册成功");
        return response;
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录信息
     * @param request HTTP请求
     * @return 登录用户信息和令牌
     */
    @Operation(summary = "用户登录", description = "用户账号密码登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(
            @Parameter(description = "用户登录信息") @RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        // 参数校验
        validateNotNull(userLoginRequest, "登录信息");
        validateNotBlank(userLoginRequest.getUserAccount(), "账号");
        validateNotBlank(userLoginRequest.getUserPassword(), "密码");

        // 用户登录
        LoginUserVO user = userService.login(
                userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword(),
                request
        );

        // 生成令牌
        LoginUserVO userVO = new LoginUserVO();
        BeanUtils.copyProperties(user, userVO);
        String token = jwtKit.generateToken(user);
        HashMap<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token", token);

        // 设置Cookie，实现自动登录状态保持
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("token", token);
        cookie.setHttpOnly(true); // 防止XSS攻击
        cookie.setPath("/"); // 在整个应用范围内有效
        cookie.setMaxAge(24 * 60 * 60); // 24小时过期
        response.addCookie(cookie);

        logOperation("用户登录", request, "账号", userLoginRequest.getUserAccount());
        return ResultUtils.successDynamic(userVO, tokenMap);
    }

    /**
     * 用户注销
     *
     * @param request HTTP请求
     * @return 注销结果
     */
    @Operation(summary = "用户注销", description = "用户退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        validateNotNull(request, "HTTP请求");
        boolean result = userService.userLogout(request);
        
        // 清除Cookie
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 立即过期
        response.addCookie(cookie);
        
        logOperation("用户注销", request);
        return handleOperationResult(result, "注销成功");
    }

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求
     * @return 当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户", description = "获取当前登录用户的详细信息")
    @GetMapping("/get/login")
    @LoginRequired
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        validateNotNull(request, "HTTP请求");
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        logOperation("获取当前登录用户", request, "用户ID", user.getId());
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 创建用户
     *
     * @param userAddRequest 用户创建信息
     * @param request HTTP请求
     * @return 创建成功的用户ID
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(
            @Parameter(description = "用户创建信息") @RequestBody UserAddRequest userAddRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(userAddRequest, "用户创建信息");

        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        
        // 设置默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);

        // 保存用户
        boolean result = userService.save(user);
        
        logOperation("创建用户", result, request, "用户名", userAddRequest.getUserName());
        return handleOperationResult(result, "用户创建成功", user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 删除结果
     */
    @Operation(summary = "删除用户", description = "管理员删除用户")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(
            @Parameter(description = "删除请求") @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(deleteRequest, "删除请求");
        validateId(deleteRequest.getId(), "用户ID");

        // 检查用户是否存在
        User user = userService.getById(deleteRequest.getId());
        validateResourceExists(user, "用户");

        // 删除用户
        boolean result = userService.removeById(deleteRequest.getId());
        
        // 如果删除成功且用户有头像，删除相关头像图片
        if (result && user.getUserAvatar() != null) {
            try {
                imageStorageService.deleteImage(user.getUserAvatar());
            } catch (Exception e) {
                // 删除图片失败不应该影响删除操作
                System.err.println("删除用户头像失败: " + e.getMessage());
            }
        }
        
        logOperation("删除用户", result, request, "用户ID", deleteRequest.getId());
        return handleOperationResult(result, "用户删除成功");
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest 用户更新信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(
            @Parameter(description = "用户更新信息") @RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(userUpdateRequest, "用户更新信息");
        validateId(userUpdateRequest.getId(), "用户ID");

        // 检查用户是否存在
        validateResourceExists(userService.getById(userUpdateRequest.getId()), "用户");

        // 创建更新对象并执行更新
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        
        logOperation("更新用户", result, request, "用户ID", userUpdateRequest.getId());
        return handleOperationResult(result, "用户更新成功");
    }

    /**
     * 根据ID获取用户（仅管理员）
     *
     * @param id 用户ID
     * @param request HTTP请求
     * @return 用户信息
     */
    @Operation(summary = "根据ID获取用户", description = "管理员根据用户ID获取用户详细信息")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(
            @Parameter(description = "用户ID") long id,
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "用户ID");

        // 获取用户信息
        User user = userService.getById(id);
        validateResourceExists(user, "用户");

        logOperation("获取用户详情", request, "用户ID", id);
        return ResultUtils.success(user);
    }

    /**
     * 根据ID获取用户视图
     *
     * @param id 用户ID
     * @param request HTTP请求
     * @return 用户视图信息
     */
    @Operation(summary = "根据ID获取用户视图", description = "根据用户ID获取用户视图信息")
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(
            @Parameter(description = "用户ID") long id,
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "用户ID");

        // 获取用户信息
        User user = userService.getById(id);
        validateResourceExists(user, "用户");

        logOperation("获取用户视图", request, "用户ID", id);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest 用户查询条件
     * @param request HTTP请求
     * @return 分页用户列表
     */
    @Operation(summary = "分页获取用户列表", description = "管理员分页获取用户列表")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(
            @Parameter(description = "用户查询条件") @RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(userQueryRequest, "用户查询条件");
        Page<User> page = validatePageParams(
                userQueryRequest.getCurrent(),
                userQueryRequest.getPageSize()
        );

        // 执行分页查询
        Page<User> userPage = userService.page(page, userService.getQueryWrapper(userQueryRequest));

        logOperation("分页获取用户列表", request, 
                "当前页", userQueryRequest.getCurrent(),
                "每页大小", userQueryRequest.getPageSize()
        );
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param id 用户ID
     * @param userName 用户名
     * @param userRole 用户角色
     * @param point 用户积分
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @param request HTTP请求
     * @return 分页用户视图列表
     */
    @Operation(summary = "分页获取用户视图列表", description = "分页获取用户视图信息列表")
    @GetMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long id,
            @Parameter(description = "用户名") @RequestParam(required = false) String userName,
            @Parameter(description = "用户角色") @RequestParam(required = false) String userRole,
            @Parameter(description = "用户积分") @RequestParam(required = false) BigDecimal point,
            @Parameter(description = "排序字段") @RequestParam(required = false) String sortField,
            @Parameter(description = "排序顺序") @RequestParam(defaultValue = "desc") String sortOrder,
            HttpServletRequest request) {
        // 参数校验（限制爬虫）
        Page<User> page = validatePageParams(current, size, 20);
        
        // 创建查询请求对象
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setCurrent(current);
        userQueryRequest.setPageSize(size);
        userQueryRequest.setId(id);
        userQueryRequest.setUserName(userName);
        userQueryRequest.setUserRole(userRole);
        userQueryRequest.setPoint(point);
        userQueryRequest.setSortField(sortField);
        userQueryRequest.setSortOrder(sortOrder);
        
        // 执行分页查询
        Page<User> userPage = userService.page(page, userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        
        logOperation("分页获取用户视图列表", request, 
                "当前页", current,
                "每页大小", size,
                "查询条件", userQueryRequest
        );
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 个人信息更新请求
     * @param request HTTP请求
     * @return 更新结果
     */
    @Operation(summary = "更新个人信息", description = "用户更新自己的个人信息")
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(
            @Parameter(description = "个人信息更新请求") @RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(userUpdateMyRequest, "个人信息更新请求");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 创建更新对象
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        
        // 执行更新
        boolean result = userService.updateById(user);
        
        logOperation("更新个人信息", result, request, "用户ID", loginUser.getId());
        return handleOperationResult(result, "个人信息更新成功");
    }

    /**
     * 审核用户
     *
     * @param userAuditRequest 用户审核请求
     * @param request HTTP请求
     * @return 是否审核成功
     */
    @Operation(summary = "审核用户", description = "管理员审核用户申请")
    @PostMapping("/admin/audit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> auditUser(
            @Parameter(description = "用户审核请求") @RequestBody UserAuditRequest userAuditRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(userAuditRequest, "用户审核请求");
        validateId(userAuditRequest.getUserId(), "用户ID");
        validateNotNull(userAuditRequest.getAuditStatus(), "审核状态");

        // 审核用户
        boolean result = userService.auditUser(
                userAuditRequest.getUserId(), 
                userAuditRequest.getAuditStatus(), 
                request
        );
        
        String statusDesc = userAuditRequest.getAuditStatus() == 1 ? "通过" : "拒绝";
        logOperation("审核用户", result, request, 
                "用户ID", userAuditRequest.getUserId(),
                "审核状态", statusDesc
        );
        return handleOperationResult(result, "用户审核成功");
    }

    /**
     * 获取待审核用户列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 待审核用户列表
     */
    @Operation(summary = "获取待审核用户列表", description = "管理员获取待审核用户列表")
    @GetMapping("/admin/pending")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> getPendingAuditUsers(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<User> page = validatePageParams(current, size);

        // 获取待审核用户列表
        List<User> users = userService.getPendingAuditUsers(page);
        
        logOperation("获取待审核用户列表", request, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 获取已拒绝用户数量
     *
     * @param request HTTP请求
     * @return 已拒绝用户数量
     */
    @Operation(summary = "获取已拒绝用户数量", description = "管理员获取已拒绝用户数量")
    @GetMapping("/admin/rejected/count")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> getRejectedUserCount(HttpServletRequest request) {
        // 获取已拒绝用户数量
        int count = userService.getRejectedUserCount();
        
        logOperation("获取已拒绝用户数量", request, "数量", count);
        return ResultUtils.success(count);
    }

    /**
     * 批量删除所有已拒绝用户
     *
     * @param userBatchDeleteRequest 批量删除请求
     * @param request HTTP请求
     * @return 删除结果
     */
    @Operation(summary = "批量删除已拒绝用户", description = "管理员一键删除所有已拒绝的用户账号")
    @PostMapping("/admin/rejected/delete-all")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> batchDeleteRejectedUsers(
            @Parameter(description = "批量删除请求") @RequestBody UserBatchDeleteRequest userBatchDeleteRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(userBatchDeleteRequest, "批量删除请求");
        
        // 确认检查：必须明确确认才能执行批量删除
        if (!userBatchDeleteRequest.isConfirm()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先确认批量删除操作");
        }
        
        // 批量删除所有已拒绝用户
        int deletedCount = userService.batchDeleteRejectedUsers(request);
        
        String message = deletedCount > 0 ? 
            "成功删除了 " + deletedCount + " 个已拒绝用户" : "没有已拒绝用户需要删除";
        
        logOperation("批量删除已拒绝用户", request, 
                "删除数量", deletedCount,
                "备注", userBatchDeleteRequest.getRemark()
        );
        return ResultUtils.success(message);
    }
}
