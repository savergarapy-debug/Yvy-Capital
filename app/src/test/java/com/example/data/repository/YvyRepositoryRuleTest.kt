package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Property
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class YvyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun extractFirestoreException(t: Throwable): FirebaseFirestoreException? {
        var curr: Throwable? = t
        while (curr != null) {
            if (curr is FirebaseFirestoreException) return curr
            curr = curr.cause
        }
        return null
    }

    @Test
    fun createProperty_andObserveActive_returnsCreatedProperty() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = YvyRepository(firestore)
        val propId = "prop_${UUID.randomUUID().toString().replace("-", "")}"

        val sample = Property(
            id = propId,
            title = "Residencia en Mburucuyá",
            description = "Amplia residencia con jardín tropical y piscina en el barrio Mburucuyá de Asunción.",
            operationType = "SALE",
            propertyType = "HOUSE",
            price = 1750000000.0,
            currency = "PYG",
            department = "Asunción",
            city = "Asunción",
            neighborhood = "Mburucuyá",
            address = "Av. Santísima Trinidad",
            latitude = -25.2710,
            longitude = -57.5690,
            areaTotalM2 = 450L,
            areaBuiltM2 = 310L,
            bedrooms = 4L,
            bathrooms = 4L,
            parkingSpaces = 2L,
            amenities = listOf("Piscina", "Quincho con parrilla"),
            imageUrls = listOf("mburucuya_1"),
            featured = true,
            status = "active",
            agentId = uid,
            agentName = "Alicia Duarte",
            agentAgency = "Yvy Capital Asunción",
            agentWhatsapp = "+595981123456",
            verified = true
        )

        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProperty(sample)
        }
        assertTrue(createRes.isSuccess)
        assertEquals(propId, createRes.getOrThrow())

        val activeList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeActiveProperties().first { list -> list.any { it.id == propId } }
        }
        assertTrue(activeList.any { it.id == propId })
    }

    @Test
    fun toggleFavorite_andSubmitInquiry_succeedsForAuthenticatedUser() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = YvyRepository(firestore)
        val propId = "favprop_${UUID.randomUUID().toString().replace("-", "")}"

        val sample = YvyRepository.getParaguayDemoProperties(aliceUid).first().copy(id = propId)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProperty(sample).getOrThrow()
        }

        val addedFav = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleFavorite(sample, isCurrentlyFavorite = false).getOrThrow()
        }
        assertTrue(addedFav)

        val favs = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeFavorites(aliceUid).first { it.any { fav -> fav.propertyId == propId } }
        }
        assertTrue(favs.any { it.propertyId == propId })

        val inquiryId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitInquiry(
                property = sample,
                userName = "Alicia Duarte",
                userEmail = ALICE_EMAIL,
                userPhone = "+595981222333",
                inquiryType = "VISIT",
                preferredDate = "Viernes 16:00 hs",
                message = "Quisiera coordinar una visita guiada a la propiedad."
            ).getOrThrow()
        }
        assertNotNull(inquiryId)
        assertTrue(inquiryId.isNotBlank())
    }

    @Test
    fun crossUserFavorites_accessDeniedWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = YvyRepository(firestore)
        val propId = "cross_${UUID.randomUUID().toString().replace("-", "")}"
        val sample = YvyRepository.getParaguayDemoProperties(aliceUid).first().copy(id = propId)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.toggleFavorite(sample, isCurrentlyFavorite = false).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = YvyRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeFavorites(aliceUid).first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (t: Throwable) {
            val fsEx = extractFirestoreException(t)
            assertNotNull("Expected FirebaseFirestoreException in cause chain, got $t", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    @Test
    fun unauthenticatedCaller_observeActiveProperties_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = YvyRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeActiveProperties().first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (t: Throwable) {
            val fsEx = extractFirestoreException(t)
            assertNotNull("Expected FirebaseFirestoreException in cause chain, got $t", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@yvycapital.com.py"
        const val BOB_EMAIL = "bob@yvycapital.com.py"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
