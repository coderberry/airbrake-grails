package grails.plugins.airbrake.test

class AirbrakeTestController {
    def airbrakeService

    static defaultAction = "thrownException"
    def beforeInterceptor = {
        session.myval = "Something in the Session!"
        session.password = "super_secret_password"
        true
    }

    def thrownException(String message) {
        def realMessage = message ?: "ThrownException"
        throw new Exception("${realMessage} ${System.currentTimeMillis()}")
    }

    def loggedException(String message) {
        def realMessage = message ?: "LoggedException"
        log.error("${realMessage} ${System.currentTimeMillis()}", new Exception("SubLogged Exception ${System.currentTimeMillis()}"))
    }

    def loggedMessage(String message) {
        def realMessage = message ?: "LoggedMessage"
        log.error("${realMessage} ${System.currentTimeMillis()}")
    }

    def trappedException(String message) {
        def realMessage = message ?: "TrappedException"
        def exception = new Exception("${realMessage} ${System.currentTimeMillis()}")
        airbrakeService.notify(exception)
    }

    def errorMessage(String message) {
        def realMessage = message ?: "ErrorMessage"
        airbrakeService.notify(null, [errorMessage: "${realMessage} ${System.currentTimeMillis()}"])
    }

    def customContext(String message, String customComponent, String customAction) {
        message = message ?: "CustomContext"
        customComponent = customComponent ?: 'CustomComponent'
        customAction = customAction ?: 'CustomAction'
        def time = System.currentTimeMillis()
        airbrakeService.addNoticeContext(customComponent, customAction, [time: time])
        airbrakeService.notify(null, [errorMessage: "${message} ${time}"])
    }
}
