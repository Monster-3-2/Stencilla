from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.models.planner import StyleNote
from app.schemas.planner import StyleNoteCreate, StyleNoteResponse, StyleNoteUpdate
import datetime

router = APIRouter(prefix="/stylenotes", tags=["stylenotes"])


def _fmt(dt: datetime.datetime) -> str:
    return dt.isoformat() if dt else ""


@router.get("", response_model=list[StyleNoteResponse])
def list_notes(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    notes = db.query(StyleNote).filter(StyleNote.user_id == current_user.id).order_by(
        StyleNote.updated_at.desc()).all()
    return [StyleNoteResponse(id=n.id, title=n.title, body=n.body, tags=n.tags,
                               created_at=_fmt(n.created_at), updated_at=_fmt(n.updated_at)) for n in notes]


@router.post("", response_model=StyleNoteResponse, status_code=201)
def create_note(
    payload: StyleNoteCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    note = StyleNote(user_id=current_user.id, title=payload.title, body=payload.body, tags=payload.tags)
    db.add(note)
    db.commit()
    db.refresh(note)
    return StyleNoteResponse(id=note.id, title=note.title, body=note.body, tags=note.tags,
                              created_at=_fmt(note.created_at), updated_at=_fmt(note.updated_at))


@router.put("/{note_id}", response_model=StyleNoteResponse)
def update_note(
    note_id: int,
    payload: StyleNoteUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    note = db.query(StyleNote).filter(StyleNote.id == note_id, StyleNote.user_id == current_user.id).first()
    if not note:
        raise HTTPException(status_code=404, detail="Note not found")
    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(note, field, value)
    note.updated_at = datetime.datetime.now(datetime.timezone.utc)
    db.commit()
    db.refresh(note)
    return StyleNoteResponse(id=note.id, title=note.title, body=note.body, tags=note.tags,
                              created_at=_fmt(note.created_at), updated_at=_fmt(note.updated_at))


@router.delete("/{note_id}", status_code=204)
def delete_note(
    note_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    note = db.query(StyleNote).filter(StyleNote.id == note_id, StyleNote.user_id == current_user.id).first()
    if not note:
        raise HTTPException(status_code=404, detail="Note not found")
    db.delete(note)
    db.commit()
