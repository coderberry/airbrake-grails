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
        service.notify(exception, [errorMessage: 'That rascally rabbit escaped'])

        then:
        1 * service.airbrakeNotifier.notify(exception, [errorMessage: 'That rascally rabbit escaped'])
    }

    def 'notify without throwable parameter'() {
        when:
        service.notify(null, [errorMessage: 'That rascally rabbit escaped'])

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

    def 'clearNoticeContext'() {
        given:
        def context = [some: 'param']
        service.addNoticeContext(context)

        when:
        service.clearNoticeContext()

        then:
        NoticeContextHolder.noticeContext == null
    }

    def 'sendToAirbrake'() {
        given:
        def notice = new Notice()
        when:
        service.sendToAirbrake(notice)

        then:
        1 * service.airbrakeNotifier.sendToAirbrake(notice)
    }

    def cleanup() {
        NoticeContextHolder.clearNoticeContext()
    }
}
