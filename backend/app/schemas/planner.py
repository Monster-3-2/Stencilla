from pydantic import BaseModel, Field


class StyleNoteCreate(BaseModel):
    title: str = Field(max_length=120)
    body: str | None = None
    tags: str | None = None   # comma-separated, e.g. "minimal,summer"


class StyleNoteUpdate(BaseModel):
    title: str | None = Field(default=None, max_length=120)
    body: str | None = None
    tags: str | None = None


class StyleNoteResponse(BaseModel):
    id: int
    title: str
    body: str | None
    tags: str | None
    created_at: str
    updated_at: str
    model_config = {"from_attributes": True}


class PlannerEventCreate(BaseModel):
    title: str = Field(max_length=200)
    event_date: str   # YYYY-MM-DD
    event_time: str | None = None   # HH:MM
    occasion: str | None = None
    dress_code: str | None = None
    notes: str | None = None
    planned_outfit_ids: str | None = None
    is_trip: bool = False
    trip_end_date: str | None = None


class PlannerEventUpdate(BaseModel):
    title: str | None = Field(default=None, max_length=200)
    event_date: str | None = None
    event_time: str | None = None
    occasion: str | None = None
    dress_code: str | None = None
    notes: str | None = None
    planned_outfit_ids: str | None = None
    is_trip: bool | None = None
    trip_end_date: str | None = None


class PlannerEventResponse(BaseModel):
    id: int
    title: str
    event_date: str
    event_time: str | None
    occasion: str | None
    dress_code: str | None
    notes: str | None
    planned_outfit_ids: str | None
    is_trip: bool
    trip_end_date: str | None
    created_at: str
    updated_at: str
    model_config = {"from_attributes": True}


class OutfitForEventRequest(BaseModel):
    event_id: int
    wardrobe_items: list[dict]   # WardrobeItemInput-compatible dicts


class OutfitForEventResponse(BaseModel):
    item_ids: list[str]
    reasoning: str | None
    avatar_description: str | None = None
