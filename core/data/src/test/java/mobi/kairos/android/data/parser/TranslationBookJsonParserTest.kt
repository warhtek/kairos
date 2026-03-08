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
import mobi.kairos.android.parser.TranslationBookJsonParser
class TranslationBookJsonParserTest {

    private lateinit var parser: TranslationBookJsonParser

    @Before
    fun setup() {
        parser = TranslationBookJsonParserImpl()
    }

    @Test
    fun `should parse from InputStream`() {
        // Given
        val jsonString = """
            {"books":[
                {
                  "id": "GEN",
                  "name": "Genesis",
                  "commonName": "Genesis",
                  "title": "The Book of Genesis",
                  "order": 1,
                  "numberOfChapters": 50,
                  "firstChapterNumber": 1,
                  "firstChapterApiLink": "/api/spa_r09/GEN/1.json",
                  "lastChapterNumber": 50,
                  "lastChapterApiLink": "/api/spa_r09/GEN/50.json",
                  "totalNumberOfVerses": 1533
                }
            ]}
        """.trimIndent()

        // When
        val result = parser.parse(ByteArrayInputStream(jsonString.toByteArray()))

        // Then
        result.fold(
            onSuccess = { books ->
                TestCase.assertTrue("List should not be empty", books.isNotEmpty())

                with(books.first()) {
                    TestCase.assertEquals("id", "GEN", id)
                    TestCase.assertEquals("name", "Genesis", name)
                    TestCase.assertEquals("commonName", "Genesis", commonName)
                    TestCase.assertEquals("title", "The Book of Genesis", title)
                    TestCase.assertEquals("order", 1, order)
                    TestCase.assertEquals("numberOfChapters", 50, numberOfChapters)
                    TestCase.assertEquals("firstChapterNumber", 1, firstChapterNumber)
                    TestCase.assertEquals("firstChapterApiLink", "/api/spa_r09/GEN/1.json", firstChapterApiLink)
                    TestCase.assertEquals("lastChapterNumber", 50, lastChapterNumber)
                    TestCase.assertEquals("lastChapterApiLink", "/api/spa_r09/GEN/50.json", lastChapterApiLink)
                    TestCase.assertEquals("totalNumberOfVerses", 1533, totalNumberOfVerses)
                    TestCase.assertFalse("isApocryphal should be false", isApocryphal)
                }
            },
            onFailure = {
                fail("Should not fail: ${it.message}")
            },
        )
    }

    @Test
    fun `should parse multiple books`() {
        // Given
        val jsonString = """
            {"books":[
                {
                  "id": "GEN",
                  "name": "Genesis",
                  "commonName": "Genesis",
                  "title": "The Book of Genesis",
                  "order": 1,
                  "numberOfChapters": 50,
                  "firstChapterNumber": 1,
                  "firstChapterApiLink": "/api/spa_r09/GEN/1.json",
                  "lastChapterNumber": 50,
                  "lastChapterApiLink": "/api/spa_r09/GEN/50.json",
                  "totalNumberOfVerses": 1533
                },
                {
                  "id": "EXO",
                  "name": "Exodus",
                  "commonName": "Exodus",
                  "title": null,
                  "order": 2,
                  "numberOfChapters": 40,
                  "firstChapterNumber": 1,
                  "firstChapterApiLink": "/api/spa_r09/EXO/1.json",
                  "lastChapterNumber": 40,
                  "lastChapterApiLink": "/api/spa_r09/EXO/40.json",
                  "totalNumberOfVerses": 1213,
                  "isApocryphal": true
                }
            ]}
        """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonString.toByteArray())

        // When
        val result = parser.parse(inputStream)

        // Then
        result.fold(
            onSuccess = { books ->
                TestCase.assertEquals("Should have 2 books", 2, books.size)

                with(books[0]) {
                    TestCase.assertEquals("GEN", id)
                    TestCase.assertEquals("Genesis", name)
                    TestCase.assertFalse(isApocryphal)
                }

                with(books[1]) {
                    TestCase.assertEquals("EXO", id)
                    TestCase.assertEquals("Exodus", name)
                    TestCase.assertNull(title)
                    TestCase.assertTrue(isApocryphal)
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
