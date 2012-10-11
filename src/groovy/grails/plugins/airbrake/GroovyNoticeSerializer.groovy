package grails.plugins.airbrake

import groovy.xml.MarkupBuilder

class GroovyNoticeSerializer {

	void toXml( Notice notice, Writer writer) {
		new MarkupBuilder(writer).notice(version: AirbrakeNotifier.AIRBRAKE_API_VERSION) {
			'api-key'(notice.apiKey)

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
							method: "${it.className}.${it.methodName}"
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

                    [params: 'params', session: 'session', cgiData: 'cgi-data'].each { property, nodeName ->
                        if (r."$property") {
                            "$nodeName" {
                                r."$property".each { k, v ->
                                    var(key: k, v)
                                }
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

				'environment-name'(notice.env)

				if (s.appVersion) {
					'app-version'(s.appVersion)
				}
                if (s.hostname) {
                    hostname(s.hostname)
                }
			}

            if (notice.user) {
                'current-user' {
                    notice.user.each {k, v ->
                        "$k"(v)
                    }
                }
            }
		}
	}
}
