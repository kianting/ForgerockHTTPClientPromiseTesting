import org.forgerock.http.Client
import org.forgerock.http.handler.HttpClientHandler
import org.forgerock.http.protocol.Request
import org.forgerock.util.AsyncFunction
import org.forgerock.http.protocol.Status

def getjwks(jwksurl) {
	def http = new Client(new HttpClientHandler())
	def req = new Request()
	req.method = "GET"
	req.uri=jwksurl
	req.headers['accept'] = "application/json"
	return http.send(req)
}

println "Beginning of test code"


def sJwks
return getjwks("https://httpbin.org/get").then { response -> 
	if(response != null && !response.entity.isRawContentEmpty()) {
		sJwks = response.entity.toString()
		if(Status.OK == response.status) {
			println("Success response: " + sJwks)
		}else {
			println("Failure response: " + sJwks)
		}
		response.close()
	}else {
		println "Response is empty!!!"
	}
}.thenAsync({
       println("Here we call the next.handle(context, request)")
	   println("sJwks is still reacheable : " + sJwks)
} as AsyncFunction)
	   

println "This line will never be reached!!!!"
