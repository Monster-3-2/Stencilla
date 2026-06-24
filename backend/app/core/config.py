from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # --- Core ---
    APP_NAME: str = "Stencilla API"
    ENVIRONMENT: str = "development"
    JWT_SECRET: str = "change-me-in-a-real-deployment"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 14  # 14 days

    # --- Database ---
    # Local dev default: a plain SQLite file, no setup needed.
    DATABASE_URL: str = "sqlite:///./stencilla.db"
    # Production: set these two to use Turso instead. When both are present, they take
    # priority over DATABASE_URL (see db/session.py).
    TURSO_DATABASE_URL: str = ""  # e.g. "your-db-name-org.turso.io" (with or without "libsql://")
    TURSO_AUTH_TOKEN: str = ""

    # --- Groq (cloud AI reasoning: vision tagging + outfit generation) ---
    GROQ_API_KEY: str = ""
    GROQ_VISION_MODEL: str = "meta-llama/llama-4-scout-17b-16e-instruct"
    GROQ_TEXT_MODEL: str = "llama-3.3-70b-versatile"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
