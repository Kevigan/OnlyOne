package com.onlyone.app.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.onlyone.app.dao.SavedChainsDao
import com.onlyone.app.data.SavedChainEntity
import com.onlyone.app.data.chainMessage.Chain
import com.onlyone.app.data.chainMessage.ChainStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserChainRepo @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val savedChainsDao: SavedChainsDao,
) {
    private val io = Dispatchers.IO
    private val chainsCol get() = firestore.collection("chains")
    private val functions = Firebase.functions("europe-west3")

    private val TAG = "UserChainRepo"

    // ───────────────────── Cloud Functions ─────────────────────

    suspend fun createChain(
        title: String?,
        targetLength: Int,
        firstAssigneeUid: String,
        hiddenHistory: Boolean = false,
        ttlHours: Int = 72,                 // optional now; CF ignores it if you kept 24h fixed
        firstMessage: String,
        authorName: String?                 // ← NEW
    ): String = withContext(io) {
        val payload = hashMapOf(
            "title" to title,
            "targetLength" to targetLength,
            "firstAssignee" to firstAssigneeUid,
            "hiddenHistory" to hiddenHistory,
            "ttlHours" to ttlHours,                 // safe to keep / or remove
            "firstMessage" to firstMessage.take(200),
            "authorName" to (authorName ?: "")      // ← NEW
        )
        val res = functions.getHttpsCallable("createChain").call(payload).await().data as Map<*, *>
        val ok = res["success"] as? Boolean ?: false
        if (!ok) error("createChain failed")
        (res["chainId"] as? String) ?: error("createChain: missing chainId")
    }

    suspend fun contributeAndPass(
        chainId: String,
        text: String,
        nextAssigneeUid: String?,
        authorName: String?                 // ← NEW
    ) = withContext(io) {
        val payload = hashMapOf(
            "chainId" to chainId,
            "text" to text,
            "authorName" to (authorName ?: "")      // ← NEW
        ).apply { if (nextAssigneeUid != null) put("nextAssignee", nextAssigneeUid) }

        val res = functions.getHttpsCallable("contributeAndPass").call(payload).await().data as Map<*, *>
        val ok = res["success"] as? Boolean ?: false
        if (!ok) error("contributeAndPass failed")
    }

    suspend fun rerouteAssignee(
        chainId: String,
        newAssigneeUid: String
    ) = withContext(io) {
        Log.d(TAG, "🌀 rerouteAssignee(): chainId=$chainId newAssignee=$newAssigneeUid")
        val payload = mapOf("chainId" to chainId, "newAssignee" to newAssigneeUid)
        val res = functions
            .getHttpsCallable("rerouteAssignee")
            .call(payload)
            .await()
            .data as Map<*, *>

        Log.d(TAG, "✅ rerouteAssignee(): response=$res")
        val ok = res["success"] as? Boolean ?: false
        if (!ok) error("rerouteAssignee failed")
    }

    // ───────────────────── Firestore reads ─────────────────────

    fun observeMyChains(uid: String): Flow<List<Chain>> = callbackFlow {
        Log.d(TAG, "👀 observeMyChains(): uid=$uid")
        val q = chainsCol
            .whereArrayContains("participants", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val reg = q.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e(TAG, "🔥 observeMyChains(): error=${err.message}", err)
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }
            val count = snap?.size() ?: 0
            Log.d(TAG, "📦 observeMyChains(): got $count docs")
            val items = snap?.documents.orEmpty().mapNotNull { d ->
                d.toObject<Chain>()?.copy(id = d.id)
            }
            trySend(items).isSuccess
        }
        awaitClose { reg.remove() }
    }

    fun observeAssignedChains(uid: String): Flow<List<Chain>> = callbackFlow {
        Log.d(TAG, "👀 observeAssignedChains(): uid=$uid")
        val q = chainsCol
            .whereEqualTo("currentAssignee", uid)
            .orderBy("lastAssignAt", Query.Direction.DESCENDING)

        val reg = q.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e(TAG, "🔥 observeAssignedChains(): error=${err.message}", err)
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }
            val count = snap?.size() ?: 0
            Log.d(TAG, "📦 observeAssignedChains(): got $count docs")
            val items = snap?.documents.orEmpty().mapNotNull { d ->
                d.toObject<Chain>()?.copy(id = d.id)
            }
            trySend(items).isSuccess
        }
        awaitClose { reg.remove() }
    }

    suspend fun getChain(chainId: String): Chain? = withContext(io) {
        Log.d(TAG, "🔍 getChain(): chainId=$chainId")
        val d = chainsCol.document(chainId).get().await()
        if (!d.exists()) {
            Log.w(TAG, "⚠️ getChain(): doc not found")
            return@withContext null
        }
        val chain = d.toObject<Chain>()?.copy(id = d.id)
        Log.d(TAG, "✅ getChain(): success id=${chain?.id}")
        chain
    }

    suspend fun getStepsOnce(chainId: String): List<ChainStep> = withContext(io) {
        Log.d(TAG, "🔍 getStepsOnce(): chainId=$chainId")
        val res = chainsCol.document(chainId)
            .collection("steps")
            .orderBy("index", Query.Direction.ASCENDING)
            .get()
            .await()

        Log.d(TAG, "✅ getStepsOnce(): steps=${res.size()}")
        res.documents.mapNotNull { it.toObject<ChainStep>() }
    }

    // ───────────── Observables: chain, steps, combined ─────────────

    fun observeChain(chainId: String): Flow<Chain?> = callbackFlow {
        Log.d("testBlas", "👀 observeChain(): start chainId=$chainId")
        val reg = chainsCol.document(chainId).addSnapshotListener { d, err ->
            if (err != null) {
                Log.e(TAG, "🔥 observeChain(): error=${err.message}", err)
                trySend(null).isSuccess
                return@addSnapshotListener
            }
            if (d == null || !d.exists()) {
                Log.w(TAG, "⚠️ observeChain(): doc missing id=$chainId")
                trySend(null).isSuccess
                return@addSnapshotListener
            }
            val chain = d.toObject<Chain>()?.copy(id = d.id)
            Log.d(TAG, "✅ observeChain(): doc loaded id=$chainId participants=${chain?.participants?.size}")
            trySend(chain).isSuccess
        }
        awaitClose {
            Log.d(TAG, "🧹 observeChain(): closed for id=$chainId")
            reg.remove()
        }
    }

    fun observeSteps(chainId: String): Flow<List<ChainStep>> = callbackFlow {
        Log.d(TAG, "👀 observeSteps(): start chainId=$chainId")
        val reg = chainsCol.document(chainId)
            .collection("steps")
            .orderBy("index", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "🔥 observeSteps(): error=${err.message}", err)
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }
                val count = snap?.size() ?: 0
                Log.d(TAG, "✅ observeSteps(): steps=$count for chain=$chainId")
                val items = snap?.toObjects(ChainStep::class.java) ?: emptyList()
                trySend(items).isSuccess
            }
        awaitClose {
            Log.d(TAG, "🧹 observeSteps(): closed for id=$chainId")
            reg.remove()
        }
    }

    fun observeChainAndSteps(chainId: String): Flow<Pair<Chain, List<ChainStep>>?> =
        combine(observeChain(chainId), observeSteps(chainId)) { chain, steps ->
            Log.d(TAG, "🔄 combine(): chain=${chain?.id ?: "null"} steps=${steps.size}")
            if (chain == null) null else chain to steps
        }

    // ───────────────────── Local save (Room) ─────────────────────

    private fun joinMessage(title: String?, stepTexts: List<String>): String {
        // Title as first line (if present), then each step on its own line.
        val lines = buildList {
            if (!title.isNullOrBlank()) add(title.trim())
            stepTexts.forEach { add(it) }
        }
        return lines.joinToString(separator = "\n")
    }

    private fun toIsoDayUtc(epochMillis: Long): String {
        // Store date only: YYYY-MM-DD in UTC
        val utc = java.time.ZoneOffset.UTC
        return java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(utc).toLocalDate().toString()
    }

    fun observeSavedChains(): Flow<List<SavedChainEntity>> =
        savedChainsDao.observeAll()

    suspend fun upsertSavedChain(entity: SavedChainEntity) =
        savedChainsDao.upsert(entity)

    suspend fun deleteSavedChain(chainId: String) =
        savedChainsDao.delete(chainId)

    suspend fun getSavedChain(chainId: String): SavedChainEntity? =
        savedChainsDao.get(chainId)

}
