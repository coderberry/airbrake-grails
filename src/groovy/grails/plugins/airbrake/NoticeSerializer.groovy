package grails.plugins.airbrake

interface NoticeSerializer {
	String serialize(AirbrakeNotifier notifier, Notice notice)
	void serialize(AirbrakeNotifier notifier, Notice notice, Writer writer)
}