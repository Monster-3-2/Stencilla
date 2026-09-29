from fastapi import APIRouter, Depends
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.shopping import ShoppingGapRequest, ShoppingGapResponse
from app.services.shopping_service import verify_shopping_gaps

router = APIRouter(prefix="/shopping", tags=["shopping"])


@router.post("/gaps", response_model=ShoppingGapResponse)
def shopping_gaps(payload: ShoppingGapRequest, current_user: User = Depends(get_current_user)):
    return verify_shopping_gaps(payload)
