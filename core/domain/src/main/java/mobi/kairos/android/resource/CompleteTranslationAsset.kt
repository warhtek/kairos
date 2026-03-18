package mobi.kairos.android.resource

import java.io.InputStream

interface CompleteTranslationAsset {
    suspend fun openJsonStream(translationId: String): Result<InputStream>
}
