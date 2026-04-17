from fastapi import FastAPI
from pydantic import BaseModel
from datetime import datetime, timedelta
import os
import requests
import json


app = FastAPI(title="zcxt-ai-service", version="0.1.0")

# 大模型API配置
LLM_API_KEY = os.getenv("LLM_API_KEY", "ark-80222631-8d42-42cf-823d-ce27a2180489-ae528")
LLM_API_URL = os.getenv("LLM_API_URL", "https://ark.cn-beijing.volces.com/api/v3/responses")
LLM_MODEL = os.getenv("LLM_MODEL", "doubao-seed-2-0-pro-260215")  # 豆包模型名称


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


def call_llm(prompt):
    """调用大模型API获取响应"""
    if not LLM_API_KEY:
        return None
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {LLM_API_KEY}"
    }
    
    data = {
        "model": LLM_MODEL,
        "input": [
            {
                "role": "system",
                "content": [
                    {
                        "type": "input_text",
                        "text": "你是一个IT资产管理系统的AI助手，负责资产预警、闲置资产推荐和耗材预测。"
                    }
                ]
            },
            {
                "role": "user",
                "content": [
                    {
                        "type": "input_text",
                        "text": prompt
                    }
                ]
            }
        ]
    }
    
    try:
        response = requests.post(LLM_API_URL, headers=headers, data=json.dumps(data))
        response.raise_for_status()
        result = response.json()
        print(f"LLM API响应: {result}")
        # 豆包API的响应格式
        if isinstance(result, dict):
            if "output" in result:
                output = result["output"]
                if isinstance(output, dict) and "text" in output:
                    return output["text"]
                elif isinstance(output, list) and len(output) > 0:
                    # 处理output是列表的情况
                    first_output = output[0]
                    if isinstance(first_output, dict) and "text" in first_output:
                        return first_output["text"]
        return None
    except Exception as e:
        print(f"LLM API调用失败: {e}")
        return None


@app.get("/health")
def health():
    return {"ok": True, "time": datetime.utcnow().isoformat()}


@app.get("/warnings", response_model=list[WarningItem])
def warnings():
    mode = os.getenv("ZCXT_AI_MODE", "demo")
    if mode == "empty":
        return []
    
    # 优先使用大模型生成预警
    if LLM_API_KEY:
        prompt = "请生成2条IT资产管理系统的预警信息，包括类型和内容。类型可以是'耗材库存预警'、'资产异常调动预警'、'资产故障预警'等。每条预警内容要具体，有实际参考价值。返回格式为JSON数组，包含type和content字段。"
        llm_response = call_llm(prompt)
        if llm_response:
            try:
                # 尝试解析大模型返回的JSON
                import ast
                warnings_list = ast.literal_eval(llm_response)
                if isinstance(warnings_list, list):
                    return [WarningItem(**item) for item in warnings_list[:2]]
            except Exception:
                pass
    
    #  fallback到默认预警
    return [
        WarningItem(type="耗材库存预警", content="硒鼓库存预计7天后低于阈值，请关注补货计划"),
        WarningItem(type="资产异常调动预警", content="检测到资产在短期内多次跨部门调动，请核查调动原因"),
    ]


