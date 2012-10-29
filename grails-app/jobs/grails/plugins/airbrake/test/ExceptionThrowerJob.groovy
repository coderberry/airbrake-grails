package grails.plugins.airbrake.test

class ExceptionThrowerJob {
    def airbrakeService

    static triggers = {
        simple name: 'exceptionThrowingTrigger', startDelay: 10000, repeatInterval: 300000
    }

    def execute() {
        def time = System.currentTimeMillis()
        airbrakeService.clearNoticeContext()
        airbrakeService.addNoticeContext([component: 'ExceptionThrowerJob', action: 'execute', params: [time: time]])
        airbrakeService.notify(null, [errorMessage: "ExceptionThrower: $time"])
    }
}
