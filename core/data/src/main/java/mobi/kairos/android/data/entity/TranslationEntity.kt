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

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import mobi.kairos.android.data.converter.TranslationTypeConverters
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection
import mobi.kairos.android.model.Translation

@Entity(tableName = "translations")
@TypeConverters(TranslationTypeConverters::class)
data class TranslationEntity(
    @PrimaryKey
    override val id: String,
    override val name: String,
    override val englishName: String,
    override val website: String,
    override val licenseUrl: String,
    override val shortName: String,
    override val language: String,
    override val languageName: String?,
    override val languageEnglishName: String?,
    override val textDirection: TextDirection,
    override val availableFormats: List<AvailableFormat>,
    override val listOfBooksApiLink: String,
    override val numberOfBooks: Int,
    override val totalNumberOfChapters: Int,
    override val totalNumberOfVerses: Int,
    override val numberOfApocryphalBooks: Int?,
    override val totalNumberOfApocryphalChapters: Int?,
    override val totalNumberOfApocryphalVerses: Int?,
) : Translation

fun Translation.toEntity(): TranslationEntity = TranslationEntity(
    id = id,
    name = name,
    englishName = englishName,
    website = website,
    licenseUrl = licenseUrl,
    shortName = shortName,
    language = language,
    languageName = languageName,
    languageEnglishName = languageEnglishName,
    textDirection = textDirection,
    availableFormats = availableFormats,
    listOfBooksApiLink = listOfBooksApiLink,
    numberOfBooks = numberOfBooks,
    totalNumberOfChapters = totalNumberOfChapters,
    totalNumberOfVerses = totalNumberOfVerses,
    numberOfApocryphalBooks = numberOfApocryphalBooks,
    totalNumberOfApocryphalChapters = totalNumberOfApocryphalChapters,
    totalNumberOfApocryphalVerses = totalNumberOfApocryphalVerses,
)
