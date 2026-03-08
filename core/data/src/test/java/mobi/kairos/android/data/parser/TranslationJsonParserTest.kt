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
package mobi.kairos.android.data.parser

import junit.framework.TestCase
import java.io.ByteArrayInputStream
import kotlin.test.fail
import org.junit.Before
import org.junit.Test
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection
import mobi.kairos.android.parser.TranslationJsonParser

class TranslationJsonParserTest {

    private lateinit var parser: TranslationJsonParser

    @Before
    fun setup() {
        parser = TranslationJsonParserImpl()
    }

    @Test
    fun `should parse from InputStream`() {
        // Given
        val jsonString = """
            {"translations":[
                {
                  "id": "spa_r09",
                  "name": "Santa Biblia — Reina Valera 1909",
                  "website": "https://ebible.org/Scriptures/details.php?id=spaRV1909",
                  "licenseUrl": "https://ebible.org/Scriptures/details.php?id=spaRV1909",
                  "licenseNotes": null,
                  "shortName": "R09",
                  "englishName": "Reina Valera 1909",
                  "language": "spa",
                  "textDirection": "ltr",
                  "sha256": "94e154b2e6e56eda1702d9e9f664357a5f2aa82634b551111b0b698d124e97d5",
                  "availableFormats": [
                    "json"
                  ],
                  "listOfBooksApiLink": "/api/spa_r09/books.json",
                  "numberOfBooks": 66,
                  "totalNumberOfChapters": 1189,
                  "totalNumberOfVerses": 31102,
                  "languageName": "español",
                  "languageEnglishName": "Spanish"
                }
            ]}
        """.trimIndent()
        // When
        val result = parser.parse(ByteArrayInputStream(jsonString.toByteArray()))

        // Then
        result.fold(
            onSuccess = { translations ->
                TestCase.assertTrue("List should not be empty", translations.isNotEmpty())

                with(translations.first()) {
                    // required attributes
                    TestCase.assertEquals("id", "spa_r09", id)
                    TestCase.assertEquals("name", "Santa Biblia — Reina Valera 1909", name)
                    TestCase.assertEquals("englishName", "Reina Valera 1909", englishName)
                    TestCase.assertEquals("website", "https://ebible.org/Scriptures/details.php?id=spaRV1909", website)
                    TestCase.assertEquals("licenseUrl", "https://ebible.org/Scriptures/details.php?id=spaRV1909", licenseUrl)
                    TestCase.assertEquals("shortName", "R09", shortName)
                    TestCase.assertEquals("language", "spa", language)
                    TestCase.assertEquals("textDirection", TextDirection.LTR, textDirection)
                    TestCase.assertEquals("listOfBooksApiLink", "/api/spa_r09/books.json", listOfBooksApiLink)
                    TestCase.assertEquals("numberOfBooks", 66, numberOfBooks)
                    TestCase.assertEquals("totalNumberOfChapters", 1189, totalNumberOfChapters)
                    TestCase.assertEquals("totalNumberOfVerses", 31102, totalNumberOfVerses)

                    // AvailableFormats
                    TestCase.assertNotNull("availableFormats should not be null", availableFormats)
                    TestCase.assertEquals("availableFormats size", 1, availableFormats.size)
                    TestCase.assertTrue("availableFormats should contain JSON", availableFormats.contains(AvailableFormat.JSON))

                    // optional
                    TestCase.assertEquals("languageName", "español", languageName)
                    TestCase.assertEquals("languageEnglishName", "Spanish", languageEnglishName)

                    // null
                    TestCase.assertNull("numberOfApocryphalBooks should be null", numberOfApocryphalBooks)
                    TestCase.assertNull("totalNumberOfApocryphalChapters should be null", totalNumberOfApocryphalChapters)
                    TestCase.assertNull("totalNumberOfApocryphalVerses should be null", totalNumberOfApocryphalVerses)
                }
            },
            onFailure = {
                fail("Should not fail: ${it.message}")
            },
        )
    }

    @Test
    fun `should parse multiple translations`() {
        // Given
        val jsonString = """
            {"translations":[
                {
                  "id": "spa_r09",
                  "name": "Santa Biblia — Reina Valera 1909",
                  "website": "",
                  "licenseUrl": "",
                  "shortName": "R09",
                  "englishName": "Reina Valera 1909",
                  "language": "spa",
                  "textDirection": "ltr",
                  "availableFormats": ["json"],
                  "listOfBooksApiLink": "/api/spa_r09/books.json",
                  "numberOfBooks": 66,
                  "totalNumberOfChapters": 1189,
                  "totalNumberOfVerses": 31102,
                  "languageName": "español",
                  "languageEnglishName": "Spanish"
                },
                {
                  "id": "eng_kjv",
                  "name": "King James Version",
                  "website": "",
                  "licenseUrl": "",
                  "shortName": "KJV",
                  "englishName": "King James Version",
                  "language": "eng",
                  "textDirection": "ltr",
                  "availableFormats": ["json", "usfm"],
                  "listOfBooksApiLink": "/api/eng_kjv/books.json",
                  "numberOfBooks": 66,
                  "totalNumberOfChapters": 1189,
                  "totalNumberOfVerses": 31102,
                  "languageName": "English",
                  "languageEnglishName": "English"
                }
            ]}
        """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonString.toByteArray())

        // When
        val result = parser.parse(inputStream)

        // Then
        result.fold(
            onSuccess = { translations ->
                TestCase.assertEquals("Should have 2 translations", 2, translations.size)

                // First translation (español)
                with(translations[0]) {
                    TestCase.assertEquals("spa_r09", id)
                    TestCase.assertEquals("español", languageName)
                    TestCase.assertEquals(1, availableFormats.size)
                }

                // Second translation (english)
                with(translations[1]) {
                    TestCase.assertEquals("eng_kjv", id)
                    TestCase.assertEquals("English", languageName)
                    TestCase.assertEquals(2, availableFormats.size)
                    TestCase.assertTrue(availableFormats.contains(AvailableFormat.JSON))
                    TestCase.assertTrue(availableFormats.contains(AvailableFormat.USFM))
                }
            },
            onFailure = {
                fail("Should not fail: ${it.message}")
            },
        )
    }

    @Test
    fun `should handle malformed JSON`() {
        // Given
        val malformedJson = "this is not json"
        val inputStream = ByteArrayInputStream(malformedJson.toByteArray())

        // When
        val result = parser.parse(inputStream)

        // Then
        TestCase.assertTrue("Result should be failure", result.isFailure)
        TestCase.assertNotNull("Exception should not be null", result.exceptionOrNull())
    }
}
