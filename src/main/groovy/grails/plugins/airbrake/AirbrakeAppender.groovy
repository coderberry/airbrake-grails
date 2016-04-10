package grails.plugins.airbrake

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.joran.spi.ConsoleTarget
import ch.qos.logback.core.util.EnvUtil
import ch.qos.logback.core.util.OptionHelper


class AirbrakeAppender<E> extends AppenderBase<E> {

    private boolean includeEventsWithoutExceptions
    private final AirbrakeNotifier notifier

    AirbrakeAppender(AirbrakeNotifier notifier, includeEventsWithoutExceptions) {
        this.notifier = notifier
        this.includeEventsWithoutExceptions = includeEventsWithoutExceptions
    }


    @Override
    protected void append(E event) {
        if ((event?.getThrowableProxy()?.getThrowable() || includeEventsWithoutExceptions)) {
            notifier.notify(event.getThrowableProxy().getThrowable(), [errorMessage: event.getMessage()])
        }
    }

}
