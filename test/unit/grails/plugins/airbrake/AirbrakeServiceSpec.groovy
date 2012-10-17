package grails.plugins.airbrake

import spock.lang.Specification
import grails.test.mixin.TestFor

@TestFor(AirbrakeService)
class AirbrakeServiceSpec extends Specification {

    def setup() {
        service.airbrakeNotifier = Mock(AirbrakeNotifier)
    }

    def 'notify calls the notifier'() {
        given:
        def exception = new RuntimeException('Damn that Rabbit')

        when:
        service.notify('That rascally rabbit escaped', exception)

        then:
        1 * service.airbrakeNotifier.notify('That rascally rabbit escaped', exception)
    }

    def 'notify has optional Throwable parameter'() {
        when:
        service.notify('That rascally rabbit escaped')

        then:
        1 * service.airbrakeNotifier.notify('That rascally rabbit escaped', null)
    }
}
