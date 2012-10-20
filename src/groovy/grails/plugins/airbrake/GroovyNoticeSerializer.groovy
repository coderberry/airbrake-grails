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
				'class'(notice.errorClass)
				message(notice.errorMessage)
				backtrace {
					notice.backtrace.each {
						'line'(
							file: it.fileName,
							number: it.lineNumber,
							method: "${it.className}.${it.methodName}"
						)
					}
				}
			}

			if (notice.url || notice.component || notice.action || notice.params || notice.session || notice.cgiData) {
				request {
					url(notice.url)
					component(notice.component)
					action(notice.action)

                    [params: 'params', session: 'session', cgiData: 'cgi-data'].each { property, nodeName ->
                        if (notice."$property") {
                            "$nodeName" {
                                notice."$property".each { k, v ->
                                    var(key: k, v)
                                }
                            }
                        }
                    }
				}
			}

			'server-environment' {
				if (notice.projectRoot) {
					'project-root'(notice.projectRoot)
				}

				'environment-name'(notice.env)

				if (notice.appVersion) {
					'app-version'(notice.appVersion)
				}
                if (notice.hostname) {
                    hostname(notice.hostname)
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
