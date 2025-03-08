package com.chainmarket.service;

import com.chainmarket.dto.GoodsUploadDTO;
import com.chainmarket.entity.Goods;
import com.chainmarket.entity.GoodsCategory;
import java.util.List;

public interface IGoodsService {
    /**
     * 上传商品
     */
    void uploadGoods(GoodsUploadDTO goodsDTO, Long sellerId);
    
    /**
     * 获取待审核商品列表
     */
    List<Goods> getPendingGoods();
    
    /**
     * 审核商品
     */
    void auditGoods(Long goodsId, Integer status, String comment);
    
    /**
     * 获取商品分类列表
     */
    List<GoodsCategory> getCategories();
    
    /**
     * 获取已上架商品列表
     */
    List<Goods> getOnSaleGoods();
    
    /**
     * 按分类获取已上架商品列表
     */
    List<Goods> getOnSaleGoodsByCategory(Long categoryId);
    
    /**
     * 获取卖家的商品列表
     */
    List<Goods> getSellerGoods(Long sellerId);
    
    /**
     * 获取商品详情
     */
    Goods getGoodsDetail(Long goodsId);
    
    /**
     * 更新商品状态
     * @param goodsId 商品ID
     * @param status 新状态
     * @param sellerId 卖家ID（用于验证权限）
     */
    void updateGoodsStatus(Long goodsId, Integer status, Long sellerId);
    
    /**
     * 获取最新上架的商品
     * @param limit 限制数量
     * @return 商品列表
     */
    List<Goods> getLatestGoods(int limit);

    /**
     * 获取用户发布的商品列表
     * @param userId 用户ID
     * @return 商品列表
     */
    List<Goods> getUserGoods(Long userId);

    /**
     * 上架商品
     * @param goodsId 商品ID
     * @param userId 用户ID
     */
    void listGoods(Long goodsId, Long userId);

    /**
     * 下架商品
     * @param goodsId 商品ID
     * @param userId 用户ID
     */
    void delistGoods(Long goodsId, Long userId);
} 