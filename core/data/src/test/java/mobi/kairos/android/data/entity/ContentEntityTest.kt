package mobi.kairos.android.data.entity

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ContentEntityTest {
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