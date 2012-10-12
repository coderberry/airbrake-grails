package grails.plugins.airbrake

import spock.lang.Specification

class UserDataSupplementerSpec extends Specification {

    def 'supplements the notice with userData from the userDataService'() {
        given:
        def userDataService = Mock(UserDataService)
        def UserDataSupplementer supplementer = new UserDataSupplementer(userDataService)
        def notice = new Notice()

        when:
        supplementer.supplement(notice)

        then:
        1 * userDataService.userData >> [id: 'Bugs Bunny', email: 'bugs@acme.com', username: 'bugs']

        and:
        notice.user == [id: 'Bugs Bunny', email: 'bugs@acme.com', username: 'bugs']
    }
}
