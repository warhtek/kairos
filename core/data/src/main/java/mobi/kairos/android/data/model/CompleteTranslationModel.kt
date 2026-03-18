package mobi.kairos.android.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@InternalSerializationApi
data class CompleteTranslationResponse(
    val translation: TranslationImportModel,
    val books: List<CompleteBookModel>,
)

@Serializable
@InternalSerializationApi
data class CompleteBookModel(
    val id: String,
    val name: String,
    val commonName: String,
    val title: String? = null,
    val order: Int,
    val numberOfChapters: Int,
    val totalNumberOfVerses: Int,
    val chapters: List<CompleteChapterModel>,
)

@Serializable
@InternalSerializationApi
data class CompleteChapterModel(
    val numberOfVerses: Int,
    val thisChapterAudioLinks: Map<String, String> = emptyMap(),
    val chapter: ChapterDataModel,
)
