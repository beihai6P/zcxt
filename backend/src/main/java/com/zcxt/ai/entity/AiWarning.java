package com.zcxt.ai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("ai_warning")
public class AiWarning {
    @TableId("warning_id")
    private String warningId;

    private String warningType;
    private String content;
    private LocalDateTime warningTime;
    private String status;
    private String handlerId;
    private LocalDateTime handleTime;

    public String getWarningId() {
        return warningId;
    }

    public void setWarningId(String warningId) {
        this.warningId = warningId;
    }

    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getWarningTime() {
        return warningTime;
    }

    public void setWarningTime(LocalDateTime warningTime) {
        this.warningTime = warningTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }
}

