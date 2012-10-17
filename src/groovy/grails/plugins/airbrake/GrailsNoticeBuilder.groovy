package grails.plugins.airbrake

import org.codehaus.groovy.grails.web.util.WebUtils

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
        def notice = new Notice(apiKey: apiKey, env: env)
        addErrorDetails(notice, errorMessage, throwable)
        addWebRequestDetails(notice)
        addSupplementerDetails(notice)
        filter(notice)
        notice
    }

    def addErrorDetails(Notice notice, String errorMessage, Throwable throwable) {
        def message = throwable?.message ?: errorMessage
        Error error = new Error(
                message: message
        )

        if (throwable) {
            error.clazz = throwable.class.name
            error.backtrace = throwable.stackTrace
        }
        notice.error = error
    }

    def addSupplementerDetails(Notice notice) {
        supplementers.each {
            it.supplement(notice)
        }
    }

    def addWebRequestDetails(Notice notice) {
		def webRequest

		try {
			webRequest = WebUtils.retrieveGrailsWebRequest()
		} catch (Exception e) {
		}

		if (webRequest) {
			def request = webRequest.currentRequest
			def origUrl = request.forwardURI ?: request.requestURL.toString()

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

			def r = new Request(
				url: url.toString(),
				component: webRequest.controllerName,
				action: webRequest.actionName,
				params: webRequest.parameterMap
			)

			def session = request.getSession(false)

			if (session) {
				r.session = session.attributeNames.collect({ [(it): session[it] ] }).sum()
			}

            r.cgiData = [
                HTTP_USER_AGENT: request.getHeader('User-Agent')
            ]

			notice.request = r

            notice.serverEnvironment.hostname = InetAddress.localHost.hostName
		}
	}

    private filter(Notice notice) {
        ['session', 'cgiData', 'params'].each {
            notice.request."$it" = filterParameters(notice.request."$it")
        }
    }

    private Map filterParameters(Map params) {

        def filteredParams = [:]

        params.each {k, v ->
            def filteredValue = filteredKeys?.any { k =~ it } ? "FILTERED" : v.toString()
            filteredParams[k] = filteredValue
        }
        filteredParams
    }
}