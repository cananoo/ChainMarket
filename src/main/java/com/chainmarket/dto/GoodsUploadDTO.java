package com.chainmarket.dto;

import java.math.BigDecimal;

public class GoodsUploadDTO {
    private String goodsName;    // 商品名称
    private Long categoryId;     // 商品分类ID
    private BigDecimal price;    // 商品价格
    private String goodsDesc;    // 商品描述
    private String imageUrl;     // 商品图片URL，单张图片

    // Getters
    public String getGoodsName() {
        return goodsName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getGoodsDesc() {
        return goodsDesc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setGoodsDesc(String goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "GoodsUploadDTO{" +
                "goodsName='" + goodsName + '\'' +
                ", categoryId=" + categoryId +
                ", price=" + price +
                ", goodsDesc='" + goodsDesc + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
} 