package mobi.kairos.android.data.resource

import java.io.InputStream
import mobi.kairos.android.resource.AssetResource
import mobi.kairos.android.resource.CompleteTranslationAsset

private const val COMPLETE_TRANSLATION_PATH = "complete.json"

class CompleteTranslationAssetImpl(private val assetResource: AssetResource) : CompleteTranslationAsset {
    override suspend fun openJsonStream(translationId: String): Result<InputStream> =
        assetResource.openStream("$translationId/$COMPLETE_TRANSLATION_PATH")
}
