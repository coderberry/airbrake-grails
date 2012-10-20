package grails.plugins.airbrake

import groovy.transform.ToString

@ToString(includeNames = true)
class Notice {
    /**
     * The API Key for the project to send the notification for
     */
    String apiKey

    /**
     * The environment that the exception occurred in (such as 'Production')
     */
    String env

    /**
     * The name of the class of the exception (such as 'RuntimeException')
     */
    String errorClass

    /**
     * The error message
     */
    String errorMessage

    /**
     * the backtrace for the exception
     */
    StackTraceElement[] backtrace

    /**
     * the url of the web request that generated the exception (if any)
     */
    String url

    /**
     * the component that was is use when the exception occurred (usually the controller)
     */
    String component

    /**
     * The action that was in use when the exception occurred (usually the controller action)
     */
    String action

    /**
     * A Map of parameters for the web request the exception occurred in (from query string of post body)
     */
    Map params

    /**
     * A Map of session data for the web request the exception occurred in
     */
    Map session

    /**
     * A Map of CGI variables for the web request the exception occurred in (for example USER_AGENT or REFERER)
     */
    Map cgiData

    /**
     * The path to the project root
     */
    String projectRoot

    /**
     * The version of the app that the exception occurred in
     */
    String appVersion

    /**
     * The hostname of the machine where the exception occured
     */
    String hostname

    Map user
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
