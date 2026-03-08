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

import java.io.InputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mobi.kairos.android.data.model.TranslationImportModel
import mobi.kairos.android.data.model.TranslationsImportResponse
import mobi.kairos.android.parser.TranslationJsonParser

class TranslationJsonParserImpl : TranslationJsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
    }

    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    override fun parse(inputStream: InputStream): Result<List<TranslationImportModel>> {
        return runCatching {
            inputStream.use { stream ->
                val response = json.decodeFromStream<TranslationsImportResponse>(stream)
                response.translations
            }
        }
    }
}
