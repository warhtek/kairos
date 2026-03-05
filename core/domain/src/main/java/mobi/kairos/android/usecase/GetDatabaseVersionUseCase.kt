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
package mobi.kairos.android.usecase

import mobi.kairos.android.repository.DatabaseRepository

class GetDatabaseVersionUseCase(private val repository: DatabaseRepository) {
    suspend operator fun invoke(): Int = repository.getVersion()
}
