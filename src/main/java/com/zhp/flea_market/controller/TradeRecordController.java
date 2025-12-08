package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.model.entity.TradeRecord;
import com.zhp.flea_market.model.vo.TradeRecordVO;
import com.zhp.flea_market.service.TradeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 交易记录接口
 */
@RestController
@RequestMapping("/trade-record")
@Slf4j
@Tag(name = "交易记录管理", description = "交易记录的查询等接口")
public class TradeRecordController extends BaseController {

    @Resource
    private TradeRecordService tradeRecordService;

    /**
     * 获取交易记录详情
     *
     * @param id 交易记录ID
     * @param request HTTP请求
     * @return 交易记录详情
     */
    @Operation(summary = "获取交易记录详情", description = "根据交易记录ID获取详细信息")
    @GetMapping("/get/{id}")
    @LoginRequired
    public BaseResponse<TradeRecordVO> getTradeRecordDetail(
            @Parameter(description = "交易记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "交易记录ID");

        // 获取交易记录详情
        TradeRecordVO tradeRecord = tradeRecordService.getTradeRecordDetail(id, request);
        
        logOperation("获取交易记录详情", request, "交易记录ID", id);
        return ResultUtils.success(tradeRecord);
    }

    /**
     * 获取买家的交易记录列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页交易记录列表
     */
    @Operation(summary = "获取买家的交易记录列表", description = "获取当前登录用户的买家交易记录列表")
    @GetMapping("/list/buyer")
    @LoginRequired
    public BaseResponse<Page<TradeRecordVO>> listBuyerTradeRecords(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<TradeRecord> page = validatePageParams(current, size);

        // 执行分页查询
        Page<TradeRecordVO> tradeRecordVOPage = tradeRecordService.getBuyerTradeRecords(request, page);
        
        logOperation("获取买家交易记录列表", request, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(tradeRecordVOPage);
    }

    /**
     * 获取卖家的交易记录列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页交易记录VO列表
     */
    @Operation(summary = "获取卖家的交易记录列表", description = "获取当前登录用户的卖家交易记录列表")
    @GetMapping("/list/seller")
    @LoginRequired
    public BaseResponse<Page<TradeRecordVO>> listSellerTradeRecords(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<TradeRecord> page = validatePageParams(current, size);

        // 执行分页查询
        Page<TradeRecordVO> tradeRecordVOPage = tradeRecordService.getSellerTradeRecords(request, page);
        
        logOperation("获取卖家交易记录列表", request, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(tradeRecordVOPage);
    }

    /**
     * 获取所有交易记录（管理员权限）
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param tradeStatus 交易状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 分页交易记录VO列表
     */
    @Operation(summary = "获取所有交易记录", description = "管理员获取所有交易记录列表")
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<TradeRecordVO>> adminListTradeRecords(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "交易状态") @RequestParam(required = false) Integer tradeStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        // 参数校验
        Page<TradeRecord> page = validatePageParams(current, size);

        // 执行分页查询
        Page<TradeRecordVO> tradeRecordVOPage = tradeRecordService.getAllTradeRecords(page, tradeStatus, startDate, endDate, request);
        
        logOperation("管理员获取所有交易记录列表", request, 
                "当前页", current,
                "每页大小", size,
                "交易状态", tradeStatus,
                "开始日期", startDate,
                "结束日期", endDate
        );
        return ResultUtils.success(tradeRecordVOPage);
    }
}