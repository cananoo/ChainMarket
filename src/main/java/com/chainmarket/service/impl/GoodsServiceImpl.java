package com.chainmarket.service.impl;

import com.chainmarket.dao.GoodsDao;
import com.chainmarket.dao.GoodsCategoryDao;
import com.chainmarket.dao.GoodsImageDao;
import com.chainmarket.dao.AuditInfoDao;
import com.chainmarket.dto.GoodsUploadDTO;
import com.chainmarket.entity.*;
import com.chainmarket.service.IGoodsService;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Collections;
import java.util.ArrayList;

@Service
public class GoodsServiceImpl implements IGoodsService {
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private GoodsCategoryDao categoryDao;
    
    @Autowired
    private GoodsImageDao imageDao;
    
    @Autowired
    private AuditInfoDao auditInfoDao;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadGoods(GoodsUploadDTO goodsDTO, Long sellerId) {
        // 验证分类是否存在
        GoodsCategory category = categoryDao.selectById(goodsDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException("商品分类不存在");
        }
        
        // 创建商品
        Goods goods = new Goods();
        goods.setGoodsName(goodsDTO.getGoodsName());
        goods.setGoodsDesc(goodsDTO.getGoodsDesc());
        goods.setCategoryId(goodsDTO.getCategoryId());
        goods.setPrice(goodsDTO.getPrice());
        goods.setSellerId(sellerId);
        goods.setStatus(0);  // 待审核状态
        
        // 设置商品图片URL
        String imageUrl = goodsDTO.getImageUrl();
        goods.setImageUrl(imageUrl != null ? imageUrl.trim() : null);
        
        // 保存商品信息
        goodsDao.insert(goods);
    }
    
    @Override
    public List<Goods> getPendingGoods() {
        return goodsDao.selectPendingGoods();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditGoods(Long goodsId, Integer status, String comment) {
        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 更新商品状态
        goods.setStatus(status);
        goodsDao.updateById(goods);
        
        // 记录审核信息
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setObjectId(goodsId);
        auditInfo.setAuditType(2);  // 商品审核
        auditInfo.setAuditStatus(status);
        auditInfo.setAuditOpinion(comment);
        auditInfoDao.insert(auditInfo);
    }
    
    @Override
    public List<GoodsCategory> getCategories() {
        return categoryDao.selectList(null);
    }
    
    @Override
    public List<Goods> getOnSaleGoods() {
        return goodsDao.selectList(new QueryWrapper<Goods>()
                .eq("status", 1)  // 已上架状态
                .orderByDesc("createTime"));
    }
    
    @Override
    public List<Goods> getOnSaleGoodsByCategory(Long categoryId) {
        return goodsDao.selectList(new QueryWrapper<Goods>()
                .eq("status", 1)  // 已上架状态
                .eq("categoryId", categoryId)
                .orderByDesc("createTime"));
    }
    
    @Override
    public List<Goods> getSellerGoods(Long sellerId) {
        return goodsDao.selectList(new QueryWrapper<Goods>()
                .eq("sellerId", sellerId)
                .orderByDesc("createTime"));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsStatus(Long goodsId, Integer status, Long sellerId) {
        // 获取商品信息
        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 验证卖家权限
        if (!goods.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        
        // 验证状态变更的合法性
        if (goods.getStatus() == 0) {
            throw new BusinessException("商品待审核，不能变更状态");
        }
        if (status != 1 && status != 2) {
            throw new BusinessException("无效的商品状态");
        }
        
        // 更新商品状态
        goods.setStatus(status);
        goodsDao.updateById(goods);
    }
    
    @Override
    public Goods getGoodsDetail(Long goodsId) {
        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        return goods;
    }
    
    @Override
    public List<Goods> getLatestGoods(int limit) {
        return goodsDao.selectList(new QueryWrapper<Goods>()
                .eq("status", 1)  // 已上架状态
                .orderByDesc("createTime")
                .last("LIMIT " + limit));
    }

    @Override
    public List<Goods> getUserGoods(Long userId) {
        System.out.println("===== 开始查询用户商品 =====");
        System.out.println("用户ID: " + userId);
        
        try {
            // 使用原生SQL查询测试
            List<Goods> rawResult = goodsDao.selectByMap(Collections.singletonMap("sellerId", userId));
            System.out.println("原生查询结果数量: " + (rawResult != null ? rawResult.size() : "null"));
            if (rawResult != null && !rawResult.isEmpty()) {
                System.out.println("第一条商品ID: " + rawResult.get(0).getGoodsId());
            }
            
            // 使用Lambda查询
            LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Goods::getSellerId, userId)
                   .orderByDesc(Goods::getCreateTime);
            
            List<Goods> result = goodsDao.selectList(wrapper);
            System.out.println("Lambda查询结果数量: " + (result != null ? result.size() : "null"));
            
            return result;
        } catch (Exception e) {
            System.out.println("查询用户商品异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            System.out.println("===== 结束查询用户商品 =====");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void listGoods(Long goodsId, Long userId) {
        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 验证所有权
        if (!goods.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }
        
        // 验证状态
        if (goods.getStatus() != 2) {  // 只有已下架的商品可以上架
            throw new BusinessException("当前商品状态不可上架");
        }
        
        // 更新状态为已上架
        goods.setStatus(1);
        goodsDao.updateById(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delistGoods(Long goodsId, Long userId) {
        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 验证所有权
        if (!goods.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }
        
        // 验证状态
        if (goods.getStatus() != 1) {  // 只有已上架的商品可以下架
            throw new BusinessException("当前商品状态不可下架");
        }
        
        // 更新状态为已下架
        goods.setStatus(2);
        goodsDao.updateById(goods);
    }
} 