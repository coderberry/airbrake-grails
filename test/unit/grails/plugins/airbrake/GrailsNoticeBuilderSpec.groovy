package grails.plugins.airbrake

import spock.lang.*

import grails.util.GrailsWebUtil
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse

@TestMixin(GrailsUnitTestMixin)
class GrailsNoticeBuilderSpec extends Specification {

    def grailsNoticeBuilder = new GrailsNoticeBuilder()
    def mockRequest = new GrailsMockHttpServletRequest()
    def mockResponse = new GrailsMockHttpServletResponse()

    def 'add apiKey and env to the Notice'() {
        given:
        grailsNoticeBuilder.apiKey = 'abcd'
        grailsNoticeBuilder.env = 'production'

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.apiKey == 'abcd'
        notice.env == 'production'
    }

    def 'does nothing if there is no web request'() {
        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.url == null
    }

    @Unroll
    def 'url should handle protocol: #protocol and port: #port'() {
        given:
        mockRequest.scheme = scheme
        mockRequest.serverName = host
        mockRequest.serverPort = port
        mockRequest.forwardURI = '/'
        bindMockRequest()

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.url == expectedUrl

        where:
        scheme   | host         | port || expectedUrl
        'http'   | 'myhost.com' | 80   || 'http://myhost.com/'
        'http'   | 'myhost.com' | 8080 || 'http://myhost.com:8080/'
        'https'  | 'myhost.com' | 443  || 'https://myhost.com/'
        'https'  | 'myhost.com' | 4434 || 'https://myhost.com:4434/'
    }

    def 'url should include forwardUri'() {
        given:
        mockRequest.scheme = 'http'
        mockRequest.serverName = 'myhost.com'
        mockRequest.serverPort = 80
        mockRequest.forwardURI = '/controller/action'
        bindMockRequest()

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.url == 'http://myhost.com/controller/action'
    }

    def 'url should handle no forwardUri '() {
        given:
        mockRequest.scheme = 'http'
        mockRequest.serverName = 'myhost.com'
        mockRequest.serverPort = 80
        mockRequest.forwardURI = null
        bindMockRequest()

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.url == 'http://myhost.com'
    }

    @Unroll
    def 'url should handle queryString: #queryString'() {
        given:
        mockRequest.scheme = 'http'
        mockRequest.serverName = 'myhost.com'
        mockRequest.serverPort = 80
        mockRequest.forwardURI = '/controller/action'
        mockRequest.queryString = queryString
        bindMockRequest()

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.url == expectedUrl

        where:
        queryString || expectedUrl
        null        || 'http://myhost.com/controller/action'
        ''          || 'http://myhost.com/controller/action'
        'id=1234'   || 'http://myhost.com/controller/action?id=1234'
    }

    def 'cgiData'() {
        given:
        mockRequest.addHeader('User-Agent', 'Mozilla')
        mockRequest.addHeader('Referer', 'http://your.referrer.com')
        bindMockRequest()

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.cgiData == [HTTP_USER_AGENT: 'Mozilla', HTTP_REFERER: 'http://your.referrer.com']
    }

    def 'serverEnvironment should include the hostname'() {
        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.hostname == InetAddress.localHost.hostName
    }

    @Unroll
    def 'buildNotice should filter #paramsMap'() {
        given: 'some filteredKeys'

        grailsNoticeBuilder.filteredKeys = ['ask' ]
        // We can use supplementers to make testing easy. Nice.
        grailsNoticeBuilder.supplementers = [ new RequestOverwritingSupplementer(ask: 'me', something: 'interesting')]

        when: 'we build the notice'
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice."$paramsMap" == [ask: '[FILTERED]', something: 'interesting']

        where:
        paramsMap << ['params', 'session', 'cgiData']
    }

    @Unroll
    def 'buildNotice should filter #paramsMap using regular expression filters'() {
        given: 'a notifier and a notice'
        grailsNoticeBuilder.filteredKeys = ['a.*' ]
        grailsNoticeBuilder.supplementers = [ new RequestOverwritingSupplementer(ask: 'me', something: 'interesting')]

        when: 'we build the notice'
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice."$paramsMap" == [ask: '[FILTERED]', something: 'interesting']

        where:
        paramsMap << ['params', 'session', 'cgiData']
    }

    def 'buildNotice creates a Notice with the right error message'() {
        given:
        def GrailsNoticeBuilder grailsNoticeBuilder = new GrailsNoticeBuilder()

        when:
        def notice = grailsNoticeBuilder.buildNotice('That rascally rabbit escaped', null)

        then:
        notice.errorMessage == 'That rascally rabbit escaped'
        notice.backtrace == null
        notice.errorClass == null
    }

    def 'buildNotice creates a Notice with the right error message, class and backtrace'() {
        given:
        def GrailsNoticeBuilder grailsNoticeBuilder = new GrailsNoticeBuilder()

        def exception = new RuntimeException('That rascally rabbit escaped')
        exception.stackTrace = [new StackTraceElement('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 )]

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, exception)

        then:
        notice.errorMessage == 'That rascally rabbit escaped'
        notice.errorClass == 'java.lang.RuntimeException'
        notice.backtrace == [new StackTraceElement('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 )]
    }

    def 'buildNotice creates a Notice with the the exception message rather than supplied message'() {
        given:
        def GrailsNoticeBuilder grailsNoticeBuilder = new GrailsNoticeBuilder()

        def exception = new RuntimeException('Damn that Rabbit')

        when:
        def notice = grailsNoticeBuilder.buildNotice('That rascally rabbit escaped', exception)

        then:
        notice.errorMessage == 'Damn that Rabbit'
    }

    def 'RequestContext should override webRequest data'() {
        given:
        mockRequest.scheme = 'http'
        mockRequest.serverName = 'myhost.com'
        mockRequest.serverPort = 80
        mockRequest.forwardURI = '/controller/action'
        bindMockRequest()

        and:
        NoticeContextHolder.addNoticeContext('customComponent', 'customAction', [some: 'param'])

        when:
        def notice = grailsNoticeBuilder.buildNotice(null, null)

        then:
        notice.url == 'http://myhost.com/controller/action' // this is not changed by the NoticeContext
        notice.component == 'customComponent'
        notice.action == 'customAction'
        notice.params == [some: 'param']
    }

    private bindMockRequest() {
        GrailsWebUtil.bindMockWebRequest(applicationContext, mockRequest, mockResponse)
    }

    private class RequestOverwritingSupplementer implements NoticeSupplementer {

        Map params

        RequestOverwritingSupplementer(Map params) {
            this.params = params
        }

        @Override
        void supplement(Notice notice) {
            notice.session = params
            notice.cgiData = params
            notice.params = params
        }
    }

    def cleanup() {
        NoticeContextHolder.clearNoticeContext()
    }
}
