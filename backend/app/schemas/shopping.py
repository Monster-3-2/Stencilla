from pydantic import BaseModel, Field, model_validator


class MeasurementProfile(BaseModel):
    unit: str = "cm"
    values: dict[str, float] = Field(default_factory=dict)

    @model_validator(mode="after")
    def valid_unit(self):
        if self.unit not in {"cm", "in"}:
            raise ValueError("measurement unit must be cm or in")
        if any(value <= 0 for value in self.values.values()):
            raise ValueError("measurements must be positive")
        return self


class ShoppingWardrobeItem(BaseModel):
    id: str
    category: str
    brand: str | None = None
    size: str | None = None
    available: bool = True
    duplicate_key: str | None = None


class RetailerProduct(BaseModel):
    retailer: str
    external_id: str
    name: str
    category: str
    url: str
    price: float = Field(ge=0)
    currency: str = "USD"
    regions: list[str] = Field(default_factory=list)
    sizes: list[str] = Field(default_factory=list)
    measurements: dict[str, float] = Field(default_factory=dict)
    available: bool = True
    duplicate_key: str | None = None


class ShoppingGapRequest(BaseModel):
    gaps: list[str] = Field(min_length=1, max_length=12)
    wardrobe_items: list[ShoppingWardrobeItem] = Field(default_factory=list)
    measurements: MeasurementProfile
    region: str = Field(min_length=2, max_length=40)
    size: str = Field(min_length=1, max_length=30)
    budget: float = Field(gt=0)
    retailers: list[RetailerProduct] = Field(min_length=1, max_length=500)
    measurement_tolerance: float = Field(default=2.0, gt=0, le=10)


class VerifiedShoppingRecommendation(BaseModel):
    gap: str
    retailer: str
    external_id: str
    name: str
    url: str
    price: float
    currency: str
    size: str
    measurement_match: bool
    verification: list[str]


class ShoppingGapResponse(BaseModel):
    recommendations: list[VerifiedShoppingRecommendation]
    rejected: dict[str, list[str]]
    verified_at: str
