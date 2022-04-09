package com.donald.abrsmappserver.utils

import com.donald.abrsmappserver.utils.music.new.Pitch
import com.donald.abrsmappserver.utils.music.new.PitchFifths
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse

class Test : HttpFunction {

    override fun service(request: HttpRequest, response: HttpResponse) {
        val builder = StringBuilder()
        repeat(10) { index ->
            val pitch = Pitch(PitchFifths(index), 4)
            builder.append(pitch.toString()).append(" ")
        }
        response.writer.write(builder.toString())
    }

}