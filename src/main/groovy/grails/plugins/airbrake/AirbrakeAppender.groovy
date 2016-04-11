package grails.plugins.airbrake

import ch.qos.logback.core.AppenderBase



class AirbrakeAppender<E> extends AppenderBase<E> {

    private boolean includeEventsWithoutExceptions
    private final AirbrakeNotifier notifier

    AirbrakeAppender(AirbrakeNotifier notifier, includeEventsWithoutExceptions) {
        this.notifier = notifier
        this.includeEventsWithoutExceptions = includeEventsWithoutExceptions
    }


    @Override
    protected void append(E event) {
        if ((event?.throwableInformation || includeEventsWithoutExceptions) ) {
            notifier.notify(event.throwableInformation?.throwable, [errorMessage: event.message.toString()])
        }
    }

}
