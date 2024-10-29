package dev.frozenmilk.rpcrc

import dev.frozenmilk.rpcrc.handling.HandlingFuture
import dev.frozenmilk.rpcrc.handling.ResponseHandler
import dev.frozenmilk.rpcrc.message.error.RPCRCError
import dev.frozenmilk.rpcrc.message.request.Request
import dev.frozenmilk.rpcrc.message.response.Response
import dev.frozenmilk.rpcrc.message.serialization.MessageDeserializer
import dev.frozenmilk.rpcrc.reserved.rpc.disconnect.DisconnectHandler
import dev.frozenmilk.rpcrc.reserved.rpc.disconnect.DisconnectRequest
import dev.frozenmilk.rpcrc.reserved.rpc.ping.PingHandler
import dev.frozenmilk.rpcrc.reserved.rpc.ping.PingRequest
import dev.frozenmilk.rpcrc.routing.Router
import dev.frozenmilk.rpcrc.serialization.Serializable
import dev.frozenmilk.util.genqueue.GenQueue
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Supplier

class Connection(private val executor: ExecutorService, val router: Router, val socket: SocketChannel) {
	init {
		socket.configureBlocking(true)
	}

	@Volatile
	private var lastReceived = System.nanoTime()

	/**
	 * starts at 2 min 30 sec
	 */
	@Volatile
	var timeOutNano = (150 * 1E9).toLong();
	var timeOut: Double
		get() = (timeOutNano / 1E9)
		set(value) { timeOutNano = (value * 1E9).toLong() }

	val connectionActive: Boolean
		get() = !shutdown && (System.nanoTime() - lastReceived) < timeOutNano

	/**
	 * no longer sending further requests, internal workings will continue while the [shutdown] process continues
	 *
	 * becomes true when [close] is called
	 */
	@Volatile
	var closed = false
		private set

	/**
	 * occurs after [closed], becomes true once [close] has finished
	 */
	@Volatile
	var shutdown = false
		private set

	@Volatile
	private var runningProcess: Thread? = null

	fun open() : Thread {
		synchronized(this) {
			if (runningProcess != null) throw RuntimeException("Connection already started")
			val ping = PingRequest(ByteBuffer.allocateDirect(0))
			val buf = ByteBuffer.allocateDirect(0)
			return Thread {
				while (!shutdown) {
					//if (socket.read(buf) == -1) {
				//		if (!pingAlive(ping)) break
				//		continue
				//	}
					try {
						handleConnection(socket)
						lastReceived = System.nanoTime()
					}
					catch (e: Throwable) {
						e.printStackTrace()
						//if (e is RPCRCError) send(e)
						publicClose { false }
						totalClose()
					}
				}
			}.also {
				it.start()
				runningProcess = it
			}
		}
	}

	/**
	 * returns if to continue or not
	 */
	private fun pingAlive(pingRequest: PingRequest): Boolean {
		if (!connectionActive) { // todo second thread might still be best / a read with timeout
			if (closed || shutdown) {
				return false
			} // don't want to request close again if it already is, we'll stop reading now

			// pretend new info has arrived for the moment
			lastReceived = System.nanoTime()

			submit {
				try {
					sendRequest(PingHandler, pingRequest).get(this.timeOutNano, TimeUnit.NANOSECONDS)
				}
				catch (_: TimeoutException) {
					publicClose { false }
					totalClose()
				}
			}
		}
		return true
	}

	private fun handleConnection(socket: SocketChannel) {
		MessageDeserializer.promote(
			this,
			null,
			socket
		)
	}

	internal fun send(message: Serializable) {
		if (shutdown) throw ClosedConnectionException()
		synchronized(socket) {
			val buf = ByteBuffer.allocateDirect(message.size())
			message.serialize(buf)
			socket.write(buf.rewind())
			println("wrote $message")
		}
	}

	/**
	 * sends a disconnect request, waits for the line to go quiet / it times out, then closes
	 */
	fun close() {
		if (closed) throw ClosedConnectionException()
		synchronized(this) {
			val disconnect = sendRequest(DisconnectHandler, DisconnectRequest())
			publicClose { !disconnect.isDone }
			if (!disconnect.isDone) disconnect.cancel(false)
			totalClose()
		}
	}

	/**
	 * @see close
	 *
	 * closes this connection to public usage, allowing us to finish shutdown
	 */
	internal fun publicClose(timeOut: Supplier<Boolean> = Supplier { true }) {
		if (closed) throw ClosedConnectionException()
		// 1. close connection to new operations
		// 2. wait for line to go quiet, or, we time out
		// 2.1 what does this look like?
		// 2.2 -> all operations are completed
		// 2.3 -> we are timedOut
		synchronized(this) {
			closed = true // closes connection to new, non-internal operations
			while (!isEmpty && connectionActive && timeOut.get()) { // waits for line to go quiet, or timeOut
			}
		}
	}

	/**
	 * follows [publicClose]
	 */
	internal fun totalClose() {
		if (shutdown) throw ClosedConnectionException()
		synchronized(this) {
			shutdown = true
			socket.use {
				runningProcess?.interrupt()
				runningProcess = null
			}
		}
	}

	//
	// CallbackIDs
	//

	private val callbackOwnership = mutableMapOf<UShort, HandlingFuture<*>>()
	@Volatile
	private var isEmpty = true
	private val callbackGenQueue = run {
		val last = (UShort.MAX_VALUE - 1u).toUShort()
		GenQueue(1u.toUShort()) { prev ->
			if (prev == last) throw RuntimeException()
			prev.inc()
		}
	}

	/**
	 * assigns `callback_id` header of [request], then sends it
	 */
	fun <REQUEST: Request<*>, RESPONSE: Response<*>> sendRequest(handler: ResponseHandler<RESPONSE>, request: REQUEST): CompletableFuture<RESPONSE> {
		if (closed) throw ClosedConnectionException()
		val id: UShort
		val future = CompletableFuture<RESPONSE>()
		synchronized(callbackOwnership) {
			id = callbackGenQueue.next()
			callbackOwnership[id] = HandlingFuture(handler, future)
			isEmpty = callbackOwnership.isEmpty()
		}
		request.callbackID = id
		send(request)
		return future
	}

	fun <RESPONSE: Response<*>> sendResponse(response: RESPONSE) {
		if (closed) throw ClosedConnectionException()
		send(response)
	}

	fun freeCallbackID(id: UShort) {
		if(id == 0u.toUShort()) return // can't recycle the broadcast id
		synchronized(callbackOwnership) {
			callbackOwnership.remove(id)
			callbackGenQueue.giveBack(id)
			isEmpty = callbackOwnership.isEmpty()
		}
	}

	operator fun get(callbackID: UShort): HandlingFuture<*>? = synchronized(callbackOwnership) { callbackOwnership[callbackID] }

	//
	// Futures
	//

	internal fun <T> submit(callable: Callable<T>) : Future<T> {
		val future = executor.submit(callable)
		return future
	}
}