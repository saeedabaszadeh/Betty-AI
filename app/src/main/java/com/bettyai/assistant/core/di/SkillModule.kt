package com.bettyai.assistant.core.di

import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.builtin.AboutSkill
import com.bettyai.assistant.skills.builtin.GreetingSkill
import com.bettyai.assistant.skills.builtin.HelpSkill
import com.bettyai.assistant.skills.builtin.RememberSkill
import com.bettyai.assistant.skills.builtin.TimeSkill
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Registers all built-in skills. To add a new capability, implement [Skill] and
 * add one @Binds @IntoSet line here — the core engine is untouched.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SkillModule {

    @Binds
    @IntoSet
    abstract fun bindTimeSkill(impl: TimeSkill): Skill

    @Binds
    @IntoSet
    abstract fun bindGreetingSkill(impl: GreetingSkill): Skill

    @Binds
    @IntoSet
    abstract fun bindHelpSkill(impl: HelpSkill): Skill

    @Binds
    @IntoSet
    abstract fun bindAboutSkill(impl: AboutSkill): Skill

    @Binds
    @IntoSet
    abstract fun bindRememberSkill(impl: RememberSkill): Skill
}
