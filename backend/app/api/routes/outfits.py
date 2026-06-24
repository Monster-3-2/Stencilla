from fastapi import APIRouter, Depends, HTTPException

from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.outfit import OutfitRequest, OutfitResponse
from app.services.groq_service import AIServiceError, generate_outfit

router = APIRouter(prefix="/outfits", tags=["outfits"])


@router.post("/suggest", response_model=OutfitResponse)
def suggest_outfit(
    payload: OutfitRequest,
    current_user: User = Depends(get_current_user),
):
    """Stateless outfit reasoning: the client sends its locally-stored wardrobe (just the
    tags, not the images) plus the user's profile context, and gets back which item ids
    form a coherent outfit. No wardrobe or outfit history is stored on the server - if the
    client wants outfit history, it should save the response locally.
    """
    if not payload.wardrobe_items:
        raise HTTPException(
            status_code=400,
            detail="No wardrobe items provided. Add some clothing photos first.",
        )

    anchor_item = None
    if payload.anchor_item_id is not None:
        anchor = next((i for i in payload.wardrobe_items if i.id == payload.anchor_item_id), None)
        if not anchor:
            raise HTTPException(status_code=404, detail="Anchor item not found in the provided wardrobe")
        anchor_item = anchor.model_dump()

    profile = {
        "age": current_user.age,
        "gender": current_user.gender,
        "lifestyle": current_user.lifestyle,
        "height_cm": current_user.height_cm,
        "body_type": current_user.body_type,
        "skin_tone": current_user.skin_tone,
        "style_goal": current_user.style_goal,
    }

    try:
        result = generate_outfit(
            profile=profile,
            wardrobe_items=[i.model_dump() for i in payload.wardrobe_items],
            occasion=payload.occasion,
            anchor_item=anchor_item,
            notes=payload.notes,
        )
    except AIServiceError as exc:
        raise HTTPException(status_code=502, detail=f"Styling AI failed: {exc}") from exc

    valid_ids = {i.id for i in payload.wardrobe_items}
    chosen_ids = [i for i in result.get("item_ids", []) if i in valid_ids]
    if anchor_item and anchor_item["id"] not in chosen_ids:
        chosen_ids.append(anchor_item["id"])

    return OutfitResponse(
        occasion=payload.occasion,
        item_ids=chosen_ids,
        reasoning=result.get("reasoning"),
        shopping_suggestions=result.get("shopping_suggestions", []),
        avatar_description=result.get("avatar_description"),
    )
