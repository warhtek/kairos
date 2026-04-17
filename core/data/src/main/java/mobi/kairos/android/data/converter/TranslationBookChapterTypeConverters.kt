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
package mobi.kairos.android.data.converter

import android.util.Log
import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import mobi.kairos.android.data.model.ChapterFootnoteModel
import mobi.kairos.android.data.model.ChapterHeadingModel
import mobi.kairos.android.data.model.ChapterHebrewSubtitleModel
import mobi.kairos.android.data.model.ChapterLineBreakModel
import mobi.kairos.android.data.model.ChapterVerseModel
import mobi.kairos.android.data.model.FormattedTextModel
import mobi.kairos.android.data.model.InlineHeadingModel
import mobi.kairos.android.data.model.InlineLineBreakModel
import mobi.kairos.android.data.model.VerseFootnoteReferenceModel
import mobi.kairos.android.model.ChapterContent
import mobi.kairos.android.model.ChapterFootnote
import mobi.kairos.android.model.ChapterInlineContent

class TranslationBookChapterTypeConverters {

    private val tag = "TypeConverter"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
        serializersModule = SerializersModule {
            polymorphic(ChapterContent::class) {
                subclass(ChapterHeadingModel::class)
                subclass(ChapterLineBreakModel::class)
                subclass(ChapterHebrewSubtitleModel::class)
                subclass(ChapterVerseModel::class)
            }
            polymorphic(ChapterInlineContent::class) {
                subclass(FormattedTextModel::class)
                subclass(InlineHeadingModel::class)
                subclass(InlineLineBreakModel::class)
                subclass(VerseFootnoteReferenceModel::class)
            }
        }
        classDiscriminator = "type"
    }

    @TypeConverter
    fun fromChapterContentList(content: List<ChapterContent>): String = json.encodeToString(content)

    @TypeConverter
    fun toChapterContentList(content: String): List<ChapterContent> {
        return try {
            // Intentar parsear normalmente
            json.decodeFromString(content)
        } catch (e: Exception) {
            // Si falla, intentar reemplazar "contentType" por "type"
            try {
                Log.d(tag, "Fixing JSON discriminator from contentType to type")
                val fixedContent = content.replace("\"contentType\":", "\"type\":")
                json.decodeFromString(fixedContent)
            } catch (e2: Exception) {
                Log.e(tag, "Error parsing chapter content", e2)
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromChapterFootnoteList(footnotes: List<ChapterFootnote>): String =
        json.encodeToString(footnotes.filterIsInstance<ChapterFootnoteModel>())

    @TypeConverter
    fun toChapterFootnoteList(footnotes: String): List<ChapterFootnote> =
        json.decodeFromString<List<ChapterFootnoteModel>>(footnotes)

    @TypeConverter
    fun fromAudioLinks(links: Map<String, String>): String = json.encodeToString(links)

    @TypeConverter
    fun toAudioLinks(links: String): Map<String, String> = json.decodeFromString(links)
}
