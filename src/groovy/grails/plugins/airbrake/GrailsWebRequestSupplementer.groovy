package grails.plugins.airbrake

import org.apache.log4j.spi.*
import org.codehaus.groovy.grails.web.util.WebUtils

class GrailsWebRequestSupplementer implements NoticeSupplementer {
	Notice supplement(Notice notice, LoggingEvent event) {

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

		notice
	}
}