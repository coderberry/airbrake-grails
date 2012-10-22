package grails.plugins.airbrake

import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

class GrailsNoticeBuilder {
    String apiKey
    String env
    List<String> filteredKeys
    List<NoticeSupplementer> supplementers

    protected GrailsNoticeBuilder() {}

    GrailsNoticeBuilder(String apiKey, String env, List<String> filteredKeys, List<NoticeSupplementer> supplementers) {
        this.apiKey = apiKey
        this.env = env
        this.filteredKeys = filteredKeys ?: []
        this.supplementers = supplementers ?: []
    }

    Notice buildNotice(String errorMessage, Throwable throwable) {
        def args = [apiKey: apiKey, env: env]
        args << getErrorDetails(errorMessage, throwable)
        args << getWebRequestDetails()
        args << (NoticeContextHolder.noticeContext ?: [:])
        def notice = new Notice(args)
        addSupplementerDetails(notice)
        filter(notice)
        notice
    }

    def getErrorDetails(String errorMessage, Throwable throwable) {
        // Grails creates a really long error message for uncaught exceptions. Essentially a combination of all the webRequest meta data.
        // However it creates very unhelpful messages for airbrake so we just prefer the simpler message on the throwable
        // This means for exceptions logged by the app code like log.error(message, exception) that we ignore the supplied message.
        // This is less than idea (we expect the user supplied message to be useful) but by the time the Appender has the details we
        // cannot distinguish between the caught and uncaught cases.
        def errorDetails = [errorMessage: throwable?.message ?: errorMessage]

        if (throwable) {
            errorDetails << [
                errorClass: throwable.class.name,
                backtrace: throwable.stackTrace
            ]
        }
        errorDetails
    }

    def addSupplementerDetails(Notice notice) {
        supplementers.each {
            it.supplement(notice)
        }
    }


    def getWebRequestDetails() {
        def requestDetails = [hostname: InetAddress.localHost.hostName]

		def webRequest = (GrailsWebRequest) RequestContextHolder.requestAttributes

		if(webRequest) {
            def request = webRequest.currentRequest
            requestDetails << [
                    url: constructUrl(request),
                    component: webRequest.controllerName,
                    action: webRequest.actionName,
                    params: webRequest.parameterMap,
            ]

			def session = request.getSession(false)

			if (session) {
                requestDetails.session = [:]
                session.attributeNames.each { requestDetails.session[it] = session.getAttribute(it) }
			}

            requestDetails.cgiData = [:]
            request.headerNames.each { requestDetails.cgiData[it] =  request.getHeader(it) }
		}

        requestDetails
	}

    private String constructUrl(HttpServletRequest request) {
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

    private filter(Notice notice) {
        ['session', 'cgiData', 'params'].each {
            notice."$it" = filterParameters(notice."$it")
        }
    }

    private Map filterParameters(Map params) {
        params?.collectEntries { k, v ->
            def filteredValue = filteredKeys?.any { k =~ it } ? "[FILTERED]" : v.toString()
            [(k): filteredValue]
        }
    }
}