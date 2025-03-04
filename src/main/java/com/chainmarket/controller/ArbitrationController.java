package com.chainmarket.controller;

import com.chainmarket.entity.Arbitration;
import com.chainmarket.entity.ArbitrationEvidence;
import com.chainmarket.entity.User;
import com.chainmarket.service.IArbitrationService;
import com.chainmarket.service.ISystemParamService;
import com.chainmarket.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import com.chainmarket.exception.BusinessException;
import java.util.List;

@Controller
@RequestMapping("/arbitration")
public class ArbitrationController {
    
    @Autowired
    private IArbitrationService arbitrationService;
    
    @Autowired
    private ISystemParamService systemParamService;
    
    @GetMapping
    public String index(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        
        // 获取仲裁员数量配置
        int arbitratorCount = systemParamService.getArbitratorCount();
        // 计算所需通过票数（N/2 + 1）
        int requiredVotes = (arbitratorCount / 2) + 1;
        
        model.addAttribute("arbitratorCount", arbitratorCount);
        model.addAttribute("requiredVotes", requiredVotes);
        model.addAttribute("arbitrationCases", arbitrationService.getPendingArbitrations());
        model.addAttribute("userOrders", arbitrationService.getUserOrders(user.getUserId()));
        
        return "arbitration/index";
    }

    @PostMapping("/apply")
    @ResponseBody
    public Result<Void> apply(
            @RequestParam Long orderId,
            @RequestParam String disputeDesc,
            @RequestParam MultipartFile evidenceFile,
            @RequestParam String evidenceDesc,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            arbitrationService.applyArbitration(orderId, user.getUserId(), disputeDesc, 
                                              evidenceFile, evidenceDesc);
            return Result.success("申请成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{caseId}")
    public String detail(@PathVariable Long caseId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        
        // 获取仲裁案件信息
        Arbitration arbitration = arbitrationService.getArbitrationById(caseId);
        if (arbitration == null) {
            throw new BusinessException("仲裁案件不存在");
        }
        
        // 获取案件证据列表
        List<ArbitrationEvidence> evidences = arbitrationService.getArbitrationEvidences(caseId);
        
        model.addAttribute("arbitration", arbitration);
        model.addAttribute("evidences", evidences);
        return "arbitration/detail";
    }

    @PostMapping("/join/{caseId}")
    @ResponseBody
    public Result<Void> join(@PathVariable Long caseId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            arbitrationService.joinArbitration(caseId, user.getUserId());
            return Result.success("加入成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 