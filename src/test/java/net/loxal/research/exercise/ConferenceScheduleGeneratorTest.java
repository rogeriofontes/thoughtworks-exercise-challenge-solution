/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.research.exercise;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

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
    public void main() throws Exception {
        final String generatedSchedule;
        generatedSchedule = Arrays.toString(Files.readAllBytes(Paths.get(ConferenceScheduleGenerator.conferenceSchedule)));

        assertEquals(referenceSchedule.length(), generatedSchedule.length());
        assertEquals(referenceSchedule, generatedSchedule);
    }
}