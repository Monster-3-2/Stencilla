from pydantic import BaseModel, EmailStr


class UserRegister(BaseModel):
    email: EmailStr
    password: str
    full_name: str | None = None


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


class ProfileUpdate(BaseModel):
    full_name: str | None = None
    age: int | None = None
    gender: str | None = None
    lifestyle: str | None = None
    height_cm: int | None = None
    body_type: str | None = None
    skin_tone: str | None = None
    style_goal: str | None = None
    preferred_colors: str | None = None
    preferred_fits: str | None = None
    preferred_silhouettes: str | None = None
    comfort_priority: str | None = None
    formality_preference: str | None = None
    modesty_preference: str | None = None
    style_inspirations: str | None = None
    confidence_signals: str | None = None


class ProfileResponse(ProfileUpdate):
    id: int
    email: EmailStr
    model_config = {"from_attributes": True}


class SettingsUpdate(BaseModel):
    notification_outfit: bool | None = None
    notification_planner: bool | None = None
    notification_tips: bool | None = None
    privacy_analytics: bool | None = None


class SettingsResponse(BaseModel):
    notification_outfit: bool
    notification_planner: bool
    notification_tips: bool
    privacy_analytics: bool
    model_config = {"from_attributes": True}
