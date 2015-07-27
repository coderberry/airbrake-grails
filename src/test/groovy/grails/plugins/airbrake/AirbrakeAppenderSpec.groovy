package grails.plugins.airbrake

import spock.lang.Specification
import ch.qos.logback.classic.spi.LoggingEvent
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import ch.qos.logback.core.spi.FilterReply

class AirbrakeAppenderSpec extends Specification {

    def mockAirbrakeNotifier = Mock(AirbrakeNotifier)
    def exception = new RuntimeException('Damn that Rabbit')

    def 'appender allows events with Error Level'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, false)

        and:
        def event = getLoggingEvent('That rascally rabbit escaped', exception)
        appender.start()

        expect:
        appender.getFilterChainDecision(event) == FilterReply.NEUTRAL
    }

    def 'appender denies events with Level less than Error'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, false)

        and:
        def level = new Level(Level.ERROR_INT -1 , "Just less than Error")
        def event = getLoggingEvent('That rascally rabbit escaped', exception, level)
        appender.start()

        expect:
        appender.getFilterChainDecision(event) == FilterReply.DENY
    }

    def 'append calls notifier if the loggingEvent has throwableInformation'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, false)

        when:
        appender.append(getLoggingEvent('That rascally rabbit escaped', exception))

        then:
        1 * mockAirbrakeNotifier.notify(exception, [errorMessage: 'That rascally rabbit escaped'])
    }

    def 'append does not call notifier if the loggingEvent has no throwableInformation'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, false)

        when:
        appender.append(getLoggingEvent('That rascally rabbit escaped'))

        then:
        0 * mockAirbrakeNotifier.notify(_, _)
    }

    def 'append calls notifier if the loggingEvent has no throwableInformation but includeEventsWithoutExceptions is true'() {
        given:
        def appender = new AirbrakeAppender(mockAirbrakeNotifier, true)

        when:
        appender.append(getLoggingEvent('That rascally rabbit escaped'))

        then:
        1 * mockAirbrakeNotifier.notify(null, [errorMessage: 'That rascally rabbit escaped'])
    }

    private LoggingEvent getLoggingEvent(String errorMessage, Throwable throwable = null, level = Level.ERROR) {
        new LoggingEvent('junk', LoggerFactory.getLogger('junk'), level, errorMessage, throwable)
    }
}
