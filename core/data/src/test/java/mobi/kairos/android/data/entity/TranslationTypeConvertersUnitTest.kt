/*
 * © 2026 MOBIWARE. All rights reserved.
 *
 * This software and its source code are the exclusive property of MOBIWARE.
 * Any unauthorized use, reproduction, distribution, modification, or disclosure
 * of this software, whether in whole or in part, is strictly prohibited.
 *
 * Violations may result in severe civil and criminal penalties under applicable
 * copyright, intellectual property, and trade secret laws.
 */
package mobi.kairos.android.data.entity

import org.junit.Assert.assertEquals
import org.junit.Test
import mobi.kairos.android.data.converter.TranslationTypeConverters
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection

class TranslationTypeConvertersTest {

    private val converters = TranslationTypeConverters()

    @Test
    fun `fromTextDirection - should convert LTR to String`() {
        val result = converters.fromTextDirection(TextDirection.LTR)
        assertEquals("LTR", result)
    }

    @Test
    fun `toTextDirection - should convert String to LTR enum`() {
        val result = converters.toTextDirection("LTR")
        assertEquals(TextDirection.LTR, result)
    }

    @Test
    fun `fromAvailableFormats - should convert list to comma string`() {
        val formats = listOf(AvailableFormat.JSON, AvailableFormat.USFM)
        val result = converters.fromAvailableFormats(formats)
        assertEquals("JSON,USFM", result)
    }

    @Test
    fun `toAvailableFormats - should convert comma string to list`() {
        val result = converters.toAvailableFormats("JSON,USFM")
        assertEquals(listOf(AvailableFormat.JSON, AvailableFormat.USFM), result)
    }

    @Test
    fun `toAvailableFormats - empty string returns empty list`() {
        val result = converters.toAvailableFormats("")
        assertEquals(emptyList<AvailableFormat>(), result)
    }
}
