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
} 