from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker
from app.core.config import settings

if settings.TURSO_DATABASE_URL:
    url = f"sqlite+libsql://{settings.TURSO_DATABASE_URL}?authToken={settings.TURSO_AUTH_TOKEN}&secure=true"
else:
    url = "sqlite:///./stencilla_dev.db"

engine = create_engine(url, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
