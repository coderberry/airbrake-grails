package grails.plugins.airbrake

import org.apache.log4j.spi.*
import org.codehaus.groovy.grails.web.util.WebUtils

class GrailsWebRequestSupplementer implements NoticeSupplementer {
	Notice supplement(Notice notice, LoggingEvent event) {

		def webRequest = null

		try { 
			webRequest = WebUtils.retrieveGrailsWebRequest()
		} catch (Exception e) {
		}

		if (webRequest) {
			def request = webRequest.currentRequest
			def origUrl = request['javax.servlet.forward.request_uri'] ?: request.requestURL.toString()

			def r = new Request(
				url: "${request.scheme}://${request.serverName}:${request.serverPort}/${origUrl}?${request.queryString}",
				component: webRequest.controllerName,
				action: webRequest.actionName,
				params: webRequest.parameterMap
			)

			def session = request.getSession(false)

			if (session) {
				r.session = session.attributeNames.collect({ [(it): session[it] ] }).sum()
			}

			notice.request = r
		}

		notice
	}
}