package com.zcxt.asset.dto;

import com.alibaba.excel.annotation.ExcelProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetExcelRow {
    @ExcelProperty("资产编号")
    private String assetId;

    @ExcelProperty("资产类型")
    private String assetType;

    @ExcelProperty("资产名称")
    private String assetName;

    @ExcelProperty("型号")
    private String model;

    @ExcelProperty("规格")
    private String specification;

    @ExcelProperty("采购日期")
    private LocalDate purchaseDate;

    @ExcelProperty("采购价格")
    private BigDecimal purchasePrice;

    @ExcelProperty("供应商")
    private String supplier;

    @ExcelProperty("部门ID")
    private String deptId;

    @ExcelProperty("使用人ID")
    private String userId;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("备注")
    private String remark;

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

