package org.cobbzilla.util.cron

import org.cobbzilla.util.reflect.ReflectionUtil

import java.util.Properties

class CronJob {

    var id: String? = null

    var cronTimeString: String? = null

    var isStartNow = false

    var commandClass: String? = null
    var properties = Properties()

    // todo
    //    @Getter @Setter private String user;
    //    @Getter @Setter private String shellCommand;

    val commandInstance: CronCommand
        get() {
            val command = ReflectionUtil.instantiate<CronCommand>(commandClass)
            command.init(properties)
            return command
        }
}
