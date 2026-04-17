package mobi.kairos.android.data.repository

import android.util.Log
import kotlinx.serialization.InternalSerializationApi
import mobi.kairos.android.data.converter.TranslationBookChapterTypeConverters
import mobi.kairos.android.data.dao.ChapterDao
import mobi.kairos.android.data.entity.toEntity
import mobi.kairos.android.data.model.ChapterDataModel
import mobi.kairos.android.model.AvailableFormat
import mobi.kairos.android.model.TextDirection
import mobi.kairos.android.model.Translation
import mobi.kairos.android.model.TranslationBook
import mobi.kairos.android.model.TranslationBookChapter
import mobi.kairos.android.repository.ChapterRepository

class ChapterRepositoryImpl(
    private val chapterDao: ChapterDao,
    private val localTranslationRepository: LocalTranslationRepository
) : ChapterRepository {

    private val tag = "ChapterRepositoryImpl"

    override suspend fun importChapters(chapters: List<TranslationBookChapter>) =
        chapterDao.insertAll(chapters.map { it.toEntity() })

    override suspend fun count() = chapterDao.count()

    override suspend fun getChapter(
        translationId: String,
        bookId: String,
        chapterNumber: Int,
    ): Result<TranslationBookChapter> = runCatching {
        Log.d(tag, "getChapter: $translationId/$bookId/$chapterNumber")

        // 1. Primero intentar leer de Room
        val fromRoom = chapterDao.getChapter(translationId, bookId, chapterNumber)

        if (fromRoom != null) {
            Log.d(tag, "Chapter found in Room")
            return@runCatching fromRoom.toDomain()
        }

        Log.d(tag, "Chapter not in Room, trying local file")

        // 2. Si no está en Room, leer del archivo JSON descargado
        val fromLocalFile = localTranslationRepository.getChapter(
            translationId = translationId,
            bookId = bookId,
            chapterNumber = chapterNumber
        )

        if (fromLocalFile != null) {
            Log.d(tag, "Chapter found in local file")
            return@runCatching fromLocalFile
        }

        Log.e(tag, "Chapter not found anywhere")
        error("Chapter not found: $translationId/$bookId/$chapterNumber")
    }
}

@OptIn(InternalSerializationApi::class)
private fun mobi.kairos.android.data.entity.TranslationBookChapterEntity.toDomain(): TranslationBookChapter {
    val converters = TranslationBookChapterTypeConverters()
    val storedTranslationId = this.translationId
    val storedBookId = this.bookId
    return object : TranslationBookChapter {
        override val thisChapterLink = this@toDomain.thisChapterLink
        override val nextChapterApiLink = this@toDomain.nextChapterApiLink
        override val previousChapterApiLink = this@toDomain.previousChapterApiLink
        override val numberOfVerses = this@toDomain.numberOfVerses
        override val thisChapterAudioLinks = converters.toAudioLinks(this@toDomain.thisChapterAudioLinks)
        override val nextChapterAudioLinks = this@toDomain.nextChapterAudioLinks?.let { converters.toAudioLinks(it) }
        override val previousChapterAudioLinks = this@toDomain.previousChapterAudioLinks?.let { converters.toAudioLinks(it) }
        override val translation = object : Translation {
            override val id = storedTranslationId
            override val name = ""
            override val englishName = ""
            override val website = ""
            override val licenseUrl = ""
            override val shortName = ""
            override val language = ""
            override val languageName = null
            override val languageEnglishName = null
            override val textDirection = TextDirection.LTR
            override val availableFormats = emptyList<AvailableFormat>()
            override val listOfBooksApiLink = ""
            override val numberOfBooks = 0
            override val totalNumberOfChapters = 0
            override val totalNumberOfVerses = 0
            override val numberOfApocryphalBooks = null
            override val totalNumberOfApocryphalChapters = null
            override val totalNumberOfApocryphalVerses = null
        }
        override val book = object : TranslationBook {
            override val id = storedBookId
            override val name = ""
            override val commonName = ""
            override val title = null
            override val order = 0
            override val numberOfChapters = 0
            override val firstChapterNumber = 1
            override val firstChapterApiLink = ""
            override val lastChapterNumber = 0
            override val lastChapterApiLink = ""
            override val totalNumberOfVerses = 0
            override val isApocryphal = false
        }
        override val chapter = ChapterDataModel(
            number = this@toDomain.chapterNumber,
            content = converters.toChapterContentList(this@toDomain.chapterContent),
            footnotes = converters.toChapterFootnoteList(this@toDomain.chapterFootnotes),
        )
    }
}
