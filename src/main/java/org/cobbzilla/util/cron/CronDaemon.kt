package org.cobbzilla.util.cron

interface CronDaemon {

    @Throws(Exception::class)
    fun start()

    @Throws(Exception::class)
    fun stop()

    @Throws(Exception::class)
    fun addJob(job: CronJob)

    @Throws(Exception::class)
    fun removeJob(id: String)

}