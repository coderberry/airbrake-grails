package grails.plugins.airbrake

import spock.lang.*

class NoticeContextHolderSpec extends Specification {

    def 'set component, action, params'() {
        when:
        NoticeContextHolder.addNoticeContext('component', 'action', [one: 'two'])

        then:
        Request request = NoticeContextHolder.noticeContext
        request.component == 'component'
        request.action == 'action'
        request.params == [one: 'two']
    }

    def 'set request'() {
        given:
        def context = [one: 'two']

        when:
        NoticeContextHolder.addNoticeContext(context)

        then:
        NoticeContextHolder.noticeContext == [one: 'two']
    }

    def 'clear'() {
        given:
        NoticeContextHolder.addNoticeContext([one: 'two'])

        when:
        NoticeContextHolder.clearNoticeContext()

        then:
        NoticeContextHolder.noticeContext == null
    }

    def cleanup() {
        NoticeContextHolder.clearNoticeContext()
    }
}
