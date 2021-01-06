
/*
 * Groovy script for OpenAM JWK URI
 *
 * This script requires these arguments: userId, password, openamUrl
 */
import org.forgerock.http.protocol.Request
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status
import org.forgerock.http.handler.HttpClientHandler
import org.forgerock.util.AsyncFunction
import org.forgerock.http.Client

import static org.forgerock.http.protocol.Response.newResponsePromise

class Global {
    static Object JwkInfo
}

/**
 * Creates unauthorized error
 */
def getUnauthorizedError() {
    logger.info("Returning Unauthorized error")
    Response errResponse = new Response(Status.UNAUTHORIZED)
    errResponse.entity.json = [code: 401, reason: "Unauthorized", message: "Authentication Failed"];

    // Need to wrap the response object in a promise
    return newResponsePromise(errResponse)
}

def performOpenAMJwkURI() {

    def customHttp = new Client(new HttpClientHandler())
    
    // Invoke OpenAM authentication
    Request jwkInfo = new Request()
    jwkInfo.uri = "${openamUrl}/oauth2/realms/root/connect/jwk_uri"
    jwkInfo.headers.put('Cache-Control', 'no-cache')
    jwkInfo.method = "GET"

    logger.info("Getting JWK info using AM endpoint: ${jwkInfo.uri}")

    return customHttp.send(context, jwkInfo)
        // when there will be a response available ...
        // Need to use 'thenAsync' instead of 'then' because we'll return another promise, not directly a response
        .thenAsync(jwkResponse -> {
        logger.info("JWK Response : ${jwkResponse.entity.json}")
        if (jwkResponse.entity.json != null) {
            Global.JwkInfo = jwkResponse.entity.json
            // If cookie validation succeeds and has valid uid
            logger.info("Retrieved JWK info successfully, Invoking next handler")
            return next.handle(context, request)
        } else {
            logger.info("Retrieving JWK info Failed")
            // In case of any failure like server exception etc, return UNAUTHORIZED status. This can be modified to return specific response status for different failures.
            // No need to call next.handle() as we want to terminate handing here
            return getUnauthorizedError()
        }
    }, throwableEx -> {
	    logger.info("A Throwable exception Occured !!! {} ", throwableEx.getMessage())
	    return getUnauthorizedError()
    }, runtimeEx -> {
	    logger.info("A Runtime exception Occured !!! {} ", runtimeEx.getMessage())
	    return getUnauthorizedError()
    })
}

// Check if JWK info is already present
if (Global.JwkInfo != null) {
    logger.info("JWK info token found, calling next handler")
    return next.handle(context, request)
} else {
    return performOpenAMJwkURI()
}