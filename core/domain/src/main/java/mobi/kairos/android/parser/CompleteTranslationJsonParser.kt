package mobi.kairos.android.parser

import java.io.InputStream
import mobi.kairos.android.model.TranslationBookChapter

interface CompleteTranslationJsonParser {
    fun parse(
        inputStream: InputStream,
        translationId: String,
    ): Result<List<TranslationBookChapter>>
}
