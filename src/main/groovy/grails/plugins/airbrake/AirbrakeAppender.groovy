package grails.plugins.airbrake

import ch.qos.logback.classic.Level
import ch.qos.logback.core.OutputStreamAppender


class AirbrakeAppender<E> extends OutputStreamAppender<E> {

    private boolean includeEventsWithoutExceptions
    private final AirbrakeNotifier notifier

    AirbrakeAppender(AirbrakeNotifier notifier, includeEventsWithoutExceptions) {
        this.notifier = notifier
        this.includeEventsWithoutExceptions = includeEventsWithoutExceptions
    }


    @Override
    protected void subAppend(E event) {
        if ((event?.throwableInformation || includeEventsWithoutExceptions) ) {
            notifier.notify(event.throwableInformation?.throwable, [errorMessage: event.message.toString()])
        }
    }

}
