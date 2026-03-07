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
package mobi.kairos.android.model

interface Translation {
    val id: String
    val name: String
    val englishName: String
    val website: String
    val licenseUrl: String
    val shortName: String
    val language: String
    val languageName: String?
    val languageEnglishName: String?
    val textDirection: TextDirection
    val availableFormats: List<AvailableFormat>
    val listOfBooksApiLink: String
    val numberOfBooks: Int
    val totalNumberOfChapters: Int
    val totalNumberOfVerses: Int
    val numberOfApocryphalBooks: Int?
    val totalNumberOfApocryphalChapters: Int?
    val totalNumberOfApocryphalVerses: Int?
}

enum class TextDirection { LTR, RTL }
enum class AvailableFormat { JSON, USFM }
