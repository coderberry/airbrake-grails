package grails.plugins.airbrake

class Notice {
    String apiKey
    String env
	Error error = new Error()
	Request request = new Request()
	ServerEnvironment serverEnvironment = new ServerEnvironment()
    Map user
}

class Error {
	String clazz
	String message
	StackTraceElement[] backtrace 
}

class Request {
	String url
	String component
	String action
	Map params
	Map session
	Map cgiData
}

class ServerEnvironment {
	String projectRoot
	String appVersion
    String hostname
}