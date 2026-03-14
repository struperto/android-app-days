package com.struperto.androidappdays.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.struperto.androidappdays.testing.targetApp
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthConnectRepositoryInstrumentedTest {
    @Test
    fun availabilityAndDailyRead_doNotCrashOnEmulator() = runBlocking {
        val repository = targetApp().appContainer.healthConnectRepository
        val availability = repository.availability()
        val grantedPermissions = repository.grantedPermissions()
        val observations = repository.readDailyObservations(LocalDate.now(targetApp().appContainer.clock))

        assertNotNull(availability)
        assertNotNull(grantedPermissions)
        assertNotNull(observations)
        if (availability != HealthConnectAvailability.AVAILABLE) {
            assertTrue(observations.isEmpty())
        }
    }
}
