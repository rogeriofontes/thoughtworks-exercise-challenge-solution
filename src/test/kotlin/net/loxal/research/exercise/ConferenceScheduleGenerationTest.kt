/*
 * Copyright 2015 Alexander Orlov <alexander.orlov@loxal.net>. All rights reserved.
 */

package net.loxal.research.exercise

import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals

/**
 * IGNORE, this implementation has been rewritten in Java.
 */
class ConferenceScheduleGenerationTest {

    // HINT: Writing functional code facilitates writing white box tests.

    @Before
    fun generateNewScheduleFromScratch(){
        if(Files.deleteIfExists(Paths.get(ConferenceScheduleGeneration.conferenceSchedule))){
            println("${ConferenceScheduleGeneration.conferenceSchedule} deleted")
        }
        ConferenceScheduleGeneration.main(conferenceEventDataInputPath)
    }

    @Test
    fun main() {
        val generatedSchedule = FileReader(ConferenceScheduleGeneration.conferenceSchedule).readText()

        assertEquals(referenceSchedule.length, generatedSchedule.length)
        assertEquals(referenceSchedule, generatedSchedule)
    }

    companion object {
        private const val conferenceEventDataInputPath = "./src/test/resource/reference-input.txt"
        private const val referenceOutputSchedulePath = "./src/test/resource/reference-output.txt"
        val referenceSchedule = FileReader(referenceOutputSchedulePath).readText()
    }
}