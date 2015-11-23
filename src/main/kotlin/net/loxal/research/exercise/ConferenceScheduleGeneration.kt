/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.research.exercise

import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ConferenceScheduleGeneration(val conferenceEventData: String) {
    private val trackContent = StringBuilder()
    private var trackNumber = 1
    private fun extractEvents(): List<String> {
        val events = conferenceEventData.split("\n")
        return events
    }

    private fun process() {
        val events = extractEvents()
        val durationEventMap = mapDurationToEvent(events)

        generateTrackSchedule(durationEventMap)
        println(trackContent.toString())

        saveConferenceSchedule()
    }

    private fun generateTrackSchedule(durationEventMap: HashMap<Int, List<String>?>) {
        var trackStartTimeInMin: Int = 540
        var lunchOccurred = false
        trackContent.appendln("Track ${trackNumber++}:") // or day
        durationEventMap.forEach {
            val duration = it.key
            it.value?.forEach {
                if (isLunchTime(lunchOccurred, trackStartTimeInMin)) {
                    appendScheduleTime(trackStartTimeInMin)
                    trackContent.appendln(" $lunchEvent")
                    lunchOccurred = true
                    trackStartTimeInMin = trackStartTimeInMin.plus(lunchDurationInMin)
                } else if (isNetworkingTime(trackStartTimeInMin)) {
                    appendScheduleTime(trackStartTimeInMin)
                    appendNetworkingEvent()

                    return generateTrackSchedule(durationEventMap)
                } else {
                    if (isLunchTime(lunchOccurred, trackStartTimeInMin.plus(duration))) {
                        trackStartTimeInMin = lunchTime

                        appendScheduleTime(trackStartTimeInMin)
                        trackContent.appendln(" $lunchEvent")
                        lunchOccurred = true
                        trackStartTimeInMin = trackStartTimeInMin.plus(lunchDurationInMin)
                    } else {
                        appendScheduleTime(trackStartTimeInMin)
                        trackContent.appendln(" $it")

                        trackStartTimeInMin = trackStartTimeInMin.plus(duration)
                        durationEventMap.put(duration, durationEventMap[duration]?.minus(it))
                    }
                }
            }
        }

        if (isNetworkingTime(trackStartTimeInMin)) {
            appendScheduleTime(trackStartTimeInMin)
            appendNetworkingEvent()
        }
    }

    private fun isNetworkingTime(trackStartTimeInMin: Int) = trackStartTimeInMin >= networkingEventTime

    private fun isLunchTime(lunchOccurred: Boolean, trackStartTimeInMin: Int) = trackStartTimeInMin >= lunchTime && !lunchOccurred

    private fun appendNetworkingEvent() {
        trackContent.appendln(" $networkingEvent")
        trackContent.appendln()
    }

    private fun appendScheduleTime(sessionStartTimeInMin: Int) {
        val rawTime = "${minToHours(sessionStartTimeInMin)}:${minToMinWithinHour(sessionStartTimeInMin)}"
        val periodTime = showPeriodTime(rawTime)
        trackContent.append(periodTime)
    }

    private fun minToHours(min: Int): Int = min / 60

    private fun minToMinWithinHour(min: Int): String {
        val minOfHour = min - (min / 60) * 60;
        return if (minOfHour.equals(5)) "05" else minOfHour.toString()
    }

    // time transformation from minutes to the eventually shown time could be tested end-to-end
    private fun showPeriodTime(source24hFormat: String): String {
        val targetFormat = SimpleDateFormat("hh:mma");
        val sourceFormat = SimpleDateFormat("HH:mm");

        return targetFormat.format(sourceFormat.parse(source24hFormat))
    }

    private fun saveConferenceSchedule() {
        val generatedSchedule = FileWriter(conferenceSchedule)
        generatedSchedule.write(trackContent.toString())
        generatedSchedule.flush()
        generatedSchedule.close()
    }

    private fun mapDurationToEvent(events: List<String>): HashMap<Int, List<String>?> {
        val eventDurationToNameMap = HashMap<Int, List<String>?>()
        events.forEach {
            val eventMatcher = eventPattern.matcher(it)

            if (eventMatcher.find()) {
                val durationGroupMatcher = eventMatcher.group(durationGroup)

                val duration: Int =
                        if (durationGroupMatcher.equals(lightningTime))
                            lightningDurationInMin
                        else
                            Integer.parseInt(durationGroupMatcher.substring(0, 2))

                eventDurationToNameMap.put(duration,
                        if (eventDurationToNameMap[duration] == null)
                            listOf(it)
                        else
                            eventDurationToNameMap[duration]?.plus(it)
                )
            }
        }

        return eventDurationToNameMap
    }

    companion object {
        private const val lunchDurationInMin: Int = 60
        private const val lunchTime: Int = 720
        private const val lunchEvent: String = "Lunch"
        private const val networkingEventTime: Int = 960
        private const val networkingEvent: String = "Networking Event"

        private const val lightningDurationInMin: Int = 5
        private const val lightningTime = "lightning"
        private const val durationGroup = "duration"
        private const val eventNameGroup = "eventName"
        private val eventPattern = Pattern.compile("(?<$eventNameGroup>.+)\\ (?<$durationGroup>(\\d{1,2}min|$lightningTime))$")
        const val conferenceSchedule = "./conference-schedule.txt"

        @JvmStatic fun main(vararg args: String) {
            val conferenceEventDataFilePath = args[0]
            if (conferenceEventDataFilePath == "") {
                println("File path to the conference events data required.")
            } else {
                ConferenceScheduleGeneration(FileReader(conferenceEventDataFilePath).readText()).process();
            }
        }
    }
}



