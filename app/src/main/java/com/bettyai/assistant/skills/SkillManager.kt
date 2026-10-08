package com.bettyai.assistant.skills

import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.data.local.dao.SkillDao
import com.bettyai.assistant.data.mapper.toEntity
import com.bettyai.assistant.domain.model.SkillInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry and lifecycle owner for all skills. Skills are injected as a set by
 * Hilt, so registering a new capability is purely additive.
 */
@Singleton
class SkillManager @Inject constructor(
    private val skillDao: SkillDao,
    private val logger: BettyLogger,
    skills: Set<@JvmSuppressWildcards Skill>
) {

    private val registry = LinkedHashMap<String, Skill>()
    private val enabledState = ConcurrentHashMap<String, Boolean>()
    private val initMutex = Mutex()

    @Volatile
    private var initialized = false

    init {
        skills.forEach { registry[it.id] = it }
    }

    /** Persist skill metadata and load user enable/disable preferences (once). */
    suspend fun ensureInitialized() {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return
            val stored = runCatching { skillDao.getAll().associateBy { it.id } }.getOrDefault(emptyMap())
            val now = System.currentTimeMillis()
            registry.values.forEach { skill ->
                enabledState[skill.id] = stored[skill.id]?.enabled ?: true
                runCatching {
                    skillDao.upsert(
                        SkillInfo(
                            id = skill.id,
                            name = skill.name,
                            description = skill.description,
                            enabled = enabledState[skill.id] ?: true,
                            permissions = skill.permissions,
                            builtIn = skill.builtIn
                        ).toEntity(now)
                    )
                }
            }
            initialized = true
            logger.info(TAG, "Registered ${registry.size} skills: ${registry.keys}")
        }
    }

    /** Dynamically register a skill at runtime (used by future modules). */
    fun register(skill: Skill) {
        registry[skill.id] = skill
        enabledState.putIfAbsent(skill.id, true)
        logger.info(TAG, "Registered skill ${skill.id}")
    }

    fun all(): List<Skill> = registry.values.toList()

    fun enabledSkills(): List<Skill> = registry.values.filter { isEnabled(it.id) }

    fun get(id: String): Skill? = registry[id]

    fun isEnabled(id: String): Boolean = enabledState[id] != false

    suspend fun setEnabled(id: String, enabled: Boolean) {
        enabledState[id] = enabled
        runCatching { skillDao.setEnabled(id, enabled, System.currentTimeMillis()) }
        logger.info(TAG, "Skill $id enabled=$enabled")
    }

    fun infos(): List<SkillInfo> = registry.values.map {
        SkillInfo(
            id = it.id,
            name = it.name,
            description = it.description,
            enabled = isEnabled(it.id),
            permissions = it.permissions,
            builtIn = it.builtIn
        )
    }

    /** Execute a skill by id. Never throws. */
    suspend fun execute(skillId: String, input: SkillInput, context: SkillContext): SkillResult {
        val skill = registry[skillId]
            ?: return SkillResult(spokenText = "این قابلیت در دسترس نیست.", handled = false)
        return try {
            skill.execute(input, context)
        } catch (t: Throwable) {
            logger.error(TAG, "Skill $skillId failed", t)
            SkillResult(spokenText = "متأسفم، در اجرای این کار مشکلی پیش آمد.", handled = false)
        }
    }

    private companion object {
        const val TAG = "SkillManager"
    }
}
