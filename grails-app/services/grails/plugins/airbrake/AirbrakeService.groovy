package grails.plugins.airbrake

class AirbrakeService {
    static transactional = false

    AirbrakeNotifier airbrakeNotifier

    void notify(Throwable throwable, Map options = [:]) {
        airbrakeNotifier.notify(throwable, options)
    }

    void addNoticeContext(String component, String action, Map params = [:]) {
        NoticeContextHolder.addNoticeContext(component, action, params)
    }

    void addNoticeContext(Map context) {
        NoticeContextHolder.addNoticeContext(context)
    }

    void clearNoticeContext() {
        NoticeContextHolder.clearNoticeContext()
    }

    /**
     * Synchronously send the Notice to Airbrake
     * @param notice
     */
    void sendToAirbrake(Notice notice) {
        airbrakeNotifier.sendToAirbrake(notice)
    }
}
