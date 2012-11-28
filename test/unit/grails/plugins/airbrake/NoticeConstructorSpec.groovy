package grails.plugins.airbrake

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import spock.lang.Specification
import spock.lang.Unroll
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.codehaus.groovy.grails.exceptions.DefaultStackTraceFilterer

@TestMixin(GrailsUnitTestMixin)
class NoticeConstructorSpec extends Specification {

    def mockRequest = new GrailsMockHttpServletRequest()
    def mockResponse = new GrailsMockHttpServletResponse()

    def 'constructor supports simple properties'() {
        given:
        def exception = new RuntimeException()
        when:
        def notice = new Notice(
                apiKey: 'abcd',
                env: 'production',
                throwable: exception,
                projectRoot:  '/some/root',
                notifierName: 'notifier',
                notifierUrl: 'http://my.notifier.com/about',
                notifierVersion: '0.1.2',
                filteredKeys: ['password'],
                user: [id: '12345']
        )

        then:
        notice.apiKey == 'abcd'
        notice.env == 'production'
        notice.throwable == exception
        notice.projectRoot == '/some/root'
        notice.notifierName == 'notifier'
        notice.notifierUrl == 'http://my.notifier.com/about'
        notice.notifierVersion == '0.1.2'
        notice.filteredKeys == ['password']
        notice.user == [id: '12345']
    }

    def 'no url is no web request'() {
        when:
        def notice = new Notice()

        then:
        notice.url == null
    }

    def 'url should use supplied url over the url of the current webRequest'() {
        given:
        mockRequest.scheme = 'http'
        mockRequest.serverName = 'myhost.com'
        mockRequest.serverPort = 80
        mockRequest.forwardURI = '/controller/action'
        bindMockRequest()

        when:
        def notice = new Notice(url: 'http://myhost.com/some/custom/url')

        then:
        notice.url == 'http://myhost.com/some/custom/url'
    }

    @Unroll
    def 'url should handle protocol: #scheme and port: #port'() {
        given:
        mockRequest.scheme = scheme
        mockRequest.serverName = host
        mockRequest.serverPort = port
        mockRequest.forwardURI = '/'
        bindMockRequest()

        when:
        def notice = new Notice()

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
        def notice = new Notice()

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
        def notice = new Notice()

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
        def notice = new Notice()

        then:
        notice.url == expectedUrl

        where:
        queryString || expectedUrl
        null        || 'http://myhost.com/controller/action'
        ''          || 'http://myhost.com/controller/action'
        'id=1234'   || 'http://myhost.com/controller/action?id=1234'
    }

    @Unroll
    def 'component should be based on component, controller or controllerName'() {
        given:
        def webRequest = bindMockRequest()
        webRequest.controllerName = controllerName

        when:
        def notice = new Notice(component: component, controller: controller)

        then:
        notice.component == expectedComponent

        where:
        component   | controller   | controllerName   || expectedComponent
        'component' | 'controller' | 'controllerName' || 'component'
        null        | 'controller' | 'controllerName' || 'controller'
        null        | null         | 'controllerName' || 'controllerName'
        null        | null         | null             || null
    }

    @Unroll
    def 'action should be based on action or actionName'() {
        given:
        def webRequest = bindMockRequest()
        webRequest.actionName = actionName

        when:
        def notice = new Notice(action: action)

        then:
        notice.action == expectedAction

        where:
        action   | actionName   || expectedAction
        'action' | 'actionName' || 'action'
        null     | 'actionName' || 'actionName'
        null     | null         || null
    }

    @Unroll
    def 'params should be based on params or webRequest.paramsMap'() {
        given:
        if (paramsMap) {
            mockRequest.addParameters(paramsMap)
        }
        def webRequest = bindMockRequest()

        when:
        def notice = new Notice(params: params)

        then:
        notice.params == expectedParams

        where:
        params       | paramsMap       || expectedParams
        [one: 'two'] | [three: 'four'] || [one: 'two']
        null         | [three: 'four'] || [three: 'four']
        null         | null            || [:]
    }

    def 'cgiData should be based on headers'() {
        given:
        mockRequest.addHeader('User-Agent', 'Mozilla')
        mockRequest.addHeader('Referer', 'http://your.referrer.com')
        bindMockRequest()

        when:
        def notice = new Notice()

        then:
        notice.cgiData == ['User-Agent': 'Mozilla', Referer: 'http://your.referrer.com']
    }

    def 'cgiData should uses supplied cgiData over headers'() {
        given:
        mockRequest.addHeader('ignored', 'header')
        bindMockRequest()

        when:
        def notice = new Notice(cgiData: [custom: 'header'])

        then:
        notice.cgiData == [custom: 'header']
    }

    def 'session should be based on session'() {
        given:
        mockRequest.session.setAttribute('one', 'two')
        mockRequest.session.setAttribute('three','four')
        bindMockRequest()

        when:
        def notice = new Notice()

        then:
        notice.session == [one: 'two', three: 'four']
    }

    def 'session should uses supplied session over webRequest session'() {
        given:
        mockRequest.session.setAttribute('ignored', 'session')
        bindMockRequest()

        when:
        def notice = new Notice(session: [custom: 'session'])

        then:
        notice.session == [custom: 'session']
    }

    def 'hostname'() {
        when:
        def notice = new Notice()

        then:
        notice.hostname == InetAddress.localHost.hostName
    }

