/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.research.exercise;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConferenceScheduleGenerator {
    protected static final String conferenceSchedule = "./conference-schedule.txt";
    private static final String NEW_LINE = "\n";
    private static final int LUNCH_DURATION_IN_MIN = 60;
    private static final int LUNCH_TIME = 720;
    private static final String LUNCH_EVENT = "Lunch";
    private static final int NETWORKING_EVENT_TIME = 960;
    private static final String NETWORKING_EVENT = "Networking Event";
    private static final int LIGHTNING_DURATION_IN_MIN = 5;
    private static final String LIGHTNING_TIME = "lightning";
    private static final String DURATION_GROUP = "duration";
    private static final String EVENT_NAME_GROUP = "eventName";
    private static final Pattern EVENT_PATTERN = Pattern.compile("(?<" + EVENT_NAME_GROUP + ">.+) (?<" + DURATION_GROUP + ">(\\d{1,2}min|" + LIGHTNING_TIME + "))$");

    private final List<String> conferenceEventLines;
    private final StringBuilder trackContent = new StringBuilder();

    private int trackNumber = 1;

    private ConferenceScheduleGenerator(final List<String> conferenceEventLines) {
        this.conferenceEventLines = conferenceEventLines;
        process();
    }

    public static void main(final String... args) {
        if (hasPathArg(args)) {
            final String conferenceEventDataFilePath = args[0];
            try {
                new ConferenceScheduleGenerator(
                        Files.readAllLines(Paths.get(conferenceEventDataFilePath))
                );
            } catch (final IOException e) {
                System.err.println("Cannot read file: " + e.getMessage());
            }
        } else {
            System.err.println("File path to the conference events data required.");
        }
    }

    private static boolean hasPathArg(final String... args) {
        return args.length > 0;
    }

    private void process() {
        final Map<Integer, List<String>> durationEventMap = mapDurationToEvent(this.conferenceEventLines);

        generateTrackSchedule(durationEventMap);
        System.out.println(trackContent.toString());

        saveConferenceSchedule();
    }

    private void generateTrackSchedule(final Map<Integer, List<String>> durationEventMap) {
        int trackStartTimeInMin = 540;
        boolean lunchOccurred = false;
        trackContent.append("Track ").append(trackNumber++).append(":").append(NEW_LINE); // or day
        for (final Map.Entry<Integer, List<String>> durationEventEntry : durationEventMap.entrySet()) {
            final int eventDuration = durationEventEntry.getKey();

            for (final String eventName : durationEventEntry.getValue()) {
                if (durationEventEntry.getValue().size() == 0) break;
                if (isLunchTime(lunchOccurred, trackStartTimeInMin)) {
                    appendScheduleTime(trackStartTimeInMin);
                    trackContent.append(" ").append(LUNCH_EVENT).append(NEW_LINE);
                    lunchOccurred = true;
                    trackStartTimeInMin = trackStartTimeInMin + LUNCH_DURATION_IN_MIN;
                } else if (isNetworkingTime(trackStartTimeInMin)) {
                    appendScheduleTime(trackStartTimeInMin);
                    appendNetworkingEvent();

                    generateTrackSchedule(durationEventMap);
                } else {
                    if (isLunchTime(lunchOccurred, trackStartTimeInMin + eventDuration)) {
                        trackStartTimeInMin = LUNCH_TIME;

                        appendScheduleTime(trackStartTimeInMin);
                        trackContent.append(" ").append(LUNCH_EVENT).append(NEW_LINE);
                        lunchOccurred = true;
                        trackStartTimeInMin = trackStartTimeInMin + LUNCH_DURATION_IN_MIN;
                    } else {
                        appendScheduleTime(trackStartTimeInMin);
                        trackContent.append(" ").append(eventName).append(NEW_LINE);

                        trackStartTimeInMin = trackStartTimeInMin + eventDuration;
                        final List<String> updatedEventList = new ArrayList<>(durationEventMap.get(eventDuration));
                        updatedEventList.remove(eventName);

                        durationEventMap.put(eventDuration, updatedEventList);
                    }
                }
            }
        }
    }

    private boolean isNetworkingTime(final int trackStartTimeInMin) {
        return trackStartTimeInMin >= NETWORKING_EVENT_TIME;
    }

    private boolean isLunchTime(final boolean lunchOccurred, final int trackStartTimeInMin) {
        return trackStartTimeInMin >= LUNCH_TIME && !lunchOccurred;
    }

    private void appendNetworkingEvent() {
        trackContent.append(" ").append(NETWORKING_EVENT).append(NEW_LINE);
        trackContent.append(NEW_LINE);
    }

    private void appendScheduleTime(final int sessionStartTimeInMin) {
        final String rawTime = minToHours(sessionStartTimeInMin) + ":" + minToMinWithinHour(sessionStartTimeInMin);
        final String periodTime = showPeriodTime(rawTime);
        trackContent.append(periodTime);
    }

    private int minToHours(final int min) {
        return min / 60;
    }

    private String minToMinWithinHour(final int min) {
        final Integer minOfHour = min - (min / 60) * 60;
        if (minOfHour.equals(5))
            return "05";
        else
            return minOfHour.toString();
    }

    // time transformation from minutes to the eventually shown time could be tested end-to-end
    private String showPeriodTime(final String source24hFormat) {
        final DateFormat targetFormat = new SimpleDateFormat("hh:mma");
        final DateFormat sourceFormat = new SimpleDateFormat("HH:mm");

        try {
            return targetFormat.format(sourceFormat.parse(source24hFormat));
        } catch (final ParseException e) {
            System.err.println(e.getMessage());
            return "n/a";
        }
    }

    private void saveConferenceSchedule() {
        final FileWriter generatedSchedule;
        try {
            generatedSchedule = new FileWriter(conferenceSchedule);
            generatedSchedule.write(trackContent.toString());
            generatedSchedule.flush();
            generatedSchedule.close();
        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private Map<Integer, List<String>> mapDurationToEvent(final List<String> events) {
        final HashMap<Integer, List<String>> eventDurationToNameMap = new HashMap<>();
        events.forEach(event -> {
            final Matcher eventMatcher = EVENT_PATTERN.matcher(event);

            if (eventMatcher.find()) {
                final String durationGroupMatcher = eventMatcher.group(DURATION_GROUP);

                final int duration;
                if (durationGroupMatcher.equals(LIGHTNING_TIME)) {
                    duration = LIGHTNING_DURATION_IN_MIN;
                } else {
                    duration = Integer.parseInt(durationGroupMatcher.substring(0, 2));
                }

                if (eventDurationToNameMap.get(duration) == null) {
                    eventDurationToNameMap.put(duration, Collections.singletonList(event));
                } else {
                    final List<String> concatenatedEventList = new ArrayList<>(eventDurationToNameMap.get(duration));
                    concatenatedEventList.add(event);
                    eventDurationToNameMap.put(duration, concatenatedEventList);
                }
            }
        });

        return eventDurationToNameMap;
    }
}
