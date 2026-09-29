package com.stencilla.app.data

import com.stencilla.app.ui.components.ChipOption

object StyleOptions {

    val genders = listOf(
        ChipOption("male", "Male"),
        ChipOption("female", "Female"),
        ChipOption("non_binary", "Non-binary"),
        ChipOption("prefer_not_to_say", "Prefer not to say"),
    )

    val lifestyles = listOf(
        ChipOption("college", "College"),
        ChipOption("corporate_job", "Corporate job"),
        ChipOption("business_owner", "Business owner"),
        ChipOption("creative_freelance", "Creative / freelance"),
        ChipOption("general_casual", "General / casual"),
    )

    val bodyTypes = listOf(
        ChipOption("slim", "Slim"),
        ChipOption("athletic", "Athletic"),
        ChipOption("average", "Average"),
        ChipOption("broad", "Broad"),
        ChipOption("plus_size", "Plus size"),
    )

    val skinTones = listOf(
        ChipOption("fair", "Fair"),
        ChipOption("light", "Light"),
        ChipOption("medium", "Medium"),
        ChipOption("olive", "Olive"),
        ChipOption("tan", "Tan"),
        ChipOption("deep", "Deep"),
        ChipOption("dark", "Dark"),
    )

    val styleGoals = listOf(
        ChipOption("minimal_chic", "Minimal chic"),
        ChipOption("elevated_casual", "Elevated casual"),
        ChipOption("streetwear", "Streetwear"),
        ChipOption("classic_formal", "Classic formal"),
    )
    val preferredFits = listOf(ChipOption("slim", "Slim"), ChipOption("regular", "Regular"), ChipOption("relaxed", "Relaxed"), ChipOption("oversized", "Oversized"), ChipOption("tailored", "Tailored"))
    val silhouettes = listOf(ChipOption("structured", "Structured"), ChipOption("straight", "Straight"), ChipOption("flowing", "Flowing"), ChipOption("layered", "Layered"), ChipOption("minimal", "Minimal"))
    val comfortPriorities = listOf(ChipOption("low", "Style first"), ChipOption("balanced", "Balanced"), ChipOption("high", "Comfort first"))
    val formalityPreferences = listOf(ChipOption("casual", "Casual"), ChipOption("smart_casual", "Smart casual"), ChipOption("polished", "Polished"), ChipOption("formal", "Formal"))
    val modestyPreferences = listOf(ChipOption("open", "More open"), ChipOption("balanced", "Balanced"), ChipOption("covered", "More covered"))

    val occasions = listOf(
        ChipOption("casual", "Casual"),
        ChipOption("work", "Work"),
        ChipOption("business", "Business"),
        ChipOption("interview", "Interview"),
        ChipOption("formal", "Formal"),
        ChipOption("wedding", "Wedding"),
        ChipOption("party", "Party"),
        ChipOption("date", "Date"),
        ChipOption("travel", "Travel"),
        ChipOption("college", "College"),
        ChipOption("date_night", "Date night"),
    )

    val dressCodes = listOf(
        ChipOption("unspecified", "No preference"),
        ChipOption("casual", "Casual"),
        ChipOption("smart_casual", "Smart casual"),
        ChipOption("business_casual", "Business casual"),
        ChipOption("business_formal", "Business formal"),
        ChipOption("cocktail", "Cocktail"),
        ChipOption("black_tie", "Black tie"),
        ChipOption("traditional", "Traditional / cultural"),
    )

    val weatherConditions = listOf(
        ChipOption("Sunny", "Sunny"),
        ChipOption("Cloudy", "Cloudy"),
        ChipOption("Rainy", "Rainy"),
        ChipOption("Windy", "Windy"),
        ChipOption("Snowy", "Snowy"),
    )
}
