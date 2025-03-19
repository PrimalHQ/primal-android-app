package net.primal.core.networking.sockets

typealias SocketConnectionOpenedCallback = (url: String) -> Unit
typealias SocketConnectionClosedCallback = (url: String, error: Throwable?) -> Unit
