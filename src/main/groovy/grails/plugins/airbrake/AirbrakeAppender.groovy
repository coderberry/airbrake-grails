package grails.plugins.airbrake

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.filter.ThresholdFilter

class AirbrakeAppender<E> extends AppenderBase<E> {

    private boolean includeEventsWithoutExceptions

    AirbrakeAppender(AirbrakeNotifier notifier, includeEventsWithoutExceptions) {
        this.notifier = notifier
        this.includeEventsWithoutExceptions = includeEventsWithoutExceptions
        // Only notify on events that are Error Level or greater
        def errorFilter = new ThresholdFilter(level: Level.ERROR)
        errorFilter.start()
        this.addFilter errorFilter
    }

    private final AirbrakeNotifier notifier

    @Override
    protected void append(E event) {
        if ((event?.throwableProxy || includeEventsWithoutExceptions) ) {
            notifier.notify(event.throwableProxy?.throwable, [errorMessage: event.message.toString()])
        }
    }
}
