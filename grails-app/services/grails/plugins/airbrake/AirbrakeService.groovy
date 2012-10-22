package grails.plugins.airbrake

class AirbrakeService {
    static transactional = false

    AirbrakeNotifier airbrakeNotifier

    void notify(String errorMessage, Throwable throwable = null) {
        airbrakeNotifier.notify(throwable, [errorMessage: errorMessage])
    }

    void addNoticeContext(String component, String action, Map params = [:]) {
        NoticeContextHolder.addNoticeContext(component, action, params)
    }

    void addNoticeContext(Map context) {
        NoticeContextHolder.addNoticeContext(context)
    }
}
