package net.primal.domain.polls

open class PollVoteException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class PollAlreadyVotedException : PollVoteException()

class PollExpiredException : PollVoteException()

class PollAuthorCannotVoteException : PollVoteException()

class PollMissingLightningAddressException : PollVoteException()

class PollNotFoundException : PollVoteException()
