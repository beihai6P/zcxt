package com.zcxt.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("asset_inventory_detail")
public class AssetInventoryDetail {
    @TableId("detail_id")
    private String detailId;

    private String inventoryId;
    private String assetId;
    private String status;
    private String abnormalType;
    private String abnormalReason;
    private LocalDateTime checkTime;
    private String checkerId;

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(String abnormalType) {
        this.abnormalType = abnormalType;
    }

    public String getAbnormalReason() {
        return abnormalReason;
    }

    public void setAbnormalReason(String abnormalReason) {
        this.abnormalReason = abnormalReason;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public String getCheckerId() {
        return checkerId;
    }

    public void setCheckerId(String checkerId) {
        this.checkerId = checkerId;
    }
}
