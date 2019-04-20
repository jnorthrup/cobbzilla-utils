package org.cobbzilla.util.string

import org.slf4j.Logger

import java.util.ResourceBundle

abstract class ResourceMessages {

    protected val bundleName: String
        get() = "labels/" + javaClass.simpleName

    val bundle = ResourceBundle.getBundle(bundleName)

    // todo: add support for locale-specific bundles and messages
    fun translate(messageTemplate: String): String {
        var messageTemplate = messageTemplate

        // strip leading/trailing curlies if they are there
        while (messageTemplate.startsWith("{")) messageTemplate = messageTemplate.substring(1)
        while (messageTemplate.endsWith("}")) messageTemplate = messageTemplate.substring(0, messageTemplate.length - 1)

        try {
            return bundle.getString(messageTemplate)
        } catch (e: Exception) {
            log.error("translate: Error looking up $messageTemplate: $e")
            return messageTemplate
        }

    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(ResourceMessages::class.java)
    }
}
