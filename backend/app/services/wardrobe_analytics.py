"""Server-side wardrobe analytics. The client sends its locally-stored item tags
(no images), and we compute stats + smart categories + AI suggestion."""
from collections import Counter
from app.schemas.outfit import (
    WardrobeItemInput, WardrobeAnalyticsRequest, WardrobeAnalyticsResponse,
    CategoryCount, ColorEntry, SmartCategory,
)

# Mapping from tag values → smart category labels
_SMART_CATEGORY_RULES: dict[str, list[dict]] = {
    "Workwear": [
        {"formality": ["formal", "business_casual"]},
    ],
    "Casual": [
        {"formality": ["casual", "smart_casual"]},
    ],
    "Party": [
        {"formality": ["formal"]},
        {"subcategory_keywords": ["dress", "suit", "blazer", "gown", "jumpsuit"]},
    ],
    "Ethnic": [
        {"subcategory_keywords": ["saree", "kurta", "lehenga", "salwar", "dhoti", "sherwani",
                                   "churidar", "dupatta", "kameez", "anarkali", "indo-western"]},
    ],
    "Athletic": [
        {"formality": ["athletic"]},
        {"subcategory_keywords": ["track", "jogger", "gym", "sport", "yoga", "shorts", "jersey"]},
    ],
    "Lounge": [
        {"formality": ["loungewear"]},
    ],
}


def _item_in_smart_category(item: WardrobeItemInput, rules: list[dict]) -> bool:
    for rule in rules:
        if "formality" in rule:
            if item.formality and item.formality in rule["formality"]:
                return True
        if "subcategory_keywords" in rule:
            sub = (item.subcategory or "").lower()
            if any(kw in sub for kw in rule["subcategory_keywords"]):
                return True
    return False


def compute_analytics(req: WardrobeAnalyticsRequest) -> WardrobeAnalyticsResponse:
    items = req.items
    n = len(items)

    # Category breakdown
    cat_counter: Counter = Counter()
    for item in items:
        cat_counter[item.category or "Uncategorized"] += 1
    category_breakdown = [CategoryCount(category=k, count=v) for k, v in cat_counter.most_common()]

    # Top colors
    color_counter: Counter = Counter()
    for item in items:
        if item.color_primary:
            color_counter[item.color_primary] += 1
        if item.color_secondary:
            color_counter[item.color_secondary] += 1
    top_colors = [ColorEntry(color=k, count=v) for k, v in color_counter.most_common(6)]

    # Smart categories
    smart_cats: list[SmartCategory] = []
    for label, rules in _SMART_CATEGORY_RULES.items():
        matched = [i.id for i in items if _item_in_smart_category(i, rules)]
        if matched:
            smart_cats.append(SmartCategory(label=label, count=len(matched), item_ids=matched))

    # Utilization: items that have been marked as used or are available (client provides total vs outfit_count)
    util_pct = min(100, int((req.outfit_count / max(n, 1)) * 100)) if req.outfit_count > 0 else 0

    return WardrobeAnalyticsResponse(
        total_items=n,
        outfit_count=req.outfit_count,
        utilization_pct=util_pct,
        top_colors=top_colors,
        category_breakdown=category_breakdown,
        smart_categories=smart_cats,
        ai_suggestion="",   # filled in by the route using groq_service
    )


def search_items(items: list[WardrobeItemInput], query: str) -> list[WardrobeItemInput]:
    """Simple server-side text search over tag fields."""
    q = query.lower().strip()
    if not q:
        return items
    results = []
    for item in items:
        searchable = " ".join(filter(None, [
            item.category, item.subcategory, item.color_primary, item.color_secondary,
            item.pattern, item.formality, item.season, item.material, item.fit, item.brand,
        ])).lower()
        if q in searchable:
            results.append(item)
    return results


def filter_items(
    items: list[WardrobeItemInput],
    category: str | None = None,
    formality: str | None = None,
    season: str | None = None,
    color: str | None = None,
) -> list[WardrobeItemInput]:
    result = items
    if category:
        cat_lower = category.lower()
        # Map UI-friendly filter names to tag values
        category_map = {
            "tops": ["shirt", "top", "blouse", "t-shirt", "tee", "sweater", "hoodie", "tank"],
            "bottoms": ["jeans", "trousers", "pants", "shorts", "skirt", "leggings"],
            "dresses": ["dress", "gown", "jumpsuit", "romper"],
            "outers": ["jacket", "coat", "blazer", "cardigan", "vest", "hoodie"],
            "shoes": ["shoes", "sneakers", "boots", "sandals", "heels", "loafers", "flats"],
            "bags": ["bag", "backpack", "purse", "tote", "clutch", "wallet"],
            "accessories": ["belt", "scarf", "hat", "cap", "watch", "jewelry", "sunglasses"],
        }
        keywords = category_map.get(cat_lower, [cat_lower])
        result = [i for i in result if any(
            kw in (i.category or "").lower() or kw in (i.subcategory or "").lower()
            for kw in keywords
        )]
    if formality:
        result = [i for i in result if (i.formality or "").lower() == formality.lower()]
    if season:
        result = [i for i in result if (i.season or "").lower() in (season.lower(), "all_season")]
    if color:
        col = color.lower()
        result = [i for i in result if col in (i.color_primary or "").lower()
                  or col in (i.color_secondary or "").lower()]
    return result
