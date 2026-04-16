package com.zcxt.stats.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("asset_stats_daily")
public class AssetStatsDaily {
    @TableId("id")
    private String id;

    private LocalDate statDate;
    private int total;
    private int inUse;
    private int idle;
    private int repairing;
    private int scrapped;
    private LocalDateTime createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getInUse() {
        return inUse;
    }

    public void setInUse(int inUse) {
        this.inUse = inUse;
    }

    public int getIdle() {
        return idle;
    }

    public void setIdle(int idle) {
        this.idle = idle;
    }

    public int getRepairing() {
        return repairing;
    }

    public void setRepairing(int repairing) {
        this.repairing = repairing;
    }

    public int getScrapped() {
        return scrapped;
    }

    public void setScrapped(int scrapped) {
        this.scrapped = scrapped;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

