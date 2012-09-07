package grails.plugins.airbrake

interface BacktraceFilter {
	boolean accept(StackTraceElement element)
}