@app.get("/recommendations/idle", response_model=list[IdleRecommendationItem])
def idle_recommendations():
    # 从后端获取实际的资产数据
    backend_url = os.getenv("BACKEND_URL", "http://localhost:8080")
    assets = []
    dept_assets = {}
    
    try:
        print(f"尝试连接后端服务: {backend_url}")
        
        # 首先尝试登录获取 token
        login_data = {"username": "admin", "password": "admin123"}
        login_response = requests.post(f"{backend_url}/api/auth/login", json=login_data, timeout=5)
        print(f"登录响应状态码: {login_response.status_code}")
        
        if login_response.status_code == 200:
            login_result = login_response.json()
            print(f"登录响应数据: {login_result}")
            if login_result.get("success"):
                token = login_result.get("data", {}).get("token")
                print(f"获取到的 token: {token}")
                
                # 使用获取到的 token 请求资产数据
                headers = {"Authorization": f"Bearer {token}"}
                
                # 获取所有资产
                assets_response = requests.get(f"{backend_url}/api/assets", headers=headers, timeout=5)
                print(f"获取资产响应状态码: {assets_response.status_code}")
                if assets_response.status_code == 200:
                    assets_data = assets_response.json()
                    print(f"获取资产响应数据: {assets_data}")
                    if assets_data.get("success"):
                        assets = assets_data.get("data", {}).get("records", [])
                        print(f"获取到的资产数量: {len(assets)}")
                
                # 获取部门资产分布
                dept_response = requests.get(f"{backend_url}/api/stats/by-dept", headers=headers, timeout=5)
                print(f"获取部门资产分布响应状态码: {dept_response.status_code}")
                if dept_response.status_code == 200:
                    dept_data = dept_response.json()
                    print(f"获取部门资产分布响应数据: {dept_data}")
                    if dept_data.get("success"):
                        dept_list = dept_data.get("data", [])
                        print(f"获取到的部门数量: {len(dept_list)}")
                        for dept in dept_list:
                            dept_assets[dept.get("deptName")] = {
                                "total": dept.get("total", 0),
                                "inUse": dept.get("inUse", 0),
                                "idle": dept.get("idle", 0),
                                "repairing": dept.get("repairing", 0),
                                "scrapped": dept.get("scrapped", 0)
                            }
    except Exception as e:
        print(f"获取后端数据失败: {e}")
        import traceback
        traceback.print_exc()
    
    # 筛选闲置资产
    idle_assets = []
    for asset in assets:
        # 尝试不同的闲置状态值
        status = asset.get("status", "").strip()
        if status in ["闲置", "idle", "Idle", "IDLE"]:
            # 计算闲置天数（简化处理，实际应该根据最后使用时间计算）
            idle_days = 30  # 默认闲置30天
            asset_id = asset.get("assetId", "")
            asset_name = asset.get("assetName", "")
            if asset_id and asset_name:
                idle_assets.append({
                    "asset_id": asset_id,
                    "asset_name": asset_name,
                    "idle_days": idle_days
                })
    
    # 如果没有找到闲置资产，使用所有资产作为备选
    if not idle_assets:
        for asset in assets[:5]:  # 最多使用5条资产
            # 计算闲置天数（简化处理，实际应该根据最后使用时间计算）
            idle_days = 30  # 默认闲置30天
            asset_id = asset.get("assetId", "")
            asset_name = asset.get("assetName", "")
            if asset_id and asset_name:
                idle_assets.append({
                    "asset_id": asset_id,
                    "asset_name": asset_name,
                    "idle_days": idle_days
                })
    
    # 分析部门资产状态，找出需要资产的部门
    needy_depts = []
    for dept_name, stats in dept_assets.items():
        # 找出维修中资产较多的部门
        if stats.get("repairing", 0) > 0:
            needy_depts.append(dept_name)
    
    # 生成推荐
    recommendations = []
    for i, asset in enumerate(idle_assets[:5]):  # 最多返回5条推荐
        # 为每个闲置资产生成推荐
        if needy_depts:
            # 如果有需要资产的部门，推荐调拨到该部门
            dept = needy_depts[i % len(needy_depts)]
            recommendation = f"该资产已闲置{asset['idle_days']}天，建议调拨至{dept}（该部门有{dept_assets[dept]['repairing']}台资产处于维修状态）"
        else:
            # 如果没有需要资产的部门，给出其他建议
            if asset['idle_days'] > 90:
                recommendation = f"该资产已闲置{asset['idle_days']}天，建议重新评估使用需求或报废处理"
            elif asset['idle_days'] > 60:
                recommendation = f"该资产已闲置{asset['idle_days']}天，建议作为备用设备或测试环境使用"
            else:
                recommendation = f"该资产已闲置{asset['idle_days']}天，建议在公司内部发布调拨通知"
        
        recommendations.append(IdleRecommendationItem(
            asset_id=asset['asset_id'],
            asset_name=asset['asset_name'],
            idle_days=asset['idle_days'],
            recommendation=recommendation
        ))
    
    # 如果没有实际的闲置资产，返回默认推荐
    if not recommendations:
        return [
            IdleRecommendationItem(asset_id="AST-001", asset_name="高配设计工作站", idle_days=45, recommendation="建议调拨至设计部"),
            IdleRecommendationItem(asset_id="AST-002", asset_name="会议室投影仪", idle_days=60, recommendation="建议重新评估使用需求或报废处理"),
            IdleRecommendationItem(asset_id="AST-003", asset_name="备用服务器", idle_days=120, recommendation="建议作为测试环境使用"),
        ]
    
    return recommendations


@app.get("/predictions/consumables", response_model=list[ConsumablePredictionItem])
def consumable_predictions():
    # 生成未来7天的日期
    predictions = []
    today = datetime.now()
    
    # 优先使用大模型生成预测
    if LLM_API_KEY:
        prompt = "请生成未来7天的耗材使用量预测，每天一个数据点。返回格式为JSON数组，包含date和predicted_usage字段，date格式为'YYYY-MM-DD'，predicted_usage为整数。"
        llm_response = call_llm(prompt)
        if llm_response:
            try:
                import ast
                predictions_list = ast.literal_eval(llm_response)
                if isinstance(predictions_list, list):
                    return [ConsumablePredictionItem(**item) for item in predictions_list[:7]]
            except Exception:
                pass
    
    # fallback到默认预测
    for i in range(7):
        date = (today + timedelta(days=i+1)).strftime("%Y-%m-%d")
        # 生成一些随机但合理的预测值
        base_usage = 120
        variation = (i % 3) * 15
        predictions.append(ConsumablePredictionItem(date=date, predicted_usage=base_usage + variation))
    
    return predictions

