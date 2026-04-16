package com.zcxt.consumable.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("asset_consumable_stock")
public class AssetConsumableStock {
    @TableId("consumable_id")
    private String consumableId;

    private String consumableType;
    private String consumableName;
    private Integer stockQuantity;
    private Integer warningThreshold;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getConsumableId() {
        return consumableId;
    }

    public void setConsumableId(String consumableId) {
        this.consumableId = consumableId;
    }

    public String getConsumableType() {
        return consumableType;
    }

    public void setConsumableType(String consumableType) {
        this.consumableType = consumableType;
    }

    public String getConsumableName() {
        return consumableName;
    }

    public void setConsumableName(String consumableName) {
        this.consumableName = consumableName;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(Integer warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
