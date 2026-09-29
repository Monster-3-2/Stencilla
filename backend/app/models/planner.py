from datetime import datetime, timezone
from sqlalchemy import DateTime, Integer, String, Text, Boolean
from sqlalchemy.orm import Mapped, mapped_column
from app.db.session import Base


class StyleNote(Base):
    __tablename__ = "style_notes"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    user_id: Mapped[int] = mapped_column(Integer, index=True, nullable=False)
    title: Mapped[str] = mapped_column(String(120), nullable=False)
    body: Mapped[str | None] = mapped_column(Text, nullable=True)
    tags: Mapped[str | None] = mapped_column(String(300), nullable=True)  # comma-separated
    created_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc),
                                                  onupdate=lambda: datetime.now(timezone.utc))


class PlannerEvent(Base):
    __tablename__ = "planner_events"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    user_id: Mapped[int] = mapped_column(Integer, index=True, nullable=False)
    title: Mapped[str] = mapped_column(String(200), nullable=False)
    event_date: Mapped[str] = mapped_column(String(10), nullable=False)   # ISO date YYYY-MM-DD
    event_time: Mapped[str | None] = mapped_column(String(5), nullable=True)  # HH:MM
    occasion: Mapped[str | None] = mapped_column(String(60), nullable=True)   # e.g. "formal"
    dress_code: Mapped[str | None] = mapped_column(String(60), nullable=True)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)
    planned_outfit_ids: Mapped[str | None] = mapped_column(Text, nullable=True)  # comma-separated item IDs
    is_trip: Mapped[bool] = mapped_column(Boolean, default=False)
    trip_end_date: Mapped[str | None] = mapped_column(String(10), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc),
                                                  onupdate=lambda: datetime.now(timezone.utc))
