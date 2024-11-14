package com.revakovskyi.core.connectivity.domain.messaging

import com.revakovskyi.core.domain.util.Error

enum class MessagingError : Error {

    CONNECTION_INTERRUPTED,
    DISCONNECTED,
    UNKNOWN,

}