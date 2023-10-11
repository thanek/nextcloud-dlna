package net.schowek.nextclouddlna.controller

import mu.KLogging
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class ErrorHandler {
    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    fun handleNoHandlerFound(e: NoHandlerFoundException, request: WebRequest): HashMap<String, String> {
        val response = HashMap<String, String>()
        response["status"] = "fail"
        response["message"] = e.localizedMessage
        logger.info { "404 from $request (${e.requestURL})" }
        return response
    }

    companion object : KLogging()
}