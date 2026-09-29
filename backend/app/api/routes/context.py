from urllib.parse import urlencode
from urllib.request import urlopen
import json
from fastapi import APIRouter, Depends, HTTPException, Query
from app.api.deps import get_current_user
from app.models.user import User

router = APIRouter(prefix="/context", tags=["context"])


@router.get("/weather")
def weather(
    latitude: float = Query(..., ge=-90, le=90),
    longitude: float = Query(..., ge=-180, le=180),
    _current_user: User = Depends(get_current_user),
):
    params = urlencode({"latitude": latitude, "longitude": longitude,
                        "current": "temperature_2m,weather_code", "temperature_unit": "celsius"})
    try:
        with urlopen(f"https://api.open-meteo.com/v1/forecast?{params}", timeout=5) as r:
            data = json.load(r)
        current = data.get("current") or {}
        if "temperature_2m" not in current:
            raise ValueError("missing current conditions")
        return {"temperature": current["temperature_2m"], "condition": _condition(current.get("weather_code"))}
    except Exception as exc:
        raise HTTPException(status_code=502, detail="Weather provider unavailable") from exc


@router.get("/forecast")
def forecast(
    latitude: float = Query(..., ge=-90, le=90),
    longitude: float = Query(..., ge=-180, le=180),
    days: int = Query(7, ge=1, le=7),
    _current_user: User = Depends(get_current_user),
):
    params = urlencode({"latitude": latitude, "longitude": longitude, "forecast_days": days,
                        "daily": "temperature_2m_max,temperature_2m_min,weather_code",
                        "temperature_unit": "celsius", "timezone": "auto"})
    try:
        with urlopen(f"https://api.open-meteo.com/v1/forecast?{params}", timeout=5) as r:
            data = json.load(r)
        daily = data.get("daily") or {}
        dates = daily.get("time") or []
        highs = daily.get("temperature_2m_max") or []
        lows = daily.get("temperature_2m_min") or []
        codes = daily.get("weather_code") or []
        return {"days": [
            {"date": d, "high": highs[i], "low": lows[i], "condition": _condition(codes[i] if i < len(codes) else None)}
            for i, d in enumerate(dates[:days])
        ]}
    except Exception as exc:
        raise HTTPException(status_code=502, detail="Weather provider unavailable") from exc


def _condition(code: int | None) -> str:
    if code in (0, 1): return "Sunny"
    if code in (2, 3): return "Cloudy"
    if code in (45, 48): return "Foggy"
    if code in (51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82): return "Rainy"
    if code in (71, 73, 75, 77, 85, 86): return "Snowy"
    if code in (95, 96, 99): return "Stormy"
    return "Unknown"
