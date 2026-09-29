from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    APP_NAME: str = "Stencilla"
    JWT_SECRET: str = "dev-secret-change-in-prod"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 30  # 30 days

    TURSO_DATABASE_URL: str = ""
    TURSO_AUTH_TOKEN: str = ""

    GROQ_API_KEY: str = ""
    GROQ_VISION_MODEL: str = "meta-llama/llama-4-scout-17b-16e-instruct"
    GROQ_TEXT_MODEL: str = "llama-3.3-70b-versatile"

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
