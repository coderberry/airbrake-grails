package grails.plugins.airbrake

interface VarFilter {
	boolean accept(String key, String value)
}