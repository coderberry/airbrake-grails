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
     * If the testOnStartup config param is enabled, send a test notice on startup and disable notice sending
     * if this test fails
     */
    @PostConstruct
    void testAirbrake() {

        Configuration config = airbrakeNotifier.configuration

        if (config.testOnStartup) {
            def noticeOptions = [throwable: new TestAirbrakeAvailableException()]
            Notice testNotice = airbrakeNotifier.buildNotice(noticeOptions)

            log.info "Sending test notification with API key '$config.apiKey' to host '$config.host'"
            boolean testNoticeSuccessful = airbrakeNotifier.sendToAirbrake(testNotice)
            setEnabled(testNoticeSuccessful)
        }
    }

    void setEnabled(boolean enabled) {
        airbrakeNotifier.configuration.enabled = enabled
    }
}

class TestAirbrakeAvailableException extends Exception {
    TestAirbrakeAvailableException() {
        super('Dummy notice to check if Airbrake is available')
    }
}
