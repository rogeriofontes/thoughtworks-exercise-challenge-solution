/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.research.exercise;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ConferenceScheduleGeneratorTest {

    // HINT: Writing functional code facilitates writing white box tests.

    private static final String conferenceEventDataInputPath = "./src/test/resource/reference-input.txt";
    private static final String referenceOutputSchedulePath = "./src/test/resource/reference-output.txt";
    private static String referenceSchedule;

    @BeforeClass
    public static void readReferenceSchedule() throws Exception {
        referenceSchedule = Arrays.toString(Files.readAllBytes(Paths.get(referenceOutputSchedulePath)));
    }

    @Before
    public void generateNewScheduleFromScratch() throws Exception {
        if (Files.deleteIfExists(Paths.get(ConferenceScheduleGenerator.conferenceSchedule))) {
            System.out.println(ConferenceScheduleGenerator.conferenceSchedule + " deleted");
        }
        ConferenceScheduleGenerator.main(conferenceEventDataInputPath);
    }

    @Test
    public void isLunchTime() throws Exception {
        ConferenceScheduleGenerator conferenceScheduleGenerator = new ConferenceScheduleGenerator(Files.readAllLines(Paths.get(conferenceEventDataInputPath)));
        final Map<Integer, List<String>> durationEventMap = new LinkedHashMap<>();
        final List<String> eventsForFive = new ArrayList<>();
        eventsForFive.add("Writing Fast Tests Against Enterprise Rails");
        eventsForFive.add("Writing Fast Tests Against Enterprise Rails1213");
        durationEventMap.put(5, eventsForFive);
        final List<String> eventsFor30 = new ArrayList<>();
        eventsFor30.add("30th event");
        eventsFor30.add("30th event alter");
        durationEventMap.put(30, eventsForFive);


        assertFalse(conferenceScheduleGenerator.isLunchTime(false, 710));
        assertFalse(conferenceScheduleGenerator.isLunchTime(false, 710));
    }

    @Test
    public void main() throws Exception {
        final String generatedSchedule;
        generatedSchedule = Arrays.toString(Files.readAllBytes(Paths.get(ConferenceScheduleGenerator.conferenceSchedule)));

        assertEquals(referenceSchedule.length(), generatedSchedule.length());
        assertEquals(referenceSchedule, generatedSchedule);
    }
}