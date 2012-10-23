package grails.plugins.airbrake.test

import grails.plugins.airbrake.Notice
import org.quartz.JobExecutionContext

class AirbrakeNotifyJob {
    def airbrakeService

    static triggers = { }

    def execute(JobExecutionContext context) {
        Notice notice = context.mergedJobDataMap.notice
        if (notice) {
            airbrakeService.sendToAirbrake(notice)
        }
    }
}