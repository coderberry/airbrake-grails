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
        1 * service.airbrakeNotifier.notify(exception, [errorMessage: 'That rascally rabbit escaped'])
    }

    def 'notify has optional Throwable parameter'() {
        when:
        service.notify('That rascally rabbit escaped')

        then:
        1 * service.airbrakeNotifier.notify(null, [errorMessage: 'That rascally rabbit escaped'])
    }

    // prove that the helper methods on the service are just pass through to the NoticeRequestContextHolder
    def 'set component, action, params'() {
        when:
        service.addNoticeContext('component', 'action', [one: 'two'])

        then:
        Map context = NoticeContextHolder.noticeContext
        context.component == 'component'
        context.action == 'action'
        context.params == [one: 'two']
    }

    def 'set context'() {
        given:
        def context = [some: 'param']

        when:
        service.addNoticeContext(context)

        then:
        NoticeContextHolder.noticeContext == [some: 'param']
    }

    def cleanup() {
        NoticeContextHolder.clearNoticeContext()
    }
}
