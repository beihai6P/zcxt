package com.zcxt.asset.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("asset_approval")
public class AssetApproval {
    @TableId("approval_id")
    private String approvalId;

    private String assetId;
    private String applicantId;
    private String approverId;
    private String approvalType;
    private String status;
    private String reason;
    private String comment;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;

    public String getApprovalId() { return approvalId; }
    public void setApprovalId(String approvalId) { this.approvalId = approvalId; }
    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }
    public String getApprovalType() { return approvalType; }
    public void setApprovalType(String approvalType) { this.approvalType = approvalType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }
}
