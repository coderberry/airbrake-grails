package grails.plugins.airbrake

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

class AirbrakeNotifier {

	static final String AIRBRAKE_HOST = 'airbrakeapp.com'
	static final String AIRBRAKE_PORT = '80'
	static final String AIRBRAKE_PATH = '/notifier_api/v2/notices'

	static final String NOTIFIER_NAME = 'grails-airbrake'
	static final String NOTIFIER_VERSION = '0.1'
	static final String NOTIFIER_URL = 'http://github.com/plecong/grails-airbrake'

	String apiKey
	String env

	String host = AIRBRAKE_HOST
	String port = null
	String path = AIRBRAKE_PATH
	boolean secure = false

	NoticeSerializer serializer = new GroovyNoticeSerializer();

	void notify(Notice notice) {
        HttpURLConnection conn = null;
        int responseCode;
        String responseMessage;

        try {
            conn = buildConnection();

            serializer.serialize(this, notice, new OutputStreamWriter(conn.outputStream))

            responseCode = conn.responseCode
            responseMessage = conn.responseMessage
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        if (responseCode < 200 || responseCode >= 300)
            System.err.println("${responseCode}: ${responseMessage}, ${notice}");

        return;
	}

    private HttpURLConnection buildConnection() {
    	String protocol = secure ? 'https' : 'http'
    	int apiPort = port ? Integer.parseInt(port) : (secure ? 443 : 80)

        URL apiURL = new URL(
            protocol,
            host,
            apiPort,
            path
        );

        HttpURLConnection conn = (HttpURLConnection) apiURL.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-type", "text/xml");
        conn.setRequestProperty("Accept", "text/xml, application/xml");
        conn.setRequestMethod("POST");
        return conn;
    }

}