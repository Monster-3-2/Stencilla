from pydantic import BaseModel


class WardrobeItemInput(BaseModel):
    id: str
    category: str | None = None
    subcategory: str | None = None
    color_primary: str | None = None
    color_secondary: str | None = None
    pattern: str | None = None
    formality: str | None = None
    season: str | None = None
    material: str | None = None
    fit: str | None = None


class OutfitRequest(BaseModel):
    occasion: str
    wardrobe_items: list[WardrobeItemInput]
    anchor_item_id: str | None = None
    notes: str | None = None


class ShoppingSuggestion(BaseModel):
    item: str
    reason: str


class OutfitResponse(BaseModel):
    occasion: str
    item_ids: list[str]
    reasoning: str | None
    shopping_suggestions: list[ShoppingSuggestion]
    avatar_description: str | None = None


class ClothingTagResponse(BaseModel):
    category: str | None = None
    subcategory: str | None = None
    color_primary: str | None = None
    color_secondary: str | None = None
    pattern: str | None = None
    formality: str | None = None
    season: str | None = None
    material: str | None = None
    fit: str | None = None
    needs_clarification: bool = False
    clarification_question: str | None = None
    ai_image_description: str | None = None
