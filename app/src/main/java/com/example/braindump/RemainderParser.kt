package com.example.braindump

import java.util.Calendar
import java.util.Locale

data class ParsedReminder(
    val title: String,
    val reminderTime: Long
)

object ReminderParser {

    /*
     * Supports:
     *
     * 12:30 PM
     * 12:30
     * 12.30 PM
     * 12.30
     * 9 AM
     * 9 PM
     *
     * A plain number such as "3 tasks" is NOT treated as a time.
     */
    private val timeRegex = Regex(
        """\b(\d{1,2})(?:(?::|\.)(\d{2}))?\s*(AM|PM|am|pm)?\b"""
    )

    private val weekdays = mapOf(
        "monday" to Calendar.MONDAY,
        "tuesday" to Calendar.TUESDAY,
        "wednesday" to Calendar.WEDNESDAY,
        "thursday" to Calendar.THURSDAY,
        "friday" to Calendar.FRIDAY,
        "saturday" to Calendar.SATURDAY,
        "sunday" to Calendar.SUNDAY
    )

    fun parse(text: String): ParsedReminder? {

        val lower = text.lowercase(Locale.getDefault())

        /*
         * Find a possible time.
         */
        val matches = timeRegex.findAll(lower)

        var timeMatch: MatchResult? = null

        for (match in matches) {

            val hourCandidate =
                match.groupValues[1].toIntOrNull()
                    ?: continue

            val minuteCandidate =
                match.groupValues[2].toIntOrNull()
                    ?: 0

            val amPm =
                match.groupValues[3]

            /*
             * Accept:
             *
             * 12:30
             * 12.30
             * 12 PM
             * 12 AM
             *
             * But don't accept random numbers such as:
             *
             * "3 tasks"
             */
            val hasMinutes =
                match.groupValues[2].isNotBlank()

            val hasAmPm =
                amPm.isNotBlank()

            val precededByAt =
                match.range.first > 0 &&
                        lower.substring(
                            0,
                            match.range.first
                        ).trimEnd().endsWith("at")

            if (
                hasMinutes ||
                hasAmPm ||
                precededByAt
            ) {
                timeMatch = match
                break
            }
        }

        timeMatch ?: return null

        var hour =
            timeMatch.groupValues[1].toIntOrNull()
                ?: return null

        val minute =
            timeMatch.groupValues[2].toIntOrNull()
                ?: 0

        val amPm =
            timeMatch.groupValues[3]
                .uppercase(Locale.getDefault())

        /*
         * 12-hour → 24-hour conversion.
         */
        when (amPm) {

            "PM" -> {
                if (hour < 12) {
                    hour += 12
                }
            }

            "AM" -> {
                if (hour == 12) {
                    hour = 0
                }
            }
        }

        /*
         * Without AM/PM, only accept normal 24-hour
         * or reasonable clock hours.
         */
        if (amPm.isBlank() && hour !in 0..23) {
            return null
        }

        if (hour !in 0..23) {
            return null
        }

        if (minute !in 0..59) {
            return null
        }

        val calendar =
            Calendar.getInstance()

        var hasSpecificDate = false

        /*
         * TOMORROW
         */
        if (lower.contains("tomorrow")) {

            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )

            hasSpecificDate = true
        }

        /*
         * TODAY
         */
        else if (lower.contains("today")) {

            hasSpecificDate = true
        }

        /*
         * WEEKDAY
         */
        else {

            for ((dayName, dayValue) in weekdays) {

                if (
                    Regex(
                        """\b$dayName\b"""
                    ).containsMatchIn(lower)
                ) {

                    moveToNextWeekday(
                        calendar,
                        dayValue
                    )

                    hasSpecificDate = true

                    break
                }
            }
        }

        /*
         * Set the detected time.
         */
        calendar.set(
            Calendar.HOUR_OF_DAY,
            hour
        )

        calendar.set(
            Calendar.MINUTE,
            minute
        )

        calendar.set(
            Calendar.SECOND,
            0
        )

        calendar.set(
            Calendar.MILLISECOND,
            0
        )

        /*
         * If no date was specified and today's time
         * has already passed, schedule it for tomorrow.
         */
        if (
            !hasSpecificDate &&
            calendar.timeInMillis <=
            System.currentTimeMillis()
        ) {

            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }

        val reminderTime =
            calendar.timeInMillis

        /*
         * Never create a reminder in the past.
         */
        if (
            reminderTime <=
            System.currentTimeMillis()
        ) {
            return null
        }

        val title =
            createTitle(text)

        return ParsedReminder(
            title = title,
            reminderTime = reminderTime
        )
    }

    private fun moveToNextWeekday(
        calendar: Calendar,
        targetDay: Int
    ) {

        val currentDay =
            calendar.get(
                Calendar.DAY_OF_WEEK
            )

        var daysUntil =
            targetDay - currentDay

        if (daysUntil <= 0) {
            daysUntil += 7
        }

        calendar.add(
            Calendar.DAY_OF_YEAR,
            daysUntil
        )
    }

    private fun createTitle(
        originalText: String
    ): String {

        var title = originalText

        /*
         * Remove the detected time.
         */
        title = title.replace(
            timeRegex,
            ""
        )

        /*
         * Remove date words.
         */
        title = title.replace(
            Regex(
                """\b(today|tomorrow)\b"""
            ),
            ""
        )

        /*
         * Remove weekdays.
         */
        for (weekday in weekdays.keys) {

            title = title.replace(
                Regex(
                    """\b$weekday\b"""
                ),
                ""
            )
        }

        /*
         * Remove common connector words.
         */
        title = title.replace(
            Regex(
                """\b(at|on)\b"""
            ),
            ""
        )

        /*
         * Clean punctuation/spaces.
         */
        title = title
            .replace(
                Regex("""\s+"""),
                " "
            )
            .trim()
            .trim(',', '.', '-', ':')

        if (title.isBlank()) {
            return "Brain Dump Reminder"
        }

        return title.replaceFirstChar {
            it.uppercase()
        }
    }
}