package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.ProductCommentMapper;
import com.zhp.flea_market.model.dto.request.ProductCommentAddRequest;
import com.zhp.flea_market.model.dto.request.ProductCommentQueryRequest;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.ProductComment;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.ProductCommentVO;
import com.zhp.flea_market.service.ProductCommentService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 二手物品留言服务实现
 */
@Service
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment>
        implements ProductCommentService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * 添加留言
     *
     * @param commentAddRequest 留言信息
     * @param request HTTP请求
     * @return 添加成功的留言ID
     */
    @Override
    public Long addComment(ProductCommentAddRequest commentAddRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 检查二手物品是否存在
        Product product = productService.getById(commentAddRequest.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品不存在");
        }

        // 如果是回复留言，检查原留言是否存在
        if (commentAddRequest.getParentId() != 0) {
            ProductComment parentComment = this.getById(commentAddRequest.getParentId());
            if (parentComment == null || !parentComment.getProductId().equals(commentAddRequest.getProductId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "父留言不存在或不属于该二手物品");
            }
        }

        // 如果是回复特定用户，检查用户是否存在
        if (commentAddRequest.getReplyUserId() != 0) {
            User replyUser = userService.getById(commentAddRequest.getReplyUserId());
            if (replyUser == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "被回复用户不存在");
            }
        }

        // 创建留言对象
        ProductComment comment = new ProductComment();
        BeanUtils.copyProperties(commentAddRequest, comment);
        comment.setUserId(loginUser.getId());

        // 保存留言
        boolean result = this.save(comment);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "留言添加失败");
        }

        return comment.getId();
    }

    /**
     * 分页获取留言列表
     *
     * @param queryRequest 查询条件
     * @return 分页留言列表
     */
    @Override
    public Page<ProductCommentVO> getCommentsByPage(ProductCommentQueryRequest queryRequest) {
        // 创建分页对象
        Page<ProductCommentVO> page = new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize());
        
        // 如果指定了二手物品ID，只获取该二手物品的留言
        if (queryRequest.getProductId() != null) {
            // 获取一级留言
            return baseMapper.selectCommentsWithReplies(page, queryRequest.getProductId(), 0L);
        }
        
        // 否则返回空结果
        return new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize(), 0);
    }

    /**
     * 获取留言的回复列表
     *
     * @param parentId 父留言ID
     * @return 回复列表
     */
    @Override
    public List<ProductCommentVO> getReplies(Long parentId) {
        QueryWrapper<ProductComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        queryWrapper.orderByAsc("create_time");
        
        List<ProductComment> comments = this.list(queryWrapper);
        
        // 转换为VO
        List<ProductCommentVO> result = new ArrayList<>();
        for (ProductComment comment : comments) {
            ProductCommentVO vo = convertToVO(comment);
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 删除留言
     *
     * @param id 留言ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteComment(Long id, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 获取留言信息
        ProductComment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "留言不存在");
        }

        // 检查权限：只有留言发布者或管理员可以删除
        boolean isAdmin = userService.isAdmin(loginUser);
        boolean isOwner = Objects.equals(comment.getUserId(), loginUser.getId());
        
        if (!isAdmin && !isOwner) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该留言");
        }

        // 删除留言（逻辑删除）
        return this.removeById(id);
    }

    /**
     * 构建留言树形结构
     *
     * @param productId 二手物品ID
     * @return 树形结构的留言列表
     */
    @Override
    public List<ProductCommentVO> getCommentTree(Long productId) {
        // 获取所有留言（包括回复）
        QueryWrapper<ProductComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.orderByAsc("create_time");
        
        List<ProductComment> allComments = this.list(queryWrapper);
        
        // 转换为VO
        List<ProductCommentVO> allVOs = allComments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建树形结构
        Map<Long, List<ProductCommentVO>> childrenMap = allVOs.stream()
                .filter(vo -> !vo.getParentId().equals(0L))
                .collect(Collectors.groupingBy(ProductCommentVO::getParentId));
        
        // 设置子留言
        for (ProductCommentVO vo : allVOs) {
            if (childrenMap.containsKey(vo.getId())) {
                vo.setChildren(childrenMap.get(vo.getId()));
            }
        }
        
        // 返回一级留言
        return allVOs.stream()
                .filter(vo -> vo.getParentId().equals(0L))
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO对象
     */
    private ProductCommentVO convertToVO(ProductComment comment) {
        ProductCommentVO vo = new ProductCommentVO();
        BeanUtils.copyProperties(comment, vo);
        
        // 设置用户信息
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setUserAvatar(user.getUserAvatar());
        }
        
        // 设置被回复用户信息
        if (comment.getReplyUserId() != null && comment.getReplyUserId() > 0) {
            User replyUser = userService.getById(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUserName(replyUser.getUserName());
            }
        }
        
        return vo;
    }
}