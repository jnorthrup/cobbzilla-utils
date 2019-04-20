package org.cobbzilla.util.mustache

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
class MustacheResourceNotFoundException(message: String) : RuntimeException(message) {
    companion object {

        private val LOG = LoggerFactory.getLogger(MustacheResourceNotFoundException::class.java)
    }
}
