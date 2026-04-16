package com.zcxt.asset.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("asset_assembly")
public class AssetAssembly {
    @TableId("assembly_id")
    private String assemblyId;

    private String assetId;
    private String componentId;
    private LocalDateTime assemblyDate;
    private String operatorId;
    private String remark;
    private LocalDateTime createTime;

    public String getAssemblyId() { return assemblyId; }
    public void setAssemblyId(String assemblyId) { this.assemblyId = assemblyId; }
    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }
    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }
    public LocalDateTime getAssemblyDate() { return assemblyDate; }
    public void setAssemblyDate(LocalDateTime assemblyDate) { this.assemblyDate = assemblyDate; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
