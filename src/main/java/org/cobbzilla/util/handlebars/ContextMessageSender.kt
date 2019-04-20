package org.cobbzilla.util.handlebars

interface ContextMessageSender {

    fun send(recipient: String, subject: String, message: String, contentType: String)

}
