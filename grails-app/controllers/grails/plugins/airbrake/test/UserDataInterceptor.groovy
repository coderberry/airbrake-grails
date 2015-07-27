package grails.plugins.airbrake.test


class UserDataInterceptor {
    def airbrakeService

    UserDataInterceptor() {
        matchAll()
    }

    boolean before() {
        def milli = System.currentTimeMillis().toString()
        airbrakeService.addNoticeContext(user: [id: milli, name: 'Mock User', email: 'mockuser@domain.com', username: 'mockuser' ])
        true
    }
}
