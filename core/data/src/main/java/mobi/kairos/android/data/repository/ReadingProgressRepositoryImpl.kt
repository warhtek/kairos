package mobi.kairos.android.data.repository

import mobi.kairos.android.data.dao.ReadingProgressDao
import mobi.kairos.android.data.entity.ReadingProgressEntity
import mobi.kairos.android.repository.ReadingProgressRepository
import mobi.kairos.android.usecase.LastReadVerse

class ReadingProgressRepositoryImpl(
    private val readingProgressDao: ReadingProgressDao,
) : ReadingProgressRepository {
    override suspend fun getLastRead(): Result<LastReadVerse?> = runCatching {
        readingProgressDao.getLastRead()?.toDomain()
    }

    override suspend fun saveLastRead(verse: LastReadVerse) {
        readingProgressDao.save(verse.toEntity())
    }
}

private fun ReadingProgressEntity.toDomain() = LastReadVerse(
    translationId = translationId,
    bookId = bookId,
    bookName = bookName,
    chapterNumber = chapterNumber,
    verseNumber = verseNumber,
    verseText = verseText,
)

private fun LastReadVerse.toEntity() = ReadingProgressEntity(
    translationId = translationId,
    bookId = bookId,
    bookName = bookName,
    chapterNumber = chapterNumber,
    verseNumber = verseNumber,
    verseText = verseText,
)
