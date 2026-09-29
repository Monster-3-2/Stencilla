from fastapi import APIRouter, Depends, File, HTTPException, Query, UploadFile
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.outfit import (
    ClothingTagResponse, WardrobeAnalyticsRequest, WardrobeAnalyticsResponse, AiSuggestionRequest,
)
from app.services.groq_service import AIServiceError, tag_clothing_item, generate_ai_wardrobe_suggestion
from app.services.wardrobe_analytics import compute_analytics, search_items, filter_items

MAX_IMAGE_BYTES = 10 * 1024 * 1024
router = APIRouter(prefix="/wardrobe", tags=["wardrobe"])


@router.post("/tag", response_model=ClothingTagResponse)
async def tag_item(
    image: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
):
    """Stateless AI tagging. Image never stored server-side."""
    image_bytes = await image.read(MAX_IMAGE_BYTES + 1)
    if len(image_bytes) > MAX_IMAGE_BYTES:
        raise HTTPException(status_code=413, detail="Image too large (max 10 MB)")
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Empty image")
    try:
        tags = tag_clothing_item(image_bytes, content_type=image.content_type or "image/jpeg")
    except AIServiceError as exc:
        raise HTTPException(status_code=502, detail="Tagging service unavailable") from exc
    return ClothingTagResponse(**tags)


@router.post("/analytics", response_model=WardrobeAnalyticsResponse)
def wardrobe_analytics(
    payload: WardrobeAnalyticsRequest,
    current_user: User = Depends(get_current_user),
):
    """Compute wardrobe overview, smart categories, and top colors from client-sent tag data.
    Also returns an AI suggestion sentence. No wardrobe data is stored server-side."""
    result = compute_analytics(payload)
    # AI suggestion
    suggestion = generate_ai_wardrobe_suggestion(
        items=[i.model_dump() for i in payload.items],
        style_goal=current_user.style_goal,
        preferred_colors=current_user.preferred_colors,
    )
    result.ai_suggestion = suggestion
    return result


@router.post("/search")
def wardrobe_search(
    payload: WardrobeAnalyticsRequest,
    query: str = Query(default="", max_length=100),
    current_user: User = Depends(get_current_user),
):
    """Text search over wardrobe tags. Client sends full local wardrobe, server returns matching IDs."""
    matched = search_items(payload.items, query)
    return {"item_ids": [i.id for i in matched], "count": len(matched)}


@router.post("/filter")
def wardrobe_filter(
    payload: WardrobeAnalyticsRequest,
    category: str | None = Query(default=None),
    formality: str | None = Query(default=None),
    season: str | None = Query(default=None),
    color: str | None = Query(default=None),
    current_user: User = Depends(get_current_user),
):
    """Filter wardrobe by category (tops/bottoms/dresses/outers/shoes/bags/accessories),
    formality, season, or color. Returns matching item IDs."""
    matched = filter_items(payload.items, category=category, formality=formality, season=season, color=color)
    return {"item_ids": [i.id for i in matched], "count": len(matched), "filter_applied": {
        "category": category, "formality": formality, "season": season, "color": color,
    }}


@router.post("/ai-suggestion")
def ai_wardrobe_suggestion(
    payload: AiSuggestionRequest,
    current_user: User = Depends(get_current_user),
):
    """Get a single AI-generated wardrobe improvement tip."""
    suggestion = generate_ai_wardrobe_suggestion(
        items=[i.model_dump() for i in payload.items],
        style_goal=payload.style_goal or current_user.style_goal,
        preferred_colors=payload.preferred_colors or current_user.preferred_colors,
    )
    return {"suggestion": suggestion}
