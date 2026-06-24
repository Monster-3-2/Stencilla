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


class ProfileResponse(ProfileUpdate):
    id: int
    email: EmailStr

    model_config = {"from_attributes": True}
