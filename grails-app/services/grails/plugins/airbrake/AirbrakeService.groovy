package grails.plugins.airbrake

class AirbrakeService {
    static transactional = false

    AirbrakeNotifier airbrakeNotifier

    void notify(String errorMessage, Throwable throwable = null) {
        airbrakeNotifier.notify(errorMessage, throwable)
    }
}
