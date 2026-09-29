from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import (
    auth, context, outfits, shopping, users,
    verifier, wardrobe, stylenotes, planner, settings as settings_router,
)
from app.core.config import settings
from app.db.session import Base, engine
from app.models import user  # noqa: F401


def _ensure_columns() -> None:
    from sqlalchemy import inspect, text
    inspector = inspect(engine)

    if "users" in inspector.get_table_names():
        existing_u = {c["name"] for c in inspector.get_columns("users")}
        user_cols = {
            "preferred_colors": "VARCHAR(300)", "preferred_fits": "VARCHAR(120)",
            "preferred_silhouettes": "VARCHAR(120)", "comfort_priority": "VARCHAR(40)",
            "formality_preference": "VARCHAR(40)", "modesty_preference": "VARCHAR(40)",
            "style_inspirations": "VARCHAR(500)", "confidence_signals": "VARCHAR(500)",
            "notification_outfit": "INTEGER DEFAULT 1",
            "notification_planner": "INTEGER DEFAULT 1",
            "notification_tips": "INTEGER DEFAULT 1",
            "privacy_analytics": "INTEGER DEFAULT 1",
        }
        with engine.begin() as conn:
            for name, type_ in user_cols.items():
                if name not in existing_u:
                    conn.execute(text(f"ALTER TABLE users ADD COLUMN {name} {type_}"))

    if "style_notes" in inspector.get_table_names():
        existing_sn = {c["name"] for c in inspector.get_columns("style_notes")}
    # tables created via create_all

    if "planner_events" in inspector.get_table_names():
        pass  # created via create_all


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    _ensure_columns()
    yield


app = FastAPI(title=settings.APP_NAME, lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[],
    allow_methods=[],
    allow_headers=[],
)


@app.get("/health")
def health_check():
    return {"status": "ok", "app": settings.APP_NAME}


app.include_router(auth.router)
app.include_router(users.router)
app.include_router(wardrobe.router)
app.include_router(outfits.router)
app.include_router(context.router)
app.include_router(verifier.router)
app.include_router(shopping.router)
app.include_router(stylenotes.router)
app.include_router(planner.router)
app.include_router(settings_router.router)
