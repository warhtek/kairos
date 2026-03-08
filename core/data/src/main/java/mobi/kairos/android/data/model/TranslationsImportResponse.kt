package mobi.kairos.android.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@InternalSerializationApi
data class TranslationsImportResponse(
    val translations: List<TranslationImportModel>
) {
}
