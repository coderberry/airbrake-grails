package grails.plugins.airbrake

import spock.lang.*

class NoticeContextHolderSpec extends Specification {

    def 'set component, action, params'() {
        when:
        NoticeContextHolder.addNoticeContext('component', 'action', [one: 'two'])

        then:
        def context = NoticeContextHolder.noticeContext
        context.component == 'component'
        context.action == 'action'
        context.params == [one: 'two']
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
