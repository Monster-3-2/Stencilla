"""Central Groq AI service — all LLM/vision calls go through here."""
import base64
import json
import re
from groq import Groq
from app.core.config import settings

_client: Groq | None = None


def _groq() -> Groq:
    global _client
    if _client is None:
        _client = Groq(api_key=settings.GROQ_API_KEY)
    return _client


class AIServiceError(Exception):
    pass


def _extract_json(text: str) -> dict:
    text = text.strip()
    match = re.search(r"```(?:json)?\s*([\s\S]+?)\s*```", text)
    if match:
        text = match.group(1)
    start = text.find("{")
    end = text.rfind("}") + 1
    if start == -1 or end == 0:
        raise AIServiceError(f"No JSON found in response: {text[:200]}")
    try:
        return json.loads(text[start:end])
    except json.JSONDecodeError as e:
        raise AIServiceError(f"JSON parse error: {e}") from e


def tag_clothing_item(image_bytes: bytes, content_type: str = "image/jpeg") -> dict:
    b64 = base64.standard_b64encode(image_bytes).decode()
    prompt = (
        "You are a fashion AI. Analyze this clothing photo and return ONLY valid JSON (no markdown, no explanation) "
        "with these keys: category, subcategory, color_primary, color_secondary, pattern, formality, season, "
        "material, fit, needs_clarification (bool), clarification_question (or null), ai_image_description "
        "(short plain-language description of the item on a neutral background for display cards). "
        "formality must be one of: casual, smart_casual, business_casual, formal, athletic, loungewear. "
        "season must be one of: spring, summer, autumn, winter, all_season. "
        "fit must be one of: slim, regular, oversized, loose, fitted, relaxed."
    )
    try:
        resp = _groq().chat.completions.create(
            model=settings.GROQ_VISION_MODEL,
            messages=[{"role": "user", "content": [
                {"type": "image_url", "image_url": {"url": f"data:{content_type};base64,{b64}"}},
                {"type": "text", "text": prompt},
            ]}],
            max_tokens=512,
            temperature=0.1,
        )
        return _extract_json(resp.choices[0].message.content or "")
    except AIServiceError:
        raise
    except Exception as exc:
        raise AIServiceError(str(exc)) from exc


def generate_outfit(
    profile: dict,
    wardrobe_items: list[dict],
    occasion: str,
    dress_code: str | None = None,
    anchor_item: dict | None = None,
    notes: str | None = None,
    weather_temp: float | None = None,
    weather_condition: str | None = None,
    calendar_event: str | None = None,
    preference_summary: str | None = None,
) -> dict:
    context_parts = [f"Occasion: {occasion}"]
    if dress_code:
        context_parts.append(f"Dress code: {dress_code}")
    if weather_temp is not None:
        context_parts.append(f"Weather: {weather_temp}°C, {weather_condition or 'unknown'}")
    if calendar_event:
        context_parts.append(f"Calendar event: {calendar_event}")
    if notes:
        context_parts.append(f"User notes: {notes}")
    if preference_summary:
        context_parts.append(f"Preference history: {preference_summary}")
    if anchor_item:
        context_parts.append(f"Must include item: {json.dumps(anchor_item)}")

    wardrobe_json = json.dumps(wardrobe_items, ensure_ascii=False)
    profile_json = json.dumps({k: v for k, v in profile.items() if v is not None}, ensure_ascii=False)

    prompt = f"""You are Stencilla, an expert AI fashion stylist. Choose a complete outfit from the wardrobe.

USER PROFILE: {profile_json}

CONTEXT: {' | '.join(context_parts)}

WARDROBE (each item has an id and tags):
{wardrobe_json}

Return ONLY valid JSON with keys:
- item_ids: list of chosen item IDs (strings)
- reasoning: short explanation (1-2 sentences) of why this outfit works
- shopping_suggestions: list of {{item, reason}} for any gaps (max 3, empty list if wardrobe is complete)
- avatar_description: short text like "Wearing a cream linen blazer over a white tee, dark slim trousers and white sneakers" — describe the full look naturally
- factors: list of up to 5 {{label, score (0-100), explanation}} rating aspects like color harmony, occasion fit
- alternatives: list of up to 2 alternative outfit options as {{item_ids, label, reason}}

Only pick items where availability is "available" and laundry_state is "clean" unless no others exist."""

    try:
        resp = _groq().chat.completions.create(
            model=settings.GROQ_TEXT_MODEL,
            messages=[{"role": "user", "content": prompt}],
            max_tokens=1024,
            temperature=0.3,
        )
        return _extract_json(resp.choices[0].message.content or "")
    except AIServiceError:
        raise
    except Exception as exc:
        raise AIServiceError(str(exc)) from exc


