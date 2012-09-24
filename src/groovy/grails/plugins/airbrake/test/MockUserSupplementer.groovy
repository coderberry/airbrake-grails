package grails.plugins.airbrake.test

import grails.plugins.airbrake.NoticeSupplementer
import grails.plugins.airbrake.Notice
import org.apache.log4j.spi.LoggingEvent

/**
 * Simple mock Supplementer to use when testing the plugin.
 * In real life the user data will come from something like spring security or some other service in your app
 */
class MockUserSupplementer implements NoticeSupplementer {
    @Override
    Notice supplement(Notice notice, LoggingEvent event) {
        def milli = System.currentTimeMillis().toString()
        notice.user = [id: milli, name: 'Mock User', email: 'mockuser@domain.com', username: 'mockuser' ]
        notice
    }
}
