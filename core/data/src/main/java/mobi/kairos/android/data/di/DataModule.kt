/**
 * Copyright (C) 2026 MOBIWARE
 *
 * This software and its source code are the exclusive property of MOBIWARE.
 *
 * Any unauthorized use, copying, distribution, or modification of this software, in whole or in part,
 * may result in severe civil and criminal penalties under applicable copyright and trade secret laws.
 */
package mobi.kairos.android.data.di

import org.koin.dsl.module

val roomModule = module {}

val dataModule = module {
    includes(roomModule)
}
