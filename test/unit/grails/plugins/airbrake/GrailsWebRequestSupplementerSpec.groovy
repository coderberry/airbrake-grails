package grails.plugins.airbrake

import spock.lang.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import grails.util.GrailsWebUtil
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse

@TestMixin(GrailsUnitTestMixin)
class GrailsWebRequestSupplementerSpec extends Specification {

    def notice = new Notice()
    def supplementer = new GrailsWebRequestSupplementer()
    def mockRequest = new GrailsMockHttpServletRequest()
    def mockResponse = new GrailsMockHttpServletResponse()

    def 'does nothing if there is no web request'() {
        when:
        def supplementedNotice = supplementer.supplement(notice, null)

        then:
        supplementedNotice.request.url == null
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
        def supplementedNotice = supplementer.supplement(notice, null)

        then:
        supplementedNotice.request.url == expectedUrl

        where:
        scheme   | host         | port || expectedUrl
        'http'   | 'myhost.com' | 80   || 'http://myhost.com/'
        'http'   | 'myhost.com' | 8080 || 'http://myhost.com:8080/'
        'https'  | 'myhost.com' | 443  || 'https://myhost.com/'
        'https'  | 'myhost.com' | 4434 || 'https://myhost.com:4434/'
    }

    @Unroll
    def 'url should include forwardUri'() {
        given:
        mockRequest.scheme = 'http'
        mockRequest.serverName = 'myhost.com'
        mockRequest.serverPort = 80
        mockRequest.forwardURI = '/controller/action'
        bindMockRequest()

        when:
        def supplementedNotice = supplementer.supplement(notice, null)

        then:
        supplementedNotice.request.url == 'http://myhost.com/controller/action'
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
        def supplementedNotice = supplementer.supplement(notice, null)

        then:
        supplementedNotice.request.url == expectedUrl

        where:
        queryString || expectedUrl
        null        || 'http://myhost.com/controller/action'
        ''          || 'http://myhost.com/controller/action'
        'id=1234'   || 'http://myhost.com/controller/action?id=1234'
    }

    def 'cgiData'() {
        given:
        mockRequest.addHeader('User-Agent', 'Mozilla')
        bindMockRequest()

        when:
        def supplementedNotice = supplementer.supplement(notice, null)

        then:
        supplementedNotice.request.cgiData == [HTTP_USER_AGENT: 'Mozilla']
    }

    def 'serverEnvironment should include the hostname'() {
        bindMockRequest()

        when:
        def supplementedNotice = supplementer.supplement(notice, null)

        then:
        supplementedNotice.serverEnvironment.hostname == InetAddress.localHost.hostName
    }

    private bindMockRequest() {
        GrailsWebUtil.bindMockWebRequest(applicationContext, mockRequest, mockResponse)
    }
}
