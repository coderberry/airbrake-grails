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
	static final String NOTIFIER_VERSION = '0.8.1'
	static final String NOTIFIER_URL = 'https://github.com/cavneb/airbrake-grails'

    final Configuration configuration

	private String path = AIRBRAKE_PATH

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
        int responseCode
        String responseMessage

        try {
            conn = buildConnection()
            conn.outputStream.withWriter { outputWriter ->
                notice.toXml(outputWriter)
            }

            responseCode = conn.responseCode
            responseMessage = conn.responseMessage
            if (responseCode < 200 || responseCode >= 300) {
                System.err.println("${responseCode}: ${responseMessage}, ${notice}")
            }
        } catch (e) {
            System.err.println "Error sending Notice ${notice} to Airbrake. Exception: ${e}"
        }
        finally {
            if (conn)
                conn.disconnect()
        }
    }

    private HttpURLConnection buildConnection() {
        URL apiURL = new URL(configuration.scheme, configuration.host, configuration.port, path)

        HttpURLConnection conn = apiURL.openConnection()
        conn.setDoOutput(true)
        conn.setRequestProperty("Content-type", "text/xml")
        conn.setRequestProperty("Accept", "text/xml, application/xml")
        conn.setRequestMethod("POST")
        conn
    }
}
