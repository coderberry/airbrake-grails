package grails.plugins.airbrake

import javax.annotation.PostConstruct

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

    /**
     * Send a test notice to airbrake
     * @return the outcome of the test
     */
    boolean testAirbrake() {

        Configuration config = airbrakeNotifier.configuration

        if (config.enabled) {
            def noticeOptions = [throwable: new TestAirbrakeAvailableException()]
            Notice testNotice = airbrakeNotifier.buildNotice(noticeOptions)

            log.info "Sending test notification with API key '$config.apiKey' to host '$config.host'"
            airbrakeNotifier.sendToAirbrake(testNotice)

        } else {
            log.warn "Cannot test Airbrake because plugin is disabled"
        }
    }

    void setEnabled(boolean enabled) {
        log.info "airbrake notifier ${enabled ? 'enabled' : 'disabled'}"
        airbrakeNotifier.configuration.enabled = enabled
    }
}

class TestAirbrakeAvailableException extends Exception {
    TestAirbrakeAvailableException() {
        super('Dummy notice to check if Airbrake is available')
    }
}
