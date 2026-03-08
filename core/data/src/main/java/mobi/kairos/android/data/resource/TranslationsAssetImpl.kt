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
package mobi.kairos.android.data.resource

import java.io.InputStream
import mobi.kairos.android.resource.AssetResource
import mobi.kairos.android.resource.TranslationsAsset

private const val TRANSLATIONS_PATH = "available_translations.json"

class TranslationsAssetImpl(private val assetResource: AssetResource) : TranslationsAsset {
    override suspend fun openJsonStream(): Result<InputStream> = assetResource.openStream(TRANSLATIONS_PATH)
}
