package grails.plugins.airbrake

import groovy.transform.ToString
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.exceptions.DefaultStackTraceFilterer
import org.codehaus.groovy.grails.exceptions.StackTraceFilterer
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

@ToString(includeNames = true)
class Notice {

    /**
     * The throwable that caused this notice
     */
    Throwable throwable

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
    Backtrace[] backtrace

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
     * A list of key names or regular expression use to filter parameters, session and cgiData
     */
    List<String> filteredKeys

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
     * The hostname of the machine where the exception occurred
     */
    String hostname

    /**
     * The details of the current user. Supports id, name, email and username.
     */
    Map user

    /**
     * The name of the notifier sending this notice
     */
    String notifierName

    /**
     * The version of the notifier sending this notice
     */
    String notifierVersion

    /**
     * The url of the notifier sending this notice
     */
    String notifierUrl

    /**
     * Arguments supplied in the constructor, with defaults from the NoticeContextHolder
     */
    private final Map args

    private StackTraceFilterer stackTraceFilterer = new DefaultStackTraceFilterer()

    Notice(Map args =[:]) {
        args = getArgsWithDefaults(args)
        def webRequest = (GrailsWebRequest) RequestContextHolder.requestAttributes

        this.args = args
        this.throwable = filterStackTrace(args.throwable)
        this.apiKey = args.apiKey
        this.projectRoot = args.projectRoot

        this.notifierName = args.notifierName
        this.notifierVersion = args.notifierVersion
        this.notifierUrl = args.notifierUrl

        this.filteredKeys = args.filteredKeys
        this.params = args.params ?: webRequest?.parameterMap

        this.url = args.url ?: constructUrl(webRequest)
        this.component = args.component ?: args.controller ?: webRequest?.controllerName
        this.action = args.action ?: webRequest?.actionName

        this.env = args.env
        this.cgiData = args.cgiData ?: getCgiData(webRequest)
        this.session = args.session ?: getSessionData(webRequest)
        this.backtrace = parseBacktrace(throwable?.stackTrace ?: args.backtrace)
        this.errorClass = throwable?.class?.name ?: args.errorClass
        // Grails creates a really long error message for uncaught exceptions. Essentially a combination of all the webRequest meta data.
        // However it creates very unhelpful messages for airbrake so we just prefer the simpler message on the throwable
        // This means for exceptions logged by the app code like log.error(message, exception) that we ignore the supplied message.
        // This is less than idea (we expect the user supplied message to be useful) but by the time the Appender has the details we
        // cannot distinguish between the caught and uncaught cases.
        this.errorMessage = throwable?.message ?: args.errorMessage
        this.hostname = args.hostname ?: InetAddress.localHost.hostName
        this.user = args.user

        applyFilters()
    }


    private Throwable filterStackTrace(Throwable unfilteredThrowable) {
        unfilteredThrowable ? stackTraceFilterer.filter(unfilteredThrowable) : unfilteredThrowable
    }

    void toXml(Writer writer) {
        new MarkupBuilder(writer).notice(version: AirbrakeNotifier.AIRBRAKE_API_VERSION) {
            'api-key'(apiKey)

            'notifier' {
                name(notifierName)
                version(notifierVersion)
                url(notifierUrl)
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

    /**
     * Coverts the Notice to a Map or primitives (Maps, Lists and basic types)
     * Extremely useful if you want to serialize the Notice as we can serialize the map and then call the
     * constructor after deserialization and get the same Notice
     * @return a Map representing this Notice
     */
    Map toMap() {
        [
            apiKey: apiKey,
            projectRoot: projectRoot,
            notifierName: notifierName,
            notifierVersion: notifierVersion,
            notifierUrl: notifierUrl,
            filteredKeys: filteredKeys,
            url: url,
            component: component,
            action: action,
            params: params,
            env: env,
            cgiData: cgiData,
            session: session,
            backtrace: backtrace*.toMap().toArray(),
            errorClass: errorClass,
            errorMessage: errorMessage,
            hostname: hostname,
            user: user
        ]
    }

    private Backtrace[] parseBacktrace(backtrace) {
        backtrace.collect { new Backtrace(it) }.toArray()
    }

    private Map getArgsWithDefaults(args) {
        def argsWithDefaults = (Map)NoticeContextHolder.noticeContext?.clone() ?: [:]
        if (args) {
            argsWithDefaults << args
        }
        argsWithDefaults
    }

    private Map getCgiData(webRequest) {
        def request = webRequest?.request
        def data = [:]
        request?.headerNames?.each { data[it] =  request.getHeader(it) }
        data
    }

    private Map getSessionData(webRequest) {
        def session = webRequest?.session
        def data = [:]
        session?.attributeNames?.each { data[it] = session.getAttribute(it) }
        data
    }

    private String constructUrl(GrailsWebRequest webRequest) {
        if (!webRequest) {
            return
        }
        def request = webRequest.currentRequest
        def origUrl = request.forwardURI

        def urlPath = origUrl
        if (request.queryString) {
            urlPath += '?' + request.queryString
        }

        def defaultPort = request.scheme == 'https' ? 443 : 80

        def url
        if (defaultPort == request.serverPort) {
            url = new URL(request.scheme, request.serverName, urlPath)
        } else {
            url = new URL(request.scheme, request.serverName, request.serverPort, urlPath)
        }

        url.toString()
    }

    private addSupplementerDetails() {
        supplementers.each {
            it.supplement(this)
        }
    }

    private applyFilters() {
        ['session', 'cgiData', 'params'].each {
           setProperty(it, filterParameters(getProperty(it)) )
        }
    }

    private Map filterParameters(Map params) {
        params?.collectEntries { k, v ->
            def filteredValue = filteredKeys?.any { k =~ it } ? "[FILTERED]" : v.toString()
            [(k): filteredValue]
        }
    }
}

