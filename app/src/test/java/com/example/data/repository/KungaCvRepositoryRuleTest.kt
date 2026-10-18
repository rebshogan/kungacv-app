package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ChatMessageDocument
import com.example.data.model.CoverLetterDocument
import com.example.data.model.ResumeDocument
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KungaCvRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveResume_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KungaCvRepository(firestore, auth)

        val starter = ResumeDocument.createExecutiveStarter(
            userId = uid,
            candidateName = "Alice Vance",
            candidateEmail = ALICE_EMAIL
        )
        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createResume(starter)
        }
        assertTrue("Expected createResume to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)
        val resumeId = createResult.getOrThrow()
        assertTrue(resumeId.isNotEmpty())

        val emittedResumes = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeResumes(uid).first { list -> list.any { it.id == resumeId } }
        }
        assertTrue(emittedResumes.any { it.id == resumeId && it.fullName == "Alice Vance" })
    }

    @Test
    fun getResumeById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = KungaCvRepository(firestore, auth)
        val starter = ResumeDocument.createExecutiveStarter(aliceUid, "Alice", ALICE_EMAIL)
        val resumeId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createResume(starter).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = KungaCvRepository(firestore, auth)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getResumeById(resumeId, ownerUserId = aliceUid)
        }
        assertTrue("Expected cross-user read to fail", crossReadResult.isFailure)
        val ex = crossReadResult.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull("Expected FirebaseFirestoreException", ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeResumes_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = KungaCvRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeResumes(aliceUid).first()
            }
            fail("Expected FirebaseFirestoreException for unauthenticated user")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun coverLetterAndChatMessage_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KungaCvRepository(firestore, auth)

        val letterResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveCoverLetter(
                CoverLetterDocument(
                    userId = uid,
                    resumeId = "res_1",
                    companyName = "Linear",
                    targetRole = "Staff Android Engineer",
                    jobDescription = "Build high-craft mobile experiences.",
                    tone = "Executive",
                    content = "Dear Linear Team, I am thrilled to apply..."
                )
            )
        }
        assertTrue("Expected saveCoverLetter to succeed", letterResult.isSuccess)

        val chatResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.appendChatMessage(
                ChatMessageDocument(
                    userId = uid,
                    role = "user",
                    text = "Review my executive summary",
                    modelTier = "gemini-3.8-flash",
                    suggestedSummary = ""
                )
            )
        }
        assertTrue("Expected appendChatMessage to succeed", chatResult.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@kungacv.test"
        const val BOB_EMAIL = "bob@kungacv.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
