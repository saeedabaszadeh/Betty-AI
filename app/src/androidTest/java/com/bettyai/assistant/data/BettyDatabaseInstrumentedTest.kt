package com.bettyai.assistant.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bettyai.assistant.data.local.BettyDatabase
import com.bettyai.assistant.data.local.entity.ConversationEntity
import com.bettyai.assistant.data.local.entity.MemoryEntity
import com.bettyai.assistant.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the Room persistence layer. It exercises the real SQLite
 * engine (in-memory) including the conversation/message relationship, the
 * foreign-key CASCADE delete, and the unique index on memory keys.
 *
 * This is an instrumentation test; it requires a connected device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class BettyDatabaseInstrumentedTest {

    private lateinit var db: BettyDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BettyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertsConversationAndMessages_andCountsThem() = runTest {
        val conversationId = db.conversationDao().upsert(
            ConversationEntity(title = "گفتگو", createdAt = 1L, updatedAt = 1L)
        )
        db.messageDao().insert(
            MessageEntity(conversationId = conversationId, role = "USER", content = "سلام", timestamp = 1L)
        )
        db.messageDao().insert(
            MessageEntity(
                conversationId = conversationId,
                role = "BETTY",
                content = "سلام! چطور می‌توانم کمک کنم؟",
                timestamp = 2L
            )
        )

        assertEquals(2, db.messageDao().countForConversation(conversationId))

        val withCount = db.conversationDao().observeAll(10).first()
        assertEquals(1, withCount.size)
        assertEquals(2, withCount.first().messageCount)
    }

    @Test
    fun deletingConversationCascadesToMessages() = runTest {
        val conversationId = db.conversationDao().upsert(
            ConversationEntity(title = "موقت", createdAt = 1L, updatedAt = 1L)
        )
        db.messageDao().insert(
            MessageEntity(conversationId = conversationId, role = "USER", content = "x", timestamp = 1L)
        )
        assertEquals(1, db.messageDao().countForConversation(conversationId))

        db.conversationDao().delete(conversationId)

        assertEquals(0, db.messageDao().countForConversation(conversationId))
        assertNull(db.conversationDao().get(conversationId))
    }

    @Test
    fun memoryUpsertReplacesByUniqueKey() = runTest {
        db.memoryDao().upsert(
            MemoryEntity(key = "name", value = "سارا", type = "fact", importance = 1, createdAt = 1L, updatedAt = 1L)
        )
        db.memoryDao().upsert(
            MemoryEntity(key = "name", value = "سارا محمدی", type = "fact", importance = 2, createdAt = 1L, updatedAt = 2L)
        )

        val recalled = db.memoryDao().recall("name")
        assertEquals("سارا محمدی", recalled?.value)
        assertEquals(1, db.memoryDao().search("").size)
    }
}
