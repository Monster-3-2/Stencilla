from datetime import datetime, timezone

from sqlalchemy import DateTime, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.session import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)

    # --- Profile: drives outfit reasoning ---
    full_name: Mapped[str | None] = mapped_column(String(120), nullable=True)
    age: Mapped[int | None] = mapped_column(Integer, nullable=True)
    gender: Mapped[str | None] = mapped_column(String(30), nullable=True)
    lifestyle: Mapped[str | None] = mapped_column(String(40), nullable=True)
    # e.g. "college", "corporate_job", "business_owner", "creative_freelance", "general_casual"
    height_cm: Mapped[int | None] = mapped_column(Integer, nullable=True)
    body_type: Mapped[str | None] = mapped_column(String(40), nullable=True)
    # e.g. "slim", "athletic", "average", "broad", "plus_size"
    skin_tone: Mapped[str | None] = mapped_column(String(40), nullable=True)
    # e.g. "fair", "light", "medium", "olive", "tan", "deep", "dark" (Fitzpatrick-style swatch pick)
    style_goal: Mapped[str | None] = mapped_column(String(60), nullable=True)
    # e.g. "minimal_chic", "elevated_casual", "streetwear", "classic_formal"

    created_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc))
