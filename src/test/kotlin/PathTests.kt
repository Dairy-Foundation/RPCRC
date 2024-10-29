import dev.frozenmilk.rpcrc.parse.toUTF8
import dev.frozenmilk.rpcrc.routing.path
import org.junit.Assert
import org.junit.Test

class PathTests {
	@Test
	fun simple() {
		var start = System.nanoTime()
		val producedParser = path("/:id/wow/*rest")
		println("compiled parser: ${(System.nanoTime() - start) / 1e6}ms")
		var total = 0L
		val iters = Int.MAX_VALUE / 5000
		for (i in 0 until iters) {
			start = System.nanoTime()
			val matches = producedParser.run { "/a/wow/a/a/a/a".toUTF8().parse() }?.first!!
			val duration = System.nanoTime() - start
			total += duration
			println("parsed path: ${(duration) / 1e6}ms")
			val id = matches.apply("id") // 'a'
			val rest = matches.apply("rest") // 'a/a/a/a'
			Assert.assertEquals("a", id)
			Assert.assertEquals("a/a/a/a", rest)
		}
		println("average: ${(total / iters.toDouble()) / 1e6}ms")
		println("iters: $iters")
	}

	@Test
	fun demo() {
		// endpoint matching
		val producedParser = path("/:id/wow/*rest")
		// testing endpoint "/a/wow/a"
		val matches = producedParser.run { "/a/wow/a/a/a/a/a".toUTF8().parse() }?.first!!
		val id = matches.apply("id") // 'a'
		val rest = matches.apply("rest") // 'a'
		Assert.assertEquals("a", id)
		Assert.assertEquals("a/a/a/a/a", rest)
	}

	@Test
	fun root() {
		val producedParser = path("/my/:cool/path")
		val matches = producedParser.run { "/my/not-so-cool/path".toUTF8().parse() }?.first!!
		val id = matches.apply("cool") // 'not-so-cool'
		Assert.assertEquals("not-so-cool", id)
	}
}