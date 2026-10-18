package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessageDocument
import com.example.data.model.CoverLetterDocument
import com.example.data.model.CvTemplate
import com.example.data.model.ResumeDocument
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class KungaCvRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun resolveUserId(explicitUserId: String?): String {
        return explicitUserId?.takeIf { it.isNotBlank() } ?: requireUserId()
    }

    // --- RESUMES ---

    fun observeResumes(userId: String? = null): Flow<List<ResumeDocument>> = flow {
        val uid = resolveUserId(userId)
        val path = "users/$uid/resumes"
        emitAll(
            db.collection("users").document(uid).collection("resumes")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ResumeDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreEx = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreEx != null) {
                        handleFirestoreError(firestoreEx, OperationType.LIST, path)
                        throw firestoreEx
                    }
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getUserResumes(userId: String? = null): Result<List<ResumeDocument>> {
        val uid = try {
            resolveUserId(userId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/resumes"
        return try {
            val snapshot = db.collection("users").document(uid).collection("resumes")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val items = snapshot.toObjects(
                ResumeDocument::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun getResumeById(resumeId: String, ownerUserId: String? = null): Result<ResumeDocument> {
        val uid = try {
            resolveUserId(ownerUserId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid).collection("resumes").document(resumeId)
        return try {
            val snap = docRef.get().await()
            val doc = snap.toObject(
                ResumeDocument::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: throw NoSuchElementException("Resume not found: $resumeId")
            Result.success(doc)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createResume(resume: ResumeDocument): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val collectionRef = db.collection("users").document(uid).collection("resumes")
        val docRef = if (resume.id.isNotBlank()) {
            collectionRef.document(resume.id)
        } else {
            collectionRef.document()
        }
        val validTemplateId = CvTemplate.fromId(resume.templateId).id
        val payload = mapOf(
            "userId" to uid,
            "title" to resume.title.ifBlank { "Untitled Resume" }.take(120),
            "templateId" to validTemplateId,
            "fullName" to resume.fullName.ifBlank { "Your Name" }.take(120),
            "jobTitle" to resume.jobTitle.take(120),
            "email" to resume.email.take(160),
            "phone" to resume.phone.take(60),
            "location" to resume.location.take(120),
            "website" to resume.website.take(200),
            "summary" to resume.summary.take(3000),
            "experiences" to resume.experiences.take(25),
            "educations" to resume.educations.take(15),
            "skills" to resume.skills.map { it.take(80) }.take(50),
            "projects" to resume.projects.take(20),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateResume(resume: ResumeDocument): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        require(resume.id.isNotBlank()) { "Resume ID must not be blank for update" }
        val docRef = db.collection("users").document(uid).collection("resumes").document(resume.id)
        val validTemplateId = CvTemplate.fromId(resume.templateId).id
        val updates = mapOf(
            "title" to resume.title.ifBlank { "Untitled Resume" }.take(120),
            "templateId" to validTemplateId,
            "fullName" to resume.fullName.ifBlank { "Your Name" }.take(120),
            "jobTitle" to resume.jobTitle.take(120),
            "email" to resume.email.take(160),
            "phone" to resume.phone.take(60),
            "location" to resume.location.take(120),
            "website" to resume.website.take(200),
            "summary" to resume.summary.take(3000),
            "experiences" to resume.experiences.take(25),
            "educations" to resume.educations.take(15),
            "skills" to resume.skills.map { it.take(80) }.take(50),
            "projects" to resume.projects.take(20),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteResume(resumeId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid).collection("resumes").document(resumeId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- COVER LETTERS ---

    fun observeCoverLetters(userId: String? = null): Flow<List<CoverLetterDocument>> = flow {
        val uid = resolveUserId(userId)
        val path = "users/$uid/coverLetters"
        emitAll(
            db.collection("users").document(uid).collection("coverLetters")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CoverLetterDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveCoverLetter(letter: CoverLetterDocument): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val validTone = if (letter.tone in listOf("Executive", "Impactful", "Conversational")) {
            letter.tone
        } else {
            "Executive"
        }
        val collectionRef = db.collection("users").document(uid).collection("coverLetters")
        return if (letter.id.isBlank()) {
            val docRef = collectionRef.document()
            val payload = mapOf(
                "userId" to uid,
                "resumeId" to letter.resumeId.take(128),
                "companyName" to letter.companyName.ifBlank { "Target Company" }.take(120),
                "targetRole" to letter.targetRole.ifBlank { "Target Role" }.take(120),
                "jobDescription" to letter.jobDescription.take(5000),
                "tone" to validTone,
                "content" to letter.content.ifBlank { " " }.take(12000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
                Result.success(docRef.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                Result.failure(e)
            }
        } else {
            val docRef = collectionRef.document(letter.id)
            val updates = mapOf(
                "resumeId" to letter.resumeId.take(128),
                "companyName" to letter.companyName.ifBlank { "Target Company" }.take(120),
                "targetRole" to letter.targetRole.ifBlank { "Target Role" }.take(120),
                "jobDescription" to letter.jobDescription.take(5000),
                "tone" to validTone,
                "content" to letter.content.ifBlank { " " }.take(12000),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.update(updates).await()
                Result.success(letter.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteCoverLetter(letterId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid).collection("coverLetters").document(letterId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- CHAT MESSAGES ---

    fun observeChatMessages(userId: String? = null): Flow<List<ChatMessageDocument>> = flow {
        val uid = resolveUserId(userId)
        val path = "users/$uid/chatMessages"
        emitAll(
            db.collection("users").document(uid).collection("chatMessages")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ChatMessageDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun appendChatMessage(message: ChatMessageDocument): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid).collection("chatMessages").document()
        val validRole = if (message.role == "model") "model" else "user"
        val payload = mapOf(
            "userId" to uid,
            "role" to validRole,
            "text" to message.text.ifBlank { "..." }.take(12000),
            "modelTier" to message.modelTier.ifBlank { "gemini-3.8-flash" }.take(60),
            "suggestedSummary" to message.suggestedSummary.take(3000),
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun clearChatHistory(userId: String? = null): Result<Unit> {
        val uid = try {
            resolveUserId(userId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/chatMessages"
        return try {
            val snap = db.collection("users").document(uid).collection("chatMessages")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            for (doc in snap.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
