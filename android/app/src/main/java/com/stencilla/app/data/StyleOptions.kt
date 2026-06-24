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

    val occasions = listOf(
        ChipOption("casual", "Casual"),
        ChipOption("business", "Business"),
        ChipOption("formal", "Formal"),
        ChipOption("party", "Party"),
        ChipOption("college", "College"),
        ChipOption("date_night", "Date night"),
    )
}
