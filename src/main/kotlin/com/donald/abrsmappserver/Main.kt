package com.donald.abrsmappserver

import com.donald.abrsmappserver.server.Server
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.serverless.AppLoader
import org.http4k.serverless.GoogleCloudHttpFunction

class GoogleCloudFunction : GoogleCloudHttpFunction(Server)

class MainFunction : HttpFunction {
    override fun service(request: HttpRequest, response: HttpResponse) {
        val function = GoogleCloudFunction()
        function.service(request, response)
    }
}

fun main(args: Array<String>) {
    Server.run()
}