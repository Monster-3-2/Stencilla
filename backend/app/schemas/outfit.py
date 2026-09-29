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
    brand: str | None = None
    size: str | None = None
    condition: str | None = None
    availability: str | None = None
    laundry_state: str | None = None
    repair_note: str | None = None


class OutfitRequest(BaseModel):
    occasion: str
    dress_code: str | None = None
    wardrobe_items: list[WardrobeItemInput]
    anchor_item_id: str | None = None
    notes: str | None = None
    weather_temp: float | None = None
    weather_condition: str | None = None
    calendar_event: str | None = None
    preference_summary: str | None = None


class ShoppingSuggestion(BaseModel):
    item: str
    reason: str


class OutfitFactor(BaseModel):
    label: str
    score: int
    explanation: str


class OutfitAlternative(BaseModel):
    item_ids: list[str]
    label: str
    reason: str


class OutfitResponse(BaseModel):
    occasion: str
    item_ids: list[str]
    reasoning: str | None
    shopping_suggestions: list[ShoppingSuggestion]
    avatar_description: str | None = None
    factors: list[OutfitFactor] = []
    alternatives: list[OutfitAlternative] = []


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


class OutfitVerificationResponse(BaseModel):
    fit_feedback: str
    color_feedback: str
    proportion_feedback: str
    overall_feedback: str
    cautions: list[str] = []


# Wardrobe analytics (computed server-side from client-sent tag data)
class WardrobeAnalyticsRequest(BaseModel):
    items: list[WardrobeItemInput]
    outfit_count: int = 0
    total_items: int = 0


class CategoryCount(BaseModel):
    category: str
    count: int


class ColorEntry(BaseModel):
    color: str
    count: int


class SmartCategory(BaseModel):
    label: str       # "Workwear", "Casual", "Party", "Ethnic", "Sport"
    count: int
    item_ids: list[str]


class WardrobeAnalyticsResponse(BaseModel):
    total_items: int
    outfit_count: int
    utilization_pct: int          # 0-100
    top_colors: list[ColorEntry]
    category_breakdown: list[CategoryCount]
    smart_categories: list[SmartCategory]
    ai_suggestion: str            # One AI-generated insight sentence


class AiSuggestionRequest(BaseModel):
    items: list[WardrobeItemInput]
    style_goal: str | None = None
    preferred_colors: str | None = None