def verify_outfit(image_bytes: bytes, content_type: str = "image/jpeg") -> dict:
    b64 = base64.standard_b64encode(image_bytes).decode()
    prompt = (
        "You are a fashion consultant. Analyze this outfit photo and return ONLY valid JSON with keys: "
        "fit_feedback (string), color_feedback (string), proportion_feedback (string), "
        "overall_feedback (string), cautions (list of strings, empty if none). "
        "Be concise, constructive and specific. No markdown."
    )
    try:
        resp = _groq().chat.completions.create(
            model=settings.GROQ_VISION_MODEL,
            messages=[{"role": "user", "content": [
                {"type": "image_url", "image_url": {"url": f"data:{content_type};base64,{b64}"}},
                {"type": "text", "text": prompt},
            ]}],
            max_tokens=512,
            temperature=0.2,
        )
        return _extract_json(resp.choices[0].message.content or "")
    except AIServiceError:
        raise
    except Exception as exc:
        raise AIServiceError(str(exc)) from exc


def generate_ai_wardrobe_suggestion(items: list[dict], style_goal: str | None, preferred_colors: str | None) -> str:
    """Generate a single actionable wardrobe insight sentence."""
    summary = json.dumps(items[:30], ensure_ascii=False)  # cap to keep tokens low
    ctx = []
    if style_goal:
        ctx.append(f"Style goal: {style_goal}")
    if preferred_colors:
        ctx.append(f"Preferred colors: {preferred_colors}")

    prompt = (
        f"Analyze this wardrobe summary and give ONE short, specific, actionable fashion tip "
        f"(max 20 words). Context: {', '.join(ctx) or 'none'}. "
        f"Wardrobe: {summary}. Return only the tip text, no JSON."
    )
    try:
        resp = _groq().chat.completions.create(
            model=settings.GROQ_TEXT_MODEL,
            messages=[{"role": "user", "content": prompt}],
            max_tokens=60,
            temperature=0.5,
        )
        return (resp.choices[0].message.content or "Build a capsule wardrobe with 3-4 neutral base pieces.").strip()
    except Exception:
        return "Add more versatile neutral pieces to unlock more outfit combinations."


def generate_outfit_for_event(
    event_title: str, occasion: str | None, dress_code: str | None, notes: str | None,
    wardrobe_items: list[dict],
) -> dict:
    """Plan an outfit specifically for a calendar event."""
    ctx = f"Event: {event_title}"
    if occasion:
        ctx += f" | Occasion type: {occasion}"
    if dress_code:
        ctx += f" | Dress code: {dress_code}"
    if notes:
        ctx += f" | Notes: {notes}"

    prompt = (
        f"You are Stencilla. Choose a complete outfit from this wardrobe for: {ctx}.\n"
        f"Wardrobe: {json.dumps(wardrobe_items[:40], ensure_ascii=False)}\n"
        "Return ONLY valid JSON with: item_ids (list), reasoning (string), avatar_description (string)."
    )
    try:
        resp = _groq().chat.completions.create(
            model=settings.GROQ_TEXT_MODEL,
            messages=[{"role": "user", "content": prompt}],
            max_tokens=400,
            temperature=0.3,
        )
        return _extract_json(resp.choices[0].message.content or "")
    except AIServiceError:
        raise
    except Exception as exc:
        raise AIServiceError(str(exc)) from exc
