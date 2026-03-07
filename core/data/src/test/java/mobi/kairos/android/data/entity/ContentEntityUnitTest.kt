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
package mobi.kairos.android.data.entity

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Test

class ContentEntityUnitTest {
    @Test
    fun `entity initialization works and supports copy, equals and component`() {
        val entity = ContentEntity(id = 5L)

        assertEquals(5L, entity.id)
        assertEquals(5L, entity.component1())

        // copy()
        val copy = entity.copy(id = 10L)
        assertEquals(10L, copy.id)
        assertNotEquals(entity, copy)

        // equals + hashCode
        val entitySame = ContentEntity(5L)
        assertEquals(entity, entitySame)
        assertEquals(entity.hashCode(), entitySame.hashCode())

        // toString()
        val text = entity.toString()
        assert(text.contains("ContentEntity"))
        assert(text.contains("id=5"))
    }
}
