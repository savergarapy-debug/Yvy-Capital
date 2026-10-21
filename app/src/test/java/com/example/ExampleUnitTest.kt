package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdvisorMemberships
import com.example.data.model.Property
import com.example.ui.util.DeepLinkHandler
import com.example.ui.util.DeepLinkTarget
import com.example.ui.util.PromoVideoGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    @Test
    fun deepLinkHandler_parsesValidPropertyAndRejectsMalformedIds() {
        val canonical = DeepLinkHandler.buildPropertyLink("demo-asu-villamorra-1")
        assertEquals("https://yvycapital.com.py/propiedad/demo-asu-villamorra-1", canonical)

        val parsed = DeepLinkHandler.parseRawString(canonical)
        assertTrue(parsed is DeepLinkTarget.PropertyDetail)
        assertEquals("demo-asu-villamorra-1", (parsed as DeepLinkTarget.PropertyDetail).propertyId)

        val customSchemeParsed = DeepLinkHandler.parseRawString("yvycapital://propiedad/demo-sanber-lago-2")
        assertTrue(customSchemeParsed is DeepLinkTarget.PropertyDetail)
        assertEquals("demo-sanber-lago-2", (customSchemeParsed as DeepLinkTarget.PropertyDetail).propertyId)

        val invalidDomain = DeepLinkHandler.parseRawString("https://phishing-domain.com/propiedad/demo-asu-villamorra-1")
        assertTrue(invalidDomain is DeepLinkTarget.InvalidLink)

        assertFalse(DeepLinkHandler.isValidId("../etc/passwd"))
        assertTrue(DeepLinkHandler.isValidId("prop-123_abc"))
    }

    @Test
    fun advisorMemberships_matchAllThreePlansAndBenefits() {
        val inicial = AdvisorMemberships.getSpec(AdvisorMemberships.TIER_INICIAL)
        assertNotNull(inicial)
        assertEquals(99_000L, inicial!!.monthlyPricePyg)
        assertEquals(5, inicial.maxActiveListings)
        assertEquals("3,5 %", inicial.advisorParticipationPercent)
        assertFalse(inicial.canFeatureListings)
        assertFalse(inicial.supportsExternalAgentMode)
        assertFalse(inicial.canGenerateVideos)

        val intermedio = AdvisorMemberships.getSpec(AdvisorMemberships.TIER_INTERMEDIO)
        assertNotNull(intermedio)
        assertEquals(189_000L, intermedio!!.monthlyPricePyg)
        assertEquals(10, intermedio.maxActiveListings)
        assertEquals("4 %", intermedio.advisorParticipationPercent)
        assertTrue(intermedio.canFeatureListings)
        assertTrue(intermedio.supportsExternalAgentMode)
        assertFalse(intermedio.canGenerateVideos)

        val senior = AdvisorMemberships.getSpec(AdvisorMemberships.TIER_SENIOR)
        assertNotNull(senior)
        assertEquals(249_000L, senior!!.monthlyPricePyg)
        assertEquals(10, senior.maxActiveListings)
        assertEquals("4,5 %", senior.advisorParticipationPercent)
        assertTrue(senior.canFeatureListings)
        assertTrue(senior.supportsExternalAgentMode)
        assertTrue(senior.hasDirectYvyCollaboration)
        assertTrue(senior.canGenerateVideos)
    }

    @Test
    fun promoVideoGenerator_createsValidMp4FileForProperty() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleProp = Property(
            id = "prop_video_test_1",
            title = "Penthouse en Villa Morra",
            description = "Penthouse de lujo en Asunción.",
            operationType = "SALE",
            propertyType = "APARTMENT",
            price = 1950000000.0,
            currency = "PYG",
            department = "Asunción",
            city = "Asunción",
            neighborhood = "Villa Morra",
            areaTotalM2 = 240L,
            bedrooms = 3L,
            bathrooms = 3L,
            parkingSpaces = 2L,
            agentName = "Arq. Valeria Ayala",
            agentWhatsapp = "+595981400100"
        )

        val res = PromoVideoGenerator.generatePropertyPromoVideo(
            context = context,
            property = sampleProp,
            agentId = "agent_123",
            aspectRatio = "9:16",
            selectedImages = listOf("fachada_principal", "area_social_quincho", "suite_principal"),
            includeBackgroundMusic = true,
            customHeadline = "Penthouse Exclusivo en Villa Morra",
            preferAiInteractions = false,
            displayCurrency = "PYG"
        )

        assertTrue(res.isSuccess)
        val video = res.getOrThrow()
        assertEquals("PLAN_SENIOR", video.planTierAtCreation)
        assertEquals("9:16", video.aspectRatio)
        val mp4File = File(video.localVideoPath)
        assertTrue(mp4File.exists())
        assertTrue(mp4File.length() > 0L)
    }

    @Test
    fun currencyFormatter_andParaguayLocations_preserveAuthorCurrencyWithoutConversion() {
        // Author currency PYG remains ₲ even if displayCurrency is USD
        val pygPrice = com.example.ui.util.CurrencyFormatter.formatPrice(
            amount = 2_150_000_000.0,
            sourceCurrency = "PYG",
            displayCurrency = "USD"
        )
        assertTrue(pygPrice.startsWith("₲ "))
        assertFalse(pygPrice.contains("$"))
        assertFalse(pygPrice.contains("USD"))

        // Author currency USD remains $ even if displayCurrency is PYG
        val usdPrice = com.example.ui.util.CurrencyFormatter.formatPrice(
            amount = 385_000.0,
            sourceCurrency = "USD",
            displayCurrency = "PYG"
        )
        assertTrue(usdPrice.startsWith("$ "))
        assertFalse(usdPrice.contains("₲"))
        assertFalse(usdPrice.contains("Gs."))

        val invalidPrice = com.example.ui.util.CurrencyFormatter.formatPrice(
            amount = 0.0,
            sourceCurrency = "PYG",
            displayCurrency = "PYG"
        )
        assertEquals("Consultar precio", invalidPrice)

        // Verify Asunción + 17 official departments of Paraguay = 18 administrative entities
        val officialDepts = com.example.ui.util.ParaguayLocations.officialDepartments
        assertEquals(18, officialDepts.size)
        assertTrue(officialDepts.contains("Asunción"))
        assertTrue(officialDepts.contains("Central"))
        assertTrue(officialDepts.contains("Cordillera"))
        assertTrue(officialDepts.contains("Alto Paraná"))
        assertTrue(officialDepts.contains("Itapúa"))
        assertTrue(officialDepts.contains("Boquerón"))
        assertTrue(officialDepts.contains("Ñeembucú"))
        assertTrue(officialDepts.contains("Alto Paraguay"))

        val centralCities = com.example.ui.util.ParaguayLocations.citiesForDepartment("Central")
        assertTrue(centralCities.contains("Todas"))
        assertTrue(centralCities.contains("Luque"))
        assertTrue(centralCities.contains("San Lorenzo"))

        val publishCities = com.example.ui.util.ParaguayLocations.citiesForPublishDepartment("Central")
        assertFalse(publishCities.contains("Todas"))
        assertTrue(publishCities.contains("Luque"))
    }

    @Test
    fun priceSorting_ordersByRealPriceWithinOriginalCurrencyWithoutFakeConversion() {
        val sampleList = listOf(
            Property(id = "usd-high", title = "Casa USD cara", price = 450_000.0, currency = "USD"),
            Property(id = "pyg-high", title = "Casa PYG cara", price = 1_850_000_000.0, currency = "PYG"),
            Property(id = "usd-low", title = "Depto USD menor", price = 120_000.0, currency = "USD"),
            Property(id = "pyg-low", title = "Terreno PYG menor", price = 320_000_000.0, currency = "PYG")
        )

        val asc = com.example.ui.viewmodel.YvyViewModel.sortPropertiesByPriceWithoutConversion(
            sampleList,
            "PRICE_ASC"
        )
        assertEquals(listOf("pyg-low", "pyg-high", "usd-low", "usd-high"), asc.map { it.id })

        val desc = com.example.ui.viewmodel.YvyViewModel.sortPropertiesByPriceWithoutConversion(
            sampleList,
            "PRICE_DESC"
        )
        assertEquals(listOf("pyg-high", "pyg-low", "usd-high", "usd-low"), desc.map { it.id })
    }
}
