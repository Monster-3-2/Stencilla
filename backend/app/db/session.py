from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker

from app.core.config import settings


def _build_engine():
    if settings.TURSO_DATABASE_URL and settings.TURSO_AUTH_TOKEN:
        # Accept the URL whether or not the user pasted the "libsql://" prefix from the
        # Turso dashboard - strip it if present, since the dialect prefix below supplies it.
        host = settings.TURSO_DATABASE_URL.removeprefix("libsql://")
        url = f"sqlite+libsql://{host}?secure=true"
        return create_engine(url, connect_args={"auth_token": settings.TURSO_AUTH_TOKEN})

    # Local dev fallback: plain SQLite file, no remote credentials needed.
    connect_args = {"check_same_thread": False} if settings.DATABASE_URL.startswith("sqlite") else {}
    return create_engine(settings.DATABASE_URL, connect_args=connect_args)


engine = _build_engine()
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass


def get_db() -> Session:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
