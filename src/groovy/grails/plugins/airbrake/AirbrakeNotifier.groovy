package grails.plugins.airbrake

import groovy.transform.ToString
import groovy.util.logging.Log4j

@ToString(includeNames=true)
@Log4j
class AirbrakeNotifier {
    static final String AIRBRAKE_API_VERSION = '2.2'
	static final String AIRBRAKE_HOST = 'airbrakeapp.com'
	static final String AIRBRAKE_PATH = '/notifier_api/v2/notices'

	static final String NOTIFIER_NAME = 'grails-airbrake'
	static final String NOTIFIER_VERSION = '0.1'
	static final String NOTIFIER_URL = 'http://github.com/plecong/grails-airbrake'

    String host
    Integer port
    boolean secure
    boolean enabled

	private String path = AIRBRAKE_PATH
    private GroovyNoticeSerializer serializer = new GroovyNoticeSerializer()
    private GrailsNoticeBuilder grailsNoticeBuilder

    // mostly to make mocking easier in specs
    protected AirbrakeNotifier() {}

    AirbrakeNotifier(
            String apiKey,
            String env,
            List<String> filteredKeys,
            String host,
            Integer port,
            boolean secure,
            List<NoticeSupplementer> supplementers,
            boolean enabled
    ) {
        this.host = host ?: AIRBRAKE_HOST
        this.port = port ?: (secure ? 443 : 80)
        this.secure = secure
        this.enabled = enabled

        grailsNoticeBuilder = new GrailsNoticeBuilder(apiKey, env, filteredKeys, supplementers)
    }

    /**
     * A convenience for setting the userDataService after construction.
     * Because of the Spring lifecycle we don't have access to the service in doWithSpring when we construct the airbrakeNotifier bean.
     * Instead we have to wait until doWithApplicationContext to get the configured userDataService.
     * @param userDataService
     */
    void addUserDataService(UserDataService userDataService) {
        grailsNoticeBuilder.supplementers << new UserDataSupplementer(userDataService)
    }

    void notify(String errorMessage, Throwable throwable) {
        if (enabled) {
            doNotify(grailsNoticeBuilder.buildNotice(errorMessage, throwable))
        }
    }

	private doNotify(Notice notice) {
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
                serializer.toXml(notice, outputWriter)
            }

            responseCode = conn.responseCode
            responseMessage = conn.responseMessage
            if (responseCode < 200 || responseCode >= 300) {
                System.err.println("${responseCode}: ${responseMessage}, ${notice}")
            }


        } catch(e) {
            System.err.println "Error sending Notice ${notice} to Airbrake. Exception: ${e}"
        }
        finally {
            if (conn)
                conn.disconnect()
        }
	}

    private HttpURLConnection buildConnection() {
    	String protocol = secure ? 'https' : 'http'

        URL apiURL = new URL(
            protocol,
            host,
            port,
            path
        )

        HttpURLConnection conn = apiURL.openConnection()
        conn.setDoOutput(true)
        conn.setRequestProperty("Content-type", "text/xml")
        conn.setRequestProperty("Accept", "text/xml, application/xml")
        conn.setRequestMethod("POST")
        return conn
    }
}
