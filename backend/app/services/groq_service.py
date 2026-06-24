import base64
import json

from groq import Groq

from app.core.config import settings

_client = Groq(api_key=settings.GROQ_API_KEY)

VALID_CATEGORIES = [
    "shirt", "tshirt", "jacket", "coat", "sweater", "jeans", "trousers",
    "shorts", "dress", "skirt", "shoes", "bag", "watch", "belt", "accessory", "other",
]
VALID_FORMALITY = ["casual", "smart_casual", "business", "formal", "party"]
VALID_MATERIALS = [
    "cotton", "polyester", "wool", "linen", "denim", "leather", "silk",
    "synthetic", "knit", "velvet", "unknown",
]
VALID_FIT = ["slim", "regular", "oversized", "relaxed", "skinny", "tailored", "unknown"]


class AIServiceError(Exception):
    pass


def _parse_json(raw_text: str) -> dict:
    try:
        return json.loads(raw_text)
    except json.JSONDecodeError as exc:
        raise AIServiceError(f"Non-JSON response: {raw_text[:200]}") from exc


def tag_clothing_item(image_bytes: bytes, content_type: str = "image/jpeg") -> dict:
    """Vision tagging. Returns structured tags + clarification question if needed
    + an AI image description for rendering the item on a plain background."""
    b64_data = base64.standard_b64encode(image_bytes).decode("utf-8")
    data_uri = f"data:{content_type};base64,{b64_data}"

    prompt = (
        "You are an expert wardrobe cataloguer for a personal styling app. "
        "Analyse this clothing/accessory photo carefully.\n\n"
        "Respond ONLY with raw JSON matching this exact shape:\n"
        "{\n"
        f'  "category": one of {json.dumps(VALID_CATEGORIES)},\n'
        '  "subcategory": "precise descriptive phrase e.g. \'slim-fit Oxford shirt\'",\n'
        '  "color_primary": "single dominant color word",\n'
        '  "color_secondary": "second color or null",\n'
        '  "pattern": "solid / striped / checked / floral / printed / textured / graphic",\n'
        f'  "formality": one of {json.dumps(VALID_FORMALITY)},\n'
        '  "season": "all_season / summer / winter / monsoon / transitional",\n'
        f'  "material": one of {json.dumps(VALID_MATERIALS)} — guess from visual texture, use "unknown" only if truly impossible,\n'
        f'  "fit": one of {json.dumps(VALID_FIT)},\n'
        '  "needs_clarification": true or false — true only if material OR fit is "unknown" and cannot be inferred,\n'
        '  "clarification_question": "One short friendly question to ask the user about what you could not determine, or null if needs_clarification is false",\n'
        '  "ai_image_description": "A precise, detailed description of this exact item as it would look laid flat on a pure white background, for use as an AI image generation prompt. Include color, material, pattern, style details. E.g. \'A slim-fit white cotton Oxford shirt with a button-down collar, fine vertical pinstripes, and a chest pocket, laid flat on pure white background, product photography style\'"\n'
        "}"
    )

    response = _client.chat.completions.create(
        model=settings.GROQ_VISION_MODEL,
        max_completion_tokens=600,
        response_format={"type": "json_object"},
        messages=[{
            "role": "user",
            "content": [
                {"type": "text", "text": prompt},
                {"type": "image_url", "image_url": {"url": data_uri}},
            ],
        }],
    )
    return _parse_json(response.choices[0].message.content)


def generate_outfit(
    profile: dict,
    wardrobe_items: list[dict],
    occasion: str,
    anchor_item: dict | None,
    notes: str | None,
) -> dict:
    system_prompt = (
        "You are Stencilla, an expert personal stylist. Build outfits strictly from the "
        "user's wardrobe items (JSON list with string ids), tailored to body type, skin tone, "
        "age, lifestyle and style goal. Only recommend genuinely flattering, occasion-appropriate "
        "combinations. Suggest up to 3 specific items to buy if the wardrobe has a gap. "
        "Also produce an avatar_description: a single paragraph describing the full outfit on a "
        "stylish human figure — describe the person wearing it head to toe, written as a prompt "
        "for a 3D fashion illustration. "
        "Respond with raw JSON only:\n"
        '{"item_ids": [list of string ids], '
        '"reasoning": "2-4 warm, specific sentences explaining why this works for THIS person", '
        '"shopping_suggestions": [{"item": "...", "reason": "..."}], '
        '"avatar_description": "Full outfit on a stylish figure, e.g. \'A tall athletic man in a '
        'white Oxford shirt tucked into navy slim-fit chinos, brown leather belt, clean white '
        'sneakers. Confident, minimal chic. Studio lighting, plain off-white background.\'"}'
    )

    user_prompt = (
        f"User profile: {json.dumps(profile)}\n\n"
        f"Wardrobe: {json.dumps(wardrobe_items)}\n\n"
        f"Occasion: {occasion}\n"
        f"Anchor item: {json.dumps(anchor_item) if anchor_item else 'none'}\n"
        f"Notes: {notes or 'none'}"
    )

    response = _client.chat.completions.create(
        model=settings.GROQ_TEXT_MODEL,
        max_completion_tokens=1000,
        response_format={"type": "json_object"},
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
    )
    return _parse_json(response.choices[0].message.content)
