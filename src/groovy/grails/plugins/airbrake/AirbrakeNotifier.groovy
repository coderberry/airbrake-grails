package grails.plugins.airbrake

import groovy.transform.ToString
import groovy.util.logging.Log4j

@ToString(includeNames=true)
@Log4j
class AirbrakeNotifier {
    def grailsApplication

    static final String AIRBRAKE_API_VERSION = '2.2'
	static final String AIRBRAKE_HOST = 'airbrakeapp.com'
	static final String AIRBRAKE_PATH = '/notifier_api/v2/notices'

	static final String NOTIFIER_NAME = 'grails-airbrake'
	static final String NOTIFIER_VERSION = '0.9.1'
	static final String NOTIFIER_URL = 'https://github.com/cavneb/airbrake-grails'

    final Configuration configuration

    // mostly to make mocking easier in specs
    protected AirbrakeNotifier() {}

    AirbrakeNotifier(Configuration configuration) {
        this.configuration = configuration
    }

    void notify(Throwable throwable, Map options = [:]) {
        // if we're not enabled don't go through the effort of building the message
        if (configuration.enabled) {
            options.throwable = throwable
            sendNotice(buildNotice(options))
        }
    }

    Notice buildNotice(Map options) {
        new Notice(configuration.merge(options))
    }

	void sendNotice(Notice notice) {
        if (configuration.async) {
            configuration.async(notice, grailsApplication)
        } else {
            sendToAirbrake(notice)
        }
	}

    def sendToAirbrake(Notice notice) {
        if (!configuration.enabled) {
            return
        }
        if (!notice.apiKey) {
            throw new RuntimeException("The API key for the project this error is from is required. Get this from the project's page in airbrake.")
        }

        log.debug "Sending Notice ${notice} to airbrake"

        HttpURLConnection conn

        try {
            conn = buildConnection()

            if (log.debugEnabled) {
                log.debug "Sending notice data to ${conn.getURL()}"

                def stringWriter = new StringWriter()
                notice.toXml(stringWriter)
                log.debug "$stringWriter"
            }

            conn.outputStream.withWriter { outputWriter ->
                notice.toXml(outputWriter)
            }

            int responseCode = conn.responseCode
            String responseMessage = conn.responseMessage

            if (responseCode in 200..<300) {
                log.debug "Received successful HTTP response $responseCode"
            } else {
                System.err.println("HTTP Response ${responseCode}: ${responseMessage}. Failed to send: ${notice}")
            }
        } catch (e) {
            System.err.println "Error sending Notice ${notice} to Airbrake. Exception: ${e}"
        } finally {
            conn?.disconnect()
        }
    }

    private HttpURLConnection buildConnection() {
        URL apiURL = new URL(configuration.scheme, configuration.host, configuration.port, configuration.path)

        HttpURLConnection conn = apiURL.openConnection()
        conn.setDoOutput(true)
        conn.setRequestProperty("Content-type", "text/xml")
        conn.setRequestProperty("Accept", "text/xml, application/xml")
        conn.setRequestMethod("POST")
        conn
    }
}
