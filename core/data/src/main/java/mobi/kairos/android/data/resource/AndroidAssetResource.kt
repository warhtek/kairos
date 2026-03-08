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

import android.content.Context
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.kairos.android.resource.AssetResource

class AndroidAssetResource(private val context: Context) : AssetResource {
    override suspend fun openStream(path: String): Result<InputStream> = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(path)
        }
    }
}
