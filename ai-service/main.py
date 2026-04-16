from fastapi import FastAPI
from pydantic import BaseModel
from datetime import datetime
import os


app = FastAPI(title="zcxt-ai-service", version="0.1.0")


class WarningItem(BaseModel):
    type: str
    content: str


class IdleRecommendationItem(BaseModel):
    asset_id: str
    asset_name: str
    idle_days: int
    recommendation: str


class ConsumablePredictionItem(BaseModel):
    date: str
    predicted_usage: int


@app.get("/health")
def health():
    return {"ok": True, "time": datetime.utcnow().isoformat()}


@app.get("/warnings", response_model=list[WarningItem])
def warnings():
    mode = os.getenv("ZCXT_AI_MODE", "demo")
    if mode == "empty":
        return []
    return [
        WarningItem(type="耗材库存预警", content="硒鼓库存预计7天后低于阈值，请关注补货计划"),
        WarningItem(type="资产异常调动预警", content="检测到资产在短期内多次跨部门调动，请核查调动原因"),
    ]


@app.get("/recommendations/idle", response_model=list[IdleRecommendationItem])
def idle_recommendations():
    return [
        IdleRecommendationItem(asset_id="AST-001", asset_name="高配设计工作站", idle_days=45, recommendation="建议调拨至设计部"),
        IdleRecommendationItem(asset_id="AST-002", asset_name="会议室投影仪", idle_days=60, recommendation="建议重新评估使用需求或报废处理"),
        IdleRecommendationItem(asset_id="AST-003", asset_name="备用服务器", idle_days=120, recommendation="建议作为测试环境使用"),
    ]


@app.get("/predictions/consumables", response_model=list[ConsumablePredictionItem])
def consumable_predictions():
    return [
        ConsumablePredictionItem(date="2026-04-17", predicted_usage=120),
        ConsumablePredictionItem(date="2026-04-18", predicted_usage=135),
        ConsumablePredictionItem(date="2026-04-19", predicted_usage=110),
        ConsumablePredictionItem(date="2026-04-20", predicted_usage=150),
        ConsumablePredictionItem(date="2026-04-21", predicted_usage=165),
        ConsumablePredictionItem(date="2026-04-22", predicted_usage=140),
        ConsumablePredictionItem(date="2026-04-23", predicted_usage=180),
    ]

