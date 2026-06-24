from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes import auth, outfits, users, wardrobe
from app.core.config import settings
from app.db.session import Base, engine

# Must be imported so SQLAlchemy registers the table before create_all runs.
from app.models import user  # noqa: F401


@asynccontextmanager
async def lifespan(app: FastAPI):
    # MVP uses create_all instead of migrations for speed; switch to Alembic before
    # making breaking schema changes in production.
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(title=settings.APP_NAME, lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # the Android app calls this API directly, not a browser
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health_check():
    return {"status": "ok", "app": settings.APP_NAME}


app.include_router(auth.router)
app.include_router(users.router)
app.include_router(wardrobe.router)
app.include_router(outfits.router)
