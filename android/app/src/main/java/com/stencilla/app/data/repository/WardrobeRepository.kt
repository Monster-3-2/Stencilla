package com.stencilla.app.data.repository

import android.content.Context
import android.net.Uri
import com.stencilla.app.data.local.db.ClothingItemDao
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.ml.OnDeviceLabeler
import com.stencilla.app.util.ImageFileUtil
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeRepository @Inject constructor(
    private val api: ApiService,
    private val onDeviceLabeler: OnDeviceLabeler,
    private val dao: ClothingItemDao,
) {
    fun observeItems(): Flow<List<ClothingItemEntity>> = dao.observeAll()
    fun observeByCategory(category: String): Flow<List<ClothingItemEntity>> = dao.observeByCategory(category)

    suspend fun addItem(context: Context, imageUri: Uri): ClothingItemEntity {
        val localFile = ImageFileUtil.copyToInternalStorage(context, imageUri)
        val roughLabel = onDeviceLabeler.labelImage(context, imageUri)

        val id = UUID.randomUUID().toString()
        var entity = ClothingItemEntity(
            id = id,
            localImagePath = localFile.absolutePath,
            onDeviceLabel = roughLabel,
            aiTagged = false,
        )
        dao.insert(entity)

        val tags = api.tagClothingItem(ImageFileUtil.fileToMultipart(localFile))

        entity = entity.copy(
            category = tags.category,
            subcategory = tags.subcategory,
            colorPrimary = tags.colorPrimary,
            colorSecondary = tags.colorSecondary,
            pattern = tags.pattern,
            formality = tags.formality,
            season = tags.season,
            material = tags.material,
            fit = tags.fit,
            aiImageDescription = tags.aiImageDescription,
            needsClarification = tags.needsClarification,
            clarificationQuestion = tags.clarificationQuestion,
            aiTagged = true,
        )
        dao.update(entity)
        return entity
    }

    suspend fun saveClarification(id: String, material: String?, fit: String?) {
        val entity = dao.getById(id) ?: return
        dao.update(
            entity.copy(
                material = material ?: entity.material,
                fit = fit ?: entity.fit,
                needsClarification = false,
                clarificationQuestion = null,
            )
        )
    }

    suspend fun deleteItem(item: ClothingItemEntity) {
        File(item.localImagePath).delete()
        dao.delete(item)
    }
}
