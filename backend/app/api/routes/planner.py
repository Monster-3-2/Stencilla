import datetime
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.models.planner import PlannerEvent
from app.schemas.planner import (
    PlannerEventCreate, PlannerEventResponse, PlannerEventUpdate,
    OutfitForEventRequest, OutfitForEventResponse,
)
from app.schemas.outfit import WardrobeItemInput
from app.services.groq_service import AIServiceError, generate_outfit_for_event

router = APIRouter(prefix="/planner", tags=["planner"])


def _fmt(dt: datetime.datetime) -> str:
    return dt.isoformat() if dt else ""


def _to_response(e: PlannerEvent) -> PlannerEventResponse:
    return PlannerEventResponse(
        id=e.id, title=e.title, event_date=e.event_date, event_time=e.event_time,
        occasion=e.occasion, dress_code=e.dress_code, notes=e.notes,
        planned_outfit_ids=e.planned_outfit_ids, is_trip=e.is_trip,
        trip_end_date=e.trip_end_date, created_at=_fmt(e.created_at), updated_at=_fmt(e.updated_at),
    )


@router.get("/events", response_model=list[PlannerEventResponse])
def list_events(
    from_date: str | None = Query(default=None, description="YYYY-MM-DD"),
    to_date: str | None = Query(default=None, description="YYYY-MM-DD"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    q = db.query(PlannerEvent).filter(PlannerEvent.user_id == current_user.id)
    if from_date:
        q = q.filter(PlannerEvent.event_date >= from_date)
    if to_date:
        q = q.filter(PlannerEvent.event_date <= to_date)
    events = q.order_by(PlannerEvent.event_date.asc(), PlannerEvent.event_time.asc()).all()
    return [_to_response(e) for e in events]


@router.get("/events/upcoming", response_model=list[PlannerEventResponse])
def upcoming_events(
    days: int = Query(default=7, ge=1, le=30),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    today = datetime.date.today().isoformat()
    limit_date = (datetime.date.today() + datetime.timedelta(days=days)).isoformat()
    events = db.query(PlannerEvent).filter(
        PlannerEvent.user_id == current_user.id,
        PlannerEvent.event_date >= today,
        PlannerEvent.event_date <= limit_date,
    ).order_by(PlannerEvent.event_date.asc(), PlannerEvent.event_time.asc()).all()
    return [_to_response(e) for e in events]


@router.get("/calendar", response_model=list[PlannerEventResponse])
def calendar_strip(
    year: int = Query(...),
    month: int = Query(..., ge=1, le=12),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Returns all events for a given month — used to populate the outfit calendar strip."""
    from_date = f"{year:04d}-{month:02d}-01"
    import calendar
    last_day = calendar.monthrange(year, month)[1]
    to_date = f"{year:04d}-{month:02d}-{last_day:02d}"
    events = db.query(PlannerEvent).filter(
        PlannerEvent.user_id == current_user.id,
        PlannerEvent.event_date >= from_date,
        PlannerEvent.event_date <= to_date,
    ).order_by(PlannerEvent.event_date.asc()).all()
    return [_to_response(e) for e in events]


@router.post("/events", response_model=PlannerEventResponse, status_code=201)
def create_event(
    payload: PlannerEventCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    event = PlannerEvent(
        user_id=current_user.id, title=payload.title, event_date=payload.event_date,
        event_time=payload.event_time, occasion=payload.occasion, dress_code=payload.dress_code,
        notes=payload.notes, planned_outfit_ids=payload.planned_outfit_ids,
        is_trip=payload.is_trip, trip_end_date=payload.trip_end_date,
    )
    db.add(event)
    db.commit()
    db.refresh(event)
    return _to_response(event)


@router.put("/events/{event_id}", response_model=PlannerEventResponse)
def update_event(
    event_id: int,
    payload: PlannerEventUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    event = db.query(PlannerEvent).filter(
        PlannerEvent.id == event_id, PlannerEvent.user_id == current_user.id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Event not found")
    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(event, field, value)
    event.updated_at = datetime.datetime.now(datetime.timezone.utc)
    db.commit()
    db.refresh(event)
    return _to_response(event)


@router.delete("/events/{event_id}", status_code=204)
def delete_event(
    event_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    event = db.query(PlannerEvent).filter(
        PlannerEvent.id == event_id, PlannerEvent.user_id == current_user.id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Event not found")
    db.delete(event)
    db.commit()


@router.post("/events/{event_id}/outfit", response_model=OutfitForEventResponse)
def outfit_for_event(
    event_id: int,
    payload: OutfitForEventRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """AI outfit suggestion tailored to a specific planned event.
    The event's title, occasion, dress_code, and notes are all passed to the AI as context,
    so future outfit suggestions for events will automatically take schedule into account."""
    event = db.query(PlannerEvent).filter(
        PlannerEvent.id == event_id, PlannerEvent.user_id == current_user.id).first()
    if not event:
        raise HTTPException(status_code=404, detail="Event not found")

    wardrobe_dicts = [WardrobeItemInput(**item).model_dump() for item in payload.wardrobe_items]

    try:
        result = generate_outfit_for_event(
            event_title=event.title,
            occasion=event.occasion,
            dress_code=event.dress_code,
            notes=event.notes,
            wardrobe_items=wardrobe_dicts,
        )
    except AIServiceError as exc:
        raise HTTPException(status_code=502, detail="AI outfit planning unavailable") from exc

    # Save chosen outfit IDs back to the event so the calendar strip can show them
    valid_ids = {item["id"] for item in wardrobe_dicts}
    chosen = [i for i in result.get("item_ids", []) if i in valid_ids]
    event.planned_outfit_ids = ",".join(chosen)
    event.updated_at = datetime.datetime.now(datetime.timezone.utc)
    db.commit()

    return OutfitForEventResponse(
        item_ids=chosen,
        reasoning=result.get("reasoning"),
        avatar_description=result.get("avatar_description"),
    )
