package grails.plugins.airbrake

import groovy.transform.ToString

@ToString(includeNames = true)
class Notice {
    String apiKey
    String env
	Error error = new Error()
	Request request = new Request()
	ServerEnvironment serverEnvironment = new ServerEnvironment()
    Map user
}

@ToString(includeNames = true)
class Error {
	String clazz
	String message
	StackTraceElement[] backtrace 
}

@ToString(includeNames = true)
class Request {
	String url
	String component
	String action
	Map params
	Map session
	Map cgiData
}

@ToString(includeNames = true)
class ServerEnvironment {
	String projectRoot
	String appVersion
    String hostname
}