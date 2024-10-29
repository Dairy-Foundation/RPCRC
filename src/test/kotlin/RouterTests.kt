import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.handling.RequestHandler
import dev.frozenmilk.rpcrc.message.request.PartialRequest
import dev.frozenmilk.rpcrc.message.request.Request
import dev.frozenmilk.rpcrc.parse.DECODER
import dev.frozenmilk.rpcrc.parse.Parser
import dev.frozenmilk.rpcrc.parse.toUTF8
import dev.frozenmilk.rpcrc.reserved.rpc.RPCRouter
import dev.frozenmilk.rpcrc.reserved.rpc.ping.PingHandler
import dev.frozenmilk.rpcrc.reserved.rpc.ping.PingRequest
import dev.frozenmilk.rpcrc.routing.Router
import dev.frozenmilk.rpcrc.routing.path
import dev.frozenmilk.rpcrc.serialization.Promoter
import dev.frozenmilk.rpcrc.serialization.Serializable
import org.junit.Assert
import org.junit.Test
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Function

class RouterTests {
	@Test
	fun buildSimpleRouter() {
		val testHandler1 = TestHandler(path("/"))
		val testHandler2 = TestHandler(path("/test"))
		val router = Router()
				.route(testHandler1)
				.route(testHandler2)

		Assert.assertEquals(testHandler1, router["/"])
		Assert.assertEquals(testHandler2, router["/test"])
		Assert.assertEquals(null, router["/err"])
	}

	/**
	 * opens two connections, sends 100 pings from one to the other, then closes the connection
	 */
	@Test
	fun pings() {
		val router = RPCRouter()

		val exec = Executors.newWorkStealingPool()
		val averageTime = DoubleArray(100)
		exec.submit {
			val serverSocket = ServerSocketChannel.open()
			serverSocket.bind(InetSocketAddress("0.0.0.0", 8000))
			serverSocket.configureBlocking(true)
			var max = 0u.toUShort()
			val socket = serverSocket.accept()
			val connection = Connection(exec, router, socket)
			connection.open()
			val path = "/rpc/ping".toUTF8()
			val futures = (1 .. 1).map {
				val request = PingRequest(path, "$it|${System.nanoTime()}".toUTF8())
//				Thread.sleep(1)
				val future = connection.sendRequest(PingHandler, request)
					.handle { pingResponse, err ->
						if (pingResponse != null) {
							val str = DECODER.decode(pingResponse.data)
							val split = str.split('|')
							val time = (System.nanoTime() - split[1].toLong()) / 1e9
							averageTime[it - 1] = time
							println("ping response in $time sec | cbi ${pingResponse.callbackID} | no. ${split[0]}")
						}
						err?.printStackTrace()
					}
				println("sending ping | cbi ${request.callbackID} | no. $it")
				max = maxOf(max, request.callbackID)
				future
			}
			try {
				CompletableFuture.allOf(*futures.toTypedArray()).get()
			}
			catch (e: Throwable) {
				e.printStackTrace()
			}
			println("max cbi was $max")
			averageTime.sort()
			println("mean rtt was ${averageTime.sum() / 100}")
			println("median rtt was ${averageTime[49]} ${averageTime[50]}")
			connection.close()
		}

		Thread.sleep(150)

		val socket = SocketChannel.open(InetSocketAddress("0.0.0.0", 8000))
		val connection = Connection(Executors.newWorkStealingPool(), router, socket)
		connection.open()
		while (!connection.shutdown) {}
	}
}

private class TestHandler(override val path: Parser<Function<String, String?>>) : RequestHandler<Request<Serializable>> {
	override val requestPromoter: Promoter<PartialRequest, Request<Serializable>>
		get() = TODO("Not yet implemented")

	override fun handleRequest(
		connection: Connection,
		captures: Function<String, String?>,
		request: Request<Serializable>
	) {
		TODO("Not yet implemented")
	}
}