from fastapi import APIRouter, Depends, HTTPException
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.outfit import OutfitRequest, OutfitResponse
from app.services.groq_service import AIServiceError, generate_outfit

router = APIRouter(prefix="/outfits", tags=["outfits"])


@router.post("/suggest", response_model=OutfitResponse)
def suggest_outfit(payload: OutfitRequest, current_user: User = Depends(get_current_user)):
    if not payload.wardrobe_items:
        raise HTTPException(status_code=400, detail="No wardrobe items provided. Add some clothing photos first.")

    anchor_item = None
    if payload.anchor_item_id is not None:
        anchor = next((i for i in payload.wardrobe_items if i.id == payload.anchor_item_id), None)
        if not anchor:
            raise HTTPException(status_code=404, detail="Anchor item not found in the provided wardrobe")
        anchor_item = anchor.model_dump()

    profile = {
        "age": current_user.age, "gender": current_user.gender, "lifestyle": current_user.lifestyle,
        "height_cm": current_user.height_cm, "body_type": current_user.body_type,
        "skin_tone": current_user.skin_tone, "style_goal": current_user.style_goal,
        "preferred_colors": current_user.preferred_colors, "preferred_fits": current_user.preferred_fits,
        "preferred_silhouettes": current_user.preferred_silhouettes,
        "comfort_priority": current_user.comfort_priority,
        "formality_preference": current_user.formality_preference,
        "modesty_preference": current_user.modesty_preference,
        "style_inspirations": current_user.style_inspirations,
        "confidence_signals": current_user.confidence_signals,
    }

    try:
        result = generate_outfit(
            profile=profile, wardrobe_items=[i.model_dump() for i in payload.wardrobe_items],
            occasion=payload.occasion, dress_code=payload.dress_code, anchor_item=anchor_item,
            notes=payload.notes, weather_temp=payload.weather_temp,
            weather_condition=payload.weather_condition, calendar_event=payload.calendar_event,
            preference_summary=payload.preference_summary,
        )
    except AIServiceError as exc:
        raise HTTPException(status_code=502, detail="Styling service unavailable") from exc

    valid_ids = {i.id for i in payload.wardrobe_items}
    chosen_ids = [i for i in result.get("item_ids", []) if i in valid_ids]
    if anchor_item and anchor_item["id"] not in chosen_ids:
        chosen_ids.append(anchor_item["id"])

    factors = []
    for factor in result.get("factors", []):
        if isinstance(factor, dict) and factor.get("label") and factor.get("explanation"):
            try:
                score = max(0, min(100, int(factor.get("score", 0))))
            except (TypeError, ValueError):
                score = 0
            factors.append({"label": str(factor["label"])[:40], "score": score,
                             "explanation": str(factor["explanation"])[:240]})

    alternatives = []
    for alt in result.get("alternatives", []):
        if not isinstance(alt, dict):
            continue
        ids = [i for i in alt.get("item_ids", []) if i in valid_ids]
        if ids and alt.get("reason"):
            alternatives.append({"item_ids": ids, "label": str(alt.get("label", "Alternative"))[:50],
                                  "reason": str(alt["reason"])[:240]})

    return OutfitResponse(
        occasion=payload.occasion, item_ids=chosen_ids, reasoning=result.get("reasoning"),
        shopping_suggestions=result.get("shopping_suggestions", []),
        avatar_description=result.get("avatar_description"),
        factors=factors[:5], alternatives=alternatives[:3],
    )
