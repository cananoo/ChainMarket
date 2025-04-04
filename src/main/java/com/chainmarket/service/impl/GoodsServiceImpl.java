package com.chainmarket.service.impl;

import com.chainmarket.dao.GoodsDao;
import com.chainmarket.dao.GoodsCategoryDao;
import com.chainmarket.dao.GoodsImageDao;
import com.chainmarket.dao.AuditInfoDao;
import com.chainmarket.dao.ChainEvidenceDao;
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
    private ChainEvidenceDao chainEvidenceDao;
    
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

    @Override
    public List<ChainEvidence> getGoodsEvidence(Long goodsId) {
        // 查询与该商品相关的所有存证记录
        // 使用SQL模糊查询，查找evidenceContent中包含商品ID的记录
        String pattern = "%商品：(" + goodsId + ")%";
        String pattern2 = "%商品：" + goodsId + "%";
        
        List<ChainEvidence> evidenceList = chainEvidenceDao.selectList(
            new QueryWrapper<ChainEvidence>()
                .and(wrapper -> wrapper
                    .like("evidenceContent", pattern)
                    .or()
                    .like("evidenceContent", pattern2))
                .in("evidenceType", 0, 1)  // 只查询创建商品和订单交易类型的存证
                .orderByAsc("createTime")  // 按时间正序排列
        );
        
        return evidenceList;
    }

    @Override
    public Goods getGoodsById(Long goodsId) {
        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        return goods;
    }

    @Override
    public GoodsCategory getCategoryById(Long categoryId) {
        return categoryDao.selectById(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(GoodsCategory category) {
        // 验证类别名称是否已存在
        if (categoryDao.exists(new QueryWrapper<GoodsCategory>()
                .eq("categoryName", category.getCategoryName()))) {
            throw new BusinessException("类别名称已存在");
        }
        
        // 保存类别
        categoryDao.insert(category);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(GoodsCategory category) {
        // 验证类别是否存在
        if (!categoryDao.exists(new QueryWrapper<GoodsCategory>()
                .eq("categoryId", category.getCategoryId()))) {
            throw new BusinessException("类别不存在");
        }
        
        // 验证类别名称是否已被其他类别使用
        if (categoryDao.exists(new QueryWrapper<GoodsCategory>()
                .eq("categoryName", category.getCategoryName())
                .ne("categoryId", category.getCategoryId()))) {
            throw new BusinessException("类别名称已被使用");
        }
        
        // 更新类别
        categoryDao.updateById(category);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        // 验证类别是否存在
        if (!categoryDao.exists(new QueryWrapper<GoodsCategory>()
                .eq("categoryId", categoryId))) {
            throw new BusinessException("类别不存在");
        }
        
        // 检查是否有商品使用该类别
        if (goodsDao.exists(new QueryWrapper<Goods>()
                .eq("categoryId", categoryId))) {
            throw new BusinessException("该类别下有商品，无法删除");
        }
        
        // 删除类别
        categoryDao.deleteById(categoryId);
    }
} 