package grails.plugins.airbrake

import org.apache.log4j.*
import org.apache.log4j.spi.*

class AirbrakeAppender extends AppenderSkeleton {

    private boolean includeEventsWithoutExceptions

    AirbrakeAppender(AirbrakeNotifier notifier, includeEventsWithoutExceptions) {
        this.notifier = notifier
        this.includeEventsWithoutExceptions = includeEventsWithoutExceptions
        setThreshold(Level.ERROR)
    }

	private final AirbrakeNotifier notifier

	@Override
	protected void append(final LoggingEvent loggingEvent) {
		if (notifier.enabled && (loggingEvent?.throwableInformation || includeEventsWithoutExceptions) ) {
			notifier.notify(loggingEvent.message.toString(), loggingEvent.throwableInformation?.throwable)
		}
	}

	@Override
	void close() {}

	@Override
	boolean requiresLayout() {
		return false
	}
}
