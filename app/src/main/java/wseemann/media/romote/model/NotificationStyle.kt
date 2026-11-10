package wseemann.media.romote.model

/** Stable preference values; unknown values fall back to the original layout. */
enum class NotificationStyle(val preferenceValue: String) {
    CLASSIC("classic"),
    THUMB_FRIENDLY("thumb_friendly");

    companion object {
        fun fromPreference(value: String?): NotificationStyle =
            entries.firstOrNull { it.preferenceValue == value } ?: CLASSIC
    }
}
