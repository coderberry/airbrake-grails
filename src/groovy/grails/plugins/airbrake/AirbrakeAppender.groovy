package grails.plugins.airbrake

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

class AirbrakeAppender extends AppenderSkeleton {
	
	protected final AirbrakeNotifier notifier = new AirbrakeNotifier()

	protected final List<NoticeSupplementer> supplementers = new LinkedList<NoticeSupplementer>()

	protected final List<BacktraceFilter> backtraceFilters = new LinkedList<BacktraceFilter>()

	protected final List<VarFilter> varFilters = new LinkedList<VarFilter>()

	public AirbrakeAppender() {
		setThreshold(Level.ERROR)

		addSupplementer(new ThrowableSupplementer())
		addSupplementer(new GrailsWebRequestSupplementer())
	}

	void setApi_key(String key) {
		notifier.apiKey = key
	}

	void setEnv(String env) {
		notifier.env = env
	}

	void setHost(String host) {
		notifier.host = host
	}

	void setPort(String port) {
		notifier.port = port
	}

	void setSecured(boolean secured) {
		notifier.secured = secured
	}

	protected void addSupplementer(NoticeSupplementer s) {
		supplementers.add(s)
	}

	protected void addBacktraceFilter(BacktraceFilter f) {
		backtraceFilters.add(s)
	}

	protected void addVarFilter(VarFilter f) {
		varFilters.add(s)
	}

	protected Notice buildNotice(final LoggingEvent loggingEvent) {
		supplementers.inject(new Notice()) { acc, val -> val.supplement(acc, loggingEvent) }
	}

	@Override
	protected void append(final LoggingEvent loggingEvent) {
		if (loggingEvent?.throwableInformation) {
			notifier.notify(buildNotice(loggingEvent))
		}
	}

	@Override
	public void close() {}

	@Override
	public boolean requiresLayout() {
		return false;
	}

}