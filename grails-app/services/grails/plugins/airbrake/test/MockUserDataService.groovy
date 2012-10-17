package grails.plugins.airbrake.test

import grails.plugins.airbrake.UserDataService

/**
 * Simple mock UserDataService to use when testing the plugin.
 * In real life the user data will come from something like spring security or some other service in your app
 */
class MockUserDataService implements UserDataService {
    static transactional = false

    @Override
    Map getUserData() {
        def milli = System.currentTimeMillis().toString()
        [id: milli, name: 'Mock User', email: 'mockuser@domain.com', username: 'mockuser' ]
    }
}
