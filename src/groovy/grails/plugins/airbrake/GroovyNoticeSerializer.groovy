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
					if (r.params) {
						params {
                            filterParameters(r.params, airbrakeNotifier).each { k, v ->
                                var(key: k, v)
                            }
						}
					}
					if (r.session) {
						session {
                            filterParameters(r.session, airbrakeNotifier).each { k, v ->
                                var(key: k, v)
                            }
						}
					}
					if (r.cgiData) {
						'cgi-data' {
                            filterParameters(r.cgiData, airbrakeNotifier).each { k, v ->
                                var(key: k, v)
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

    private Map filterParameters(Map params, AirbrakeNotifier airbrakeNotifier) {
        params.collectEntries { k, v ->
            def filteredValue = airbrakeNotifier.filteredKeys?.any {
                // println "key $k, filter $it"
                k =~ it } ? "FILTERED" : v.toString()
            [(k): filteredValue]
        }
    }
}
