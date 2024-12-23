package com.revakovskyi.core.connectivity.data

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import com.revakovskyi.core.connectivity.domain.DeviceNode
import com.revakovskyi.core.connectivity.domain.DeviceType
import com.revakovskyi.core.connectivity.domain.NodeDiscovery
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * A class responsible for discovering connected Wear OS devices capable of running the
 * corresponding app (e.g., phone app discovering the watch app or vice versa).
 *
 * @param context The application context used to initialize the Wearable CapabilityClient.
 */
class WearNodeDiscovery(context: Context) : NodeDiscovery {

    private val capabilityClient = Wearable.getCapabilityClient(context)


    /**
     * Observes the connected devices compatible with the specified [DeviceType].
     * This method continuously listens for changes in connected devices and emits
     * updates through a [Flow].
     *
     * @param localDeviceType The type of the local device (e.g., [DeviceType.WATCH] or [DeviceType.PHONE]).
     * @return A [Flow] emitting a [Set] of [DeviceNode], representing the currently connected devices.
     *
     * ### Behavior:
     * - Determines the required capability for the remote device based on the [localDeviceType].
     * - Queries the current state of connected nodes with the specified capability.
     * - Starts listening for updates to the capability information and emits changes as they occur.
     *
     * ### Listener:
     * - A [CapabilityInfo] listener is registered to track updates to the connected devices:
     *   ```kotlin
     *   val listener: (CapabilityInfo) -> Unit = { capabilityInfo ->
     *       trySend(
     *           capabilityInfo.nodes
     *               .map { it.toDeviceNode() } // Map updated nodes to DeviceNode
     *               .toSet()
     *       )
     *   }
     *   ```
     *   - This listener reacts to changes in the capability information, such as when
     *     new nodes become available or existing nodes disconnect.
     *   - It maps the updated `nodes` to domain-specific `DeviceNode` objects and sends
     *     the updated set through the [Flow].
     *
     * ### Example Use Case:
     * A phone running the app can use this method to discover watches that have the
     * corresponding Wear app installed and connected, or vice versa.
     */
    override fun observeConnectedDevices(localDeviceType: DeviceType): Flow<Set<DeviceNode>> {
        return callbackFlow {
            val remoteCapability: String = when (localDeviceType) {
                DeviceType.WATCH -> "running_tracker_phone_app"
                DeviceType.PHONE -> "running_tracker_wear_app"
            }

            try {
                val capability: CapabilityInfo = capabilityClient
                    .getCapability(remoteCapability, CapabilityClient.FILTER_REACHABLE)
                    .await()

                val connectedDevices: Set<DeviceNode> = capability.nodes
                    .map { it.toDeviceNode() }
                    .toSet()

                send(connectedDevices)
            } catch (e: ApiException) {
                awaitClose()
                return@callbackFlow
            }

            val listener: (CapabilityInfo) -> Unit = { capabilityInfo ->
                trySend(
                    capabilityInfo.nodes
                        .map { it.toDeviceNode() }
                        .toSet()
                )
            }

            capabilityClient.addListener(listener, remoteCapability)

            awaitClose { capabilityClient.removeListener(listener) }
        }
    }

}
