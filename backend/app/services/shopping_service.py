from datetime import datetime, timezone
from app.schemas.shopping import ShoppingGapRequest, ShoppingGapResponse, VerifiedShoppingRecommendation


def verify_shopping_gaps(payload: ShoppingGapRequest) -> ShoppingGapResponse:
    recommendations: list[VerifiedShoppingRecommendation] = []
    rejected: dict[str, list[str]] = {gap: [] for gap in payload.gaps}

    existing_categories = {item.category.lower() for item in payload.wardrobe_items if item.available}
    existing_brands = {(item.brand or "").lower() for item in payload.wardrobe_items}
    existing_dup_keys = {item.duplicate_key for item in payload.wardrobe_items if item.duplicate_key}

    for gap in payload.gaps:
        gap_lower = gap.lower()
        filled = False
        for product in payload.retailers:
            reasons: list[str] = []
            if not product.available:
                reasons.append("out_of_stock")
            if product.price > payload.budget:
                reasons.append(f"over_budget:{product.price:.2f}>{payload.budget:.2f}")
            if payload.region and product.regions and payload.region not in product.regions:
                reasons.append(f"region_not_served:{payload.region}")
            if payload.size and product.sizes and payload.size not in product.sizes:
                reasons.append(f"size_unavailable:{payload.size}")
            if product.duplicate_key and product.duplicate_key in existing_dup_keys:
                reasons.append("duplicate_in_wardrobe")
            if product.category.lower() in existing_categories and gap_lower not in product.category.lower():
                reasons.append("category_already_covered")

            measurement_match = True
            if payload.measurements.values and product.measurements:
                for key, target in payload.measurements.values.items():
                    prod_val = product.measurements.get(key)
                    if prod_val is not None and abs(prod_val - target) > payload.measurement_tolerance:
                        measurement_match = False
                        reasons.append(f"measurement_mismatch:{key}")
                        break

            if reasons:
                rejected[gap] = rejected.get(gap, []) + reasons
                continue

            if not filled and (gap_lower in product.category.lower() or gap_lower in product.name.lower()):
                recommendations.append(VerifiedShoppingRecommendation(
                    gap=gap, retailer=product.retailer, external_id=product.external_id,
                    name=product.name, url=product.url, price=product.price, currency=product.currency,
                    size=payload.size, measurement_match=measurement_match,
                    verification=["price_ok", "size_available", "region_ok"],
                ))
                filled = True

    return ShoppingGapResponse(
        recommendations=recommendations,
        rejected={k: list(set(v)) for k, v in rejected.items()},
        verified_at=datetime.now(timezone.utc).isoformat(),
    )