    @Unroll
    def 'constructor should filter #paramsMap'() {
        when: 'have some some paramsFilters'
        def notice = new Notice(filteredKeys: ['ask'], (paramsMap): [ask: 'me', something: 'interesting'] )

        then:
        notice."$paramsMap" == [ask: '[FILTERED]', something: 'interesting']

        where:
        paramsMap << ['params', 'session', 'cgiData']
    }

    @Unroll
    def 'constructor should filter #paramsMap using regular expression filters'() {
        when: 'we construct the notice'
        def notice = new Notice(filteredKeys: ['a.*'], (paramsMap): [ask: 'me', something: 'interesting'] )

        then:
        notice."$paramsMap" == [ask: '[FILTERED]', something: 'interesting']

        where:
        paramsMap << ['params', 'session', 'cgiData']
    }

    @Unroll
    def 'errorClass is based on throwable or the errroClass argument'() {
        when:
        def notice = new Notice(throwable: exception, errorClass: errorClass)

        then:
        notice.errorClass == expectedErrorClass

        where:
        exception              | errorClass         || expectedErrorClass
        new RuntimeException() | 'customErrorClass' || 'java.lang.RuntimeException'
        null                   | 'customErrorClass' || 'customErrorClass'
        null                   | null               || null
    }

    @Unroll
    def 'backtrace is based on throwable backtrace or the backtrace argument'() {
        when:
        def notice = new Notice(throwable: exception, backtrace: backtrace)

        then:
        notice.backtrace == expectedBacktrace

        where:
        exception                                             | backtrace      || expectedBacktrace
        new RuntimeException(stackTrace: exceptionStackTrace) | argsStackTrace || exceptionBacktrace
        null                                                  | argsStackTrace || argsBacktrace
        null                                                  | null           || []
    }

    def 'constructor filters the stackTrace'() {
        given:
        def exception = new RuntimeException('Damn that Rabbit')
        def anyStackTraceFilter = GroovySpy(DefaultStackTraceFilterer, global: true)

        when:
        new Notice(throwable: exception, stackTraceFilterer: new DefaultStackTraceFilterer())

        then:
        1 * anyStackTraceFilter.filter(exception)
    }

    def 'constructor should not try to filter the stackTrace of a null throwable'() {
        given:
        def anyStackTraceFilter = GroovySpy(DefaultStackTraceFilterer, global: true)

        when:
        new Notice(throwable: null)

        then:
        0 * anyStackTraceFilter.filter(_)
    }

    def 'constructor should not try to filter the stackTrace of a null stackTraceFilterer'() {
        given:
        def exception = new RuntimeException('Damn that Rabbit')
        def anyStackTraceFilter = GroovySpy(DefaultStackTraceFilterer, global: true)

        when:
        new Notice(throwable: exception)

        then:
        0 * anyStackTraceFilter.filter(_)
    }

    private getExceptionStackTrace() {
        [new StackTraceElement('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 )]
    }

    private getArgsStackTrace() {
        [new StackTraceElement('com.acme.RabbitTrapsController', 'net', 'RabbitTrapsController.groovy', 5 )]
    }

    private getExceptionBacktrace() {
        [new Backtrace('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 )]
    }

    private getArgsBacktrace() {
        [new Backtrace('com.acme.RabbitTrapsController', 'net', 'RabbitTrapsController.groovy', 5 )]
    }

    @Unroll
    def 'backtrace parsed from a list of maps as the  backtrace argument'() {
        when:
        def backtraceMaps = [
            [className: 'com.acme.RabbitTraps', methodName: 'catch', fileName: 'RabbitTraps.groovy', lineNumber: 10],
            [className: 'com.acme.RabbitTrapsController', methodName: 'net', fileName: 'RabbitTrapsController.groovy', lineNumber: 5]
        ]
        def notice = new Notice(backtrace: backtraceMaps)

        then:
        notice.backtrace == [
            new Backtrace('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 ),
            new Backtrace('com.acme.RabbitTrapsController', 'net', 'RabbitTrapsController.groovy', 5 )
        ]
    }

    def 'constructor creates a Notice with the right error message'() {
        when:
        def notice = new Notice(errorMessage: 'That rascally rabbit escaped')

        then:
        notice.errorMessage == 'That rascally rabbit escaped'
        notice.backtrace == []
        notice.errorClass == null
    }

    def 'constructor creates a Notice with the right error message, class and backtrace'() {
        given:
        def stackTrace = [new StackTraceElement('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 )]
        def exception = new RuntimeException('That rascally rabbit escaped')
        exception.stackTrace = stackTrace

        when:
        def notice = new Notice(throwable: exception)

        then:
        notice.errorMessage == 'That rascally rabbit escaped'
        notice.errorClass == 'java.lang.RuntimeException'
        notice.backtrace == [new Backtrace('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 )]
    }

    def 'constructor creates a Notice with the the exception message rather than supplied message'() {
        given:
        def exception = new RuntimeException('Damn that Rabbit')

        when:
        def notice = new Notice(throwable: exception, errorMessage: 'That rascally rabbit escaped')

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
        def notice = new Notice()

        then:
        notice.url == 'http://myhost.com/controller/action' // this is not changed by the NoticeContext
        notice.component == 'customComponent'
        notice.action == 'customAction'
        notice.params == [some: 'param']
    }

    private GrailsWebRequest bindMockRequest() {
        GrailsWebUtil.bindMockWebRequest(applicationContext, mockRequest, mockResponse)
    }

    def cleanup() {
        NoticeContextHolder.clearNoticeContext()
        RequestContextHolder.requestAttributes = null
    }
}
