package org.cobbzilla.util.cron

import java.util.Properties

interface CronCommand {

    fun init(properties: Properties)

    @Throws(Exception::class)
    fun exec(context: Map<String, Any>)

}
