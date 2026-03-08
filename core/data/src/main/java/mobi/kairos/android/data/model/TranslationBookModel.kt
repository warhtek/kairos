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
package mobi.kairos.android.data.model

import mobi.kairos.android.model.TranslationBook

data class TranslationBookModel(
    override val id: String,
    override val name: String,
    override val commonName: String,
    override val title: String?,
    override val order: Int,
    override val numberOfChapters: Int,
    override val firstChapterNumber: Int,
    override val firstChapterApiLink: String,
    override val lastChapterNumber: Int,
    override val lastChapterApiLink: String,
    override val totalNumberOfVerses: Int,
    override val isApocryphal: Boolean = false,
) : TranslationBook
