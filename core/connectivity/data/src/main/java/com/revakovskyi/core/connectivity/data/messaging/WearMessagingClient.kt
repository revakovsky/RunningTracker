package com.revakovskyi.core.connectivity.data.messaging

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.core.connectivity.domain.messaging.MessagingClient
import com.revakovskyi.core.connectivity.domain.messaging.MessagingError
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.core.domain.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A messaging client implementation for Wear OS that facilitates communication between
 * connected devices (e.g., phone and watch). This client uses the Wearable MessageClient
 * to send and receive messages.
 *
 * @param context The context of the application used to initialize the Wearable MessageClient.
 */
class WearMessagingClient(context: Context) : MessagingClient {

    private val client = Wearable.getMessageClient(context)

    private val messageQueue = mutableListOf<MessagingAction>()
    private var connectedNodeId: String? = null

    /**
     * Connects to a Wear OS node and listens for incoming messages. Messages are received
     * as [MessagingAction] objects.
     *
     * @param nodeId The ID of the node to connect to.
     * @return A [Flow] emitting incoming [MessagingAction]s from the node.
     *
     * The method starts listening for messages matching the specified base path. Any
     * actions in the [messageQueue] are sent after connection.
     */
    override fun connectToNode(nodeId: String): Flow<MessagingAction> {
        connectedNodeId = nodeId

        return callbackFlow {
            val listener: (MessageEvent) -> Unit = { event ->
                if (event.path.startsWith(BASE_PATH_MESSAGING_ACTION)) {
                    val json = event.data.decodeToString()
                    val action = Json.decodeFromString<MessagingActionDto>(json)
                    trySend(action.toDomainAction())
                }
            }

            client.addListener(listener)

            messageQueue.forEach { sendOrQueueAction(it) }
            messageQueue.clear()

            awaitClose { client.removeListener(listener) }
        }
    }

    /**
     * Sends a messaging action to the connected node. If no node is connected, the action
     * is queued for sending later.
     *
     * @param action The [MessagingAction] to be sent.
     * @return [EmptyDataResult] representing the result of the operation:
     * - [Result.Success] if the message is successfully sent.
     * - [Result.Error] if an error occurs or no node is connected.
     *
     * If the client is disconnected, the action is added to a queue that will
     * be processed once a connection is established.
     */
    override suspend fun sendOrQueueAction(action: MessagingAction): EmptyDataResult<MessagingError> {
        return connectedNodeId?.let { id ->
            try {
                val json = Json.encodeToString(action.toDto())
                client.sendMessage(id, BASE_PATH_MESSAGING_ACTION, json.encodeToByteArray()).await()
                Result.Success(Unit)
            } catch (e: ApiException) {
                Result.Error(
                    if (e.status.isInterrupted) MessagingError.CONNECTION_INTERRUPTED
                    else MessagingError.UNKNOWN
                )
            }
        } ?: run {
            messageQueue.add(action)
            Result.Error(MessagingError.DISCONNECTED)
        }
    }


    /***
     * The base path for messaging actions used in this client
     */
    companion object {
        private const val BASE_PATH_MESSAGING_ACTION = "running_tracker/messaging_action"
    }

}
