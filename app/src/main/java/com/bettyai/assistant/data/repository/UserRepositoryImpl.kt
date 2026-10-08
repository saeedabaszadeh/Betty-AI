package com.bettyai.assistant.data.repository

import com.bettyai.assistant.data.local.dao.UserDao
import com.bettyai.assistant.data.mapper.toDomain
import com.bettyai.assistant.data.mapper.toEntity
import com.bettyai.assistant.domain.model.UserProfile
import com.bettyai.assistant.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun observeProfile(): Flow<UserProfile?> =
        userDao.observe(UserProfile.SINGLETON_ID).map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? =
        userDao.get(UserProfile.SINGLETON_ID)?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) {
        userDao.upsert(profile.toEntity())
    }

    override suspend fun setDisplayName(name: String) {
        val now = System.currentTimeMillis()
        val existing = userDao.get(UserProfile.SINGLETON_ID)?.toDomain()
        val updated = (existing ?: UserProfile(displayName = name, createdAt = now)).copy(
            displayName = name,
            updatedAt = now
        )
        userDao.upsert(updated.toEntity())
    }

    override suspend fun displayName(): String? =
        userDao.get(UserProfile.SINGLETON_ID)?.displayName?.takeIf { it.isNotBlank() }
}
