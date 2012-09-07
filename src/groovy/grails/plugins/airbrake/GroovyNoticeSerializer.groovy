package grails.plugins.airbrake

import groovy.xml.MarkupBuilder

class GroovyNoticeSerializer implements NoticeSerializer {
	
	String serialize(AirbrakeNotifier notifier, Notice notice) {
		def writer = new StringWriter()
		serialize(notifier, notice, writer)
		writer.toString()
	}

	void serialize(AirbrakeNotifier airbrakeNotifier, Notice notice, Writer writer) {
		new MarkupBuilder(writer).notice(version: '2.1') {
			'api-key'(airbrakeNotifier.apiKey)
			
			'notifier' {
				name(AirbrakeNotifier.NOTIFIER_NAME)
				version(AirbrakeNotifier.NOTIFIER_VERSION)
				url(AirbrakeNotifier.NOTIFIER_URL)
			}

			error {
				def e = notice.error
				'class'(e.clazz)
				message(e.message)
				backtrace {
					e.backtrace.each { 
						'line'(
							file: it.fileName,
							number: it.lineNumber,
							method: it.methodName
						)
					}
				}
			}

			if (notice.request) {
				def r = notice.request
				request {
					url(r.url)
					component(r.component)
					action(r.action)
					if (r.params) {
						params {
							r.params.each { k, v ->
								var(key: k, v.toString())
							}
						}
					}
					if (r.session) {
						session {
							r.session.each { k, v ->
								var(key: k, v.toString())
							}
						}
					}
					if (r.cgiData) {
						'cgi-data' {
							r.cgiData.each { k, v ->
								var(key: k, v.toString())
							}
						}
					}					
				}
			}

			'server-environment' {
				def s = notice.serverEnvironment

				if (s.projectRoot) {
					'project-root'(s.projectRoot)
				}

				'environment-name'(airbrakeNotifier.env)

				if (s.appVersion) {
					'app-version'(s.appVersion)
				}
			}
		}
	}	
}