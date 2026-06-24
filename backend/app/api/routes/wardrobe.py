from fastapi import APIRouter, Depends, File, HTTPException, UploadFile

from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.outfit import ClothingTagResponse
from app.services.groq_service import AIServiceError, tag_clothing_item

router = APIRouter(prefix="/wardrobe", tags=["wardrobe"])


@router.post("/tag", response_model=ClothingTagResponse)
async def tag_item(
    image: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
):
    """Stateless tagging: the client uploads a photo, gets structured tags back, and stores
    both the image and the tags locally. Nothing is persisted on the server - there is no
    server-side wardrobe table. This keeps the backend simple and means clothing photos never
    leave the user's device except for this one momentary classification call.
    """
    image_bytes = await image.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Empty image upload")

    try:
        tags = tag_clothing_item(image_bytes, content_type=image.content_type or "image/jpeg")
    except AIServiceError as exc:
        raise HTTPException(status_code=502, detail=f"Tagging AI failed: {exc}") from exc

    return ClothingTagResponse(**tags)
