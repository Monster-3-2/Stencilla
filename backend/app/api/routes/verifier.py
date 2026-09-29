from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.outfit import OutfitVerificationResponse
from app.services.groq_service import AIServiceError, verify_outfit

MAX_IMAGE_BYTES = 10 * 1024 * 1024
ALLOWED = {"image/jpeg", "image/png", "image/webp"}
SIGS = {"image/jpeg": (b"\xff\xd8\xff",), "image/png": (b"\x89PNG\r\n\x1a\n",), "image/webp": (b"RIFF",)}
router = APIRouter(prefix="/verifier", tags=["verifier"])


@router.post("/analyze", response_model=OutfitVerificationResponse)
async def analyze_outfit(
    image: UploadFile = File(...),
    consent: bool = Form(False),
    retain_on_server: bool = Form(False),
    current_user: User = Depends(get_current_user),
):
    if not consent:
        raise HTTPException(status_code=400, detail="Explicit consent is required")
    if retain_on_server:
        raise HTTPException(status_code=400, detail="Server retention is not supported")
    ct = (image.content_type or "").lower()
    if ct not in ALLOWED:
        raise HTTPException(status_code=415, detail="Only JPEG, PNG, and WebP images are supported")
    image_bytes = await image.read(MAX_IMAGE_BYTES + 1)
    if len(image_bytes) > MAX_IMAGE_BYTES:
        raise HTTPException(status_code=413, detail="Image too large (max 10 MB)")
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Empty image")
    if not any(image_bytes.startswith(s) for s in SIGS[ct]):
        raise HTTPException(status_code=415, detail="Image content does not match declared type")
    try:
        return OutfitVerificationResponse(**verify_outfit(image_bytes, content_type=ct))
    except (AIServiceError, ValueError, TypeError) as exc:
        raise HTTPException(status_code=502, detail="Outfit analysis unavailable") from exc
    finally:
        await image.close()
