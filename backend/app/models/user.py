from datetime import datetime, timezone
from sqlalchemy import Boolean, DateTime, Integer, String
from sqlalchemy.orm import Mapped, mapped_column
from app.db.session import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)

    full_name: Mapped[str | None] = mapped_column(String(120), nullable=True)
    age: Mapped[int | None] = mapped_column(Integer, nullable=True)
    gender: Mapped[str | None] = mapped_column(String(30), nullable=True)
    lifestyle: Mapped[str | None] = mapped_column(String(40), nullable=True)
    height_cm: Mapped[int | None] = mapped_column(Integer, nullable=True)
    body_type: Mapped[str | None] = mapped_column(String(40), nullable=True)
    skin_tone: Mapped[str | None] = mapped_column(String(40), nullable=True)
    style_goal: Mapped[str | None] = mapped_column(String(60), nullable=True)
    preferred_colors: Mapped[str | None] = mapped_column(String(300), nullable=True)
    preferred_fits: Mapped[str | None] = mapped_column(String(120), nullable=True)
    preferred_silhouettes: Mapped[str | None] = mapped_column(String(120), nullable=True)
    comfort_priority: Mapped[str | None] = mapped_column(String(40), nullable=True)
    formality_preference: Mapped[str | None] = mapped_column(String(40), nullable=True)
    modesty_preference: Mapped[str | None] = mapped_column(String(40), nullable=True)
    style_inspirations: Mapped[str | None] = mapped_column(String(500), nullable=True)
    confidence_signals: Mapped[str | None] = mapped_column(String(500), nullable=True)

    # Settings
    notification_outfit: Mapped[bool] = mapped_column(Boolean, default=True)
    notification_planner: Mapped[bool] = mapped_column(Boolean, default=True)
    notification_tips: Mapped[bool] = mapped_column(Boolean, default=True)
    privacy_analytics: Mapped[bool] = mapped_column(Boolean, default=True)

    created_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc))
