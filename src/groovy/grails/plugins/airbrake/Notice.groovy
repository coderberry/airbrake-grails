package grails.plugins.airbrake

import groovy.transform.ToString
import groovy.xml.MarkupBuilder

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

    /**
     * The details of the current user. Supports id, name, email and username.
     */
    Map user

    void toXml(Writer writer) {
        new MarkupBuilder(writer).notice(version: AirbrakeNotifier.AIRBRAKE_API_VERSION) {
            'api-key'(apiKey)

            'notifier' {
                name(AirbrakeNotifier.NOTIFIER_NAME)
                version(AirbrakeNotifier.NOTIFIER_VERSION)
                url(AirbrakeNotifier.NOTIFIER_URL)
            }

            error {
                'class'(errorClass)
                message(errorMessage)
                backtrace {
                    backtrace.each {
                        'line'(
                                file: it.fileName,
                                number: it.lineNumber,
                                method: "${it.className}.${it.methodName}"
                        )
                    }
                }
            }

            if (url || component || action || params || session || cgiData) {
                request {
                    url(url)
                    component(component)
                    action(action)

                    [params: 'params', session: 'session', cgiData: 'cgi-data'].each { property, nodeName ->
                        if ("$property") {
                            "$nodeName" {
                                getProperty(property).each { k, v ->
                                    var(key: k, v)
                                }
                            }
                        }
                    }
                }
            }

            'server-environment' {
                if (projectRoot) {
                    'project-root'(projectRoot)
                }

                'environment-name'(env)

                if (appVersion) {
                    'app-version'(appVersion)
                }
                if (hostname) {
                    hostname(hostname)
                }
            }

            if (user) {
                'current-user' {
                    user.each {k, v ->
                        "$k"(v)
                    }
                }
            }
        }
    }
}

