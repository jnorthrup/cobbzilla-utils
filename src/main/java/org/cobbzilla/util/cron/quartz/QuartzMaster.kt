package org.cobbzilla.util.cron.quartz

import org.cobbzilla.util.cron.CronCommand
import org.cobbzilla.util.cron.CronDaemon
import org.cobbzilla.util.cron.CronJob
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import java.util.TimeZone

import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger

class QuartzMaster : CronDaemon {

    private var scheduler: Scheduler? = null
    var timeZone: TimeZone? = null

    var jobs: List<CronJob>? = null

    @Throws(Exception::class)
    override fun start() {
        scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler!!.start()

        if (jobs != null) {
            for (job in jobs!!) {
                addJob(job)
            }
        }
    }

    @Throws(SchedulerException::class)
    override fun addJob(job: CronJob) {
        val id = job.id

        val specialJob = Job { context ->
            val map = context.mergedJobDataMap
            try {
                val command = job.commandInstance
                command.init(job.properties)
                command.exec(map)
            } catch (e: Exception) {
                throw JobExecutionException(e)
            }
        }

        val jobDetail = newJob(specialJob.javaClass).withIdentity(id!! + JOB_SUFFIX).build()

        var builder = newTrigger().withIdentity(id + TRIGGER_SUFFIX)
        if (job.isStartNow) builder = builder.startNow()
        val cronSchedule = cronSchedule(job.cronTimeString!!)
        val trigger = builder.withSchedule(if (timeZone != null) cronSchedule.inTimeZone(timeZone) else cronSchedule).build()

        scheduler!!.scheduleJob(jobDetail, trigger)
    }

    @Throws(Exception::class)
    override fun removeJob(id: String) {
        scheduler!!.deleteJob(JobKey(id + JOB_SUFFIX))
    }

    @Throws(Exception::class)
    override fun stop() {
        scheduler!!.shutdown()
    }

    companion object {

        private val CAL_SUFFIX = "_calendar"
        private val JOB_SUFFIX = "_jobDetail"
        private val TRIGGER_SUFFIX = "_trigger"
    }
}
