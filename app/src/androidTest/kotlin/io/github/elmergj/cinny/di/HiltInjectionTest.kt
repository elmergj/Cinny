package io.github.elmergj.cinny.di

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

import io.github.elmergj.cinny.domain.repository.NoteRepository
import io.github.elmergj.cinny.domain.repository.FolderRepository
import io.github.elmergj.cinny.domain.repository.ReminderRepository

import javax.inject.Inject

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.ext.junit.runners.AndroidJUnit4

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltInjectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var noteRepository: NoteRepository

    @Inject
    lateinit var folderRepository: FolderRepository

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun repositoriesAreInjected() {
        assertNotNull(noteRepository)
        assertNotNull(folderRepository)
        assertNotNull(reminderRepository)
    }
}