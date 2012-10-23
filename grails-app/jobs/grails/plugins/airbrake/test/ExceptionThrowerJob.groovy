package grails.plugins.airbrake.test

class ExceptionThrowerJob {
    def airbrakeService

    static triggers = {
        simple name: 'exceptionThrowingTrigger', startDelay: 5000, repeatInterval: 60000
    }

    def execute() {
        def time = System.currentTimeMillis()
        airbrakeService.clearNoticeContext()
        airbrakeService.addNoticeContext([component: 'ExceptionThrowerJob', action: 'execute', params: [time: time]])
        throw new RuntimeException("ExceptionThrower: $time")
    }
}
