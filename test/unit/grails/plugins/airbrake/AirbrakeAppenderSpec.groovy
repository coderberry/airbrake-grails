package grails.plugins.airbrake

import spock.lang.Specification
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.LogManager
import org.apache.log4j.Priority

class AirbrakeAppenderSpec extends Specification {

    def mockAirbrakeNotifier = Mock(AirbrakeNotifier)
    def exception = new RuntimeException('Damn that Rabbit')

    def 'append has a threshold of error'() {
        expect:
        new AirbrakeAppender(mockAirbrakeNotifier, false).threshold == Priority.ERROR
    }

    def 'append calls notifier if the loggingEvent has throwableInformation'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, false)
        mockAirbrakeNotifier.enabled >> true

        when:
        appender.append(getLoggingEvent('That rascally rabbit escaped', exception))

        then:
        1 * mockAirbrakeNotifier.notify('That rascally rabbit escaped', exception)
    }

    def 'append does not call notifier if its disabled'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, false)
        mockAirbrakeNotifier.enabled >> false

        when:
        appender.append(getLoggingEvent('That rascally rabbit escaped', exception))

        then:
        0 * mockAirbrakeNotifier.notify(_, _)
    }

    def 'append does not call notifier if the loggingEvent has no throwableInformation'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, false)
        mockAirbrakeNotifier.enabled >> true

        when:
        appender.append(getLoggingEvent('That rascally rabbit escaped'))

        then:
        0 * mockAirbrakeNotifier.notify(_, _)
    }

    def 'append calls notifier if the loggingEvent has no throwableInformation but includeEventsWithoutExceptions is true'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, true)
        mockAirbrakeNotifier.enabled >> true

        when:
        appender.append(getLoggingEvent('That rascally rabbit escaped'))

        then:
        1 * mockAirbrakeNotifier.notify('That rascally rabbit escaped', null)
    }

    private LoggingEvent getLoggingEvent(String errorMessage, Throwable throwable = null) {
        new LoggingEvent('junk', LogManager.getLogger('junk'), Priority.ERROR, errorMessage, throwable)
    }
}
