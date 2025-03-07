package net.primal.networking.sockets

typealias SocketConnectionOpenedCallback = (url: String) -> Unit
typealias SocketConnectionClosedCallback = (url: String, error: Throwable?) -> Unit
