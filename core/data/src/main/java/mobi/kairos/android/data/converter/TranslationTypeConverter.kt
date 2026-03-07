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

import androidx.room.TypeConverter
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection

class TranslationTypeConverters {

    @TypeConverter
    fun fromTextDirection(direction: TextDirection): String = direction.name

    @TypeConverter
    fun toTextDirection(direction: String): TextDirection = TextDirection.valueOf(direction)

    @TypeConverter
    fun fromAvailableFormats(formats: List<AvailableFormat>): String = formats.joinToString(",") { it.name }

    @TypeConverter
    fun toAvailableFormats(formatsString: String): List<AvailableFormat> {
        if (formatsString.isBlank()) return emptyList()
        return formatsString.split(",").map { AvailableFormat.valueOf(it) }
    }
}
