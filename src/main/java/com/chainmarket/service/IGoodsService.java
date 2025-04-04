package com.chainmarket.service;

import com.chainmarket.dto.GoodsUploadDTO;
import com.chainmarket.entity.Goods;
import com.chainmarket.entity.GoodsCategory;
import com.chainmarket.entity.ChainEvidence;
import java.util.List;

public interface IGoodsService {
    /**
     * 上传商品
     */
    void uploadGoods(GoodsUploadDTO goodsDTO, Long sellerId);
    

    
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

    /**
     * 获取商品溯源信息
     * @param goodsId 商品ID
     * @return 溯源信息列表
     */
    List<ChainEvidence> getGoodsEvidence(Long goodsId);

    /**
     * 根据ID获取商品
     * @param goodsId 商品ID
     * @return 商品信息
     */
    Goods getGoodsById(Long goodsId);

    /**
     * 根据ID获取商品分类
     * @param categoryId 分类ID
     * @return 分类信息
     */
    GoodsCategory getCategoryById(Long categoryId);

    /**
     * 添加商品类别
     * @param category 商品类别
     */
    void addCategory(GoodsCategory category);
    
    /**
     * 更新商品类别
     * @param category 商品类别
     */
    void updateCategory(GoodsCategory category);
    
    /**
     * 删除商品类别
     * @param categoryId 类别ID
     */
    void deleteCategory(Long categoryId);
} 