package org.cobbzilla.util.cron

import java.util.Properties

abstract class CronCommandBase : CronCommand {

    protected var properties: Properties

    override fun init(properties: Properties) {
        this.properties = properties
    }

}
