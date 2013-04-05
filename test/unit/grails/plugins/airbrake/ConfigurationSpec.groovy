package grails.plugins.airbrake

import spock.lang.*
import org.codehaus.groovy.grails.exceptions.DefaultStackTraceFilterer
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin

@TestMixin(GrailsUnitTestMixin)
class ConfigurationSpec extends Specification {

    @Unroll
    def 'constructor assigns a port if non is supplied'() {
        when:
        def config = new Configuration(secure: secure)

        then:
        config.port == port

        where:
        secure || port
        false  || 80
        true   || 443
    }

    def 'constructor uses supplied port'() {
        when:
        def config = new Configuration(port: 1234)

        then:
        config.port == 1234
    }

    def 'constructor assigns a path if none is specified'() {
        when:
        def config = new Configuration()

        then:
        config.path == AirbrakeNotifier.AIRBRAKE_PATH
    }

    def 'constructor uses supplied path'() {
        given:
        def suppliedPath = '/errbit/notifier_api/v2/notices'

        when:
        def config = new Configuration(path: suppliedPath)

        then:
        config.path == suppliedPath
    }

    @Unroll
    def 'scheme'() {
        when:
        def config = new Configuration(secure: secure)

        then:
        config.scheme == scheme

        where:
        secure || scheme
        false  || 'http'
        true   || 'https'
    }

    def 'stackTraceFilterer defaults to DefaultStackTraceFilterer'() {
        expect:
        new Configuration().stackTraceFilterer instanceof DefaultStackTraceFilterer
    }

    def 'merge does not include async, class or metaClass'() {
        given:
        def configuration = new Configuration(env: 'production', async: { 'do something'} )

        when:
        def merge = configuration.merge(component: 'customComponent')

        then:
        merge.env == 'production'
        merge.component == 'customComponent'
        merge.metaClass == null
        merge.async == null
        merge.class == null
    }

    def 'async that is not a Closure or true should default to thread pool Closure'() {
        given: ''
        def configuration = new Configuration([ async: 'do something' ])

        expect:
        configuration.async instanceof Closure
    }

    def 'async can be a Closure'() {
        given: 'configuration options with an async closure option'
        GroovySpy(Executors, global: true)
        def options = [async: { 'do something' } ]


        when: 'we construct a Configuration'
        def configuration = new Configuration(options)

        then: 'the contruscted configuration to have an async closure'
        configuration.async != null
        configuration.async() == 'do something'

        and: 'no executors methods were called'
        0 * Executors._
    }

    def 'we construct a threadPool based async handle for async=true'() {
        given: 'a mocked threadPool'
        GroovySpy(Executors, global: true)
        def mockExecutorService = Mock(ExecutorService)

        and: 'a mocked airbrakeNotifier bean'
        def mockAirbrakeNotifier = Mock(AirbrakeNotifier)
        grailsApplication.mainContext.registerMockBean('airbrakeNotifier', mockAirbrakeNotifier)

        and: 'a notice'
        def notice = new Notice()

        when: 'we create a new configuration'
        def configuration = new Configuration( [async: true ])

        then: 'we create a new thread pool and construct a async closure'
        1 * Executors.newFixedThreadPool(_ as Integer) >> mockExecutorService
        configuration.async instanceof Closure

        when: 'we call that async closure'
        configuration.async(notice, grailsApplication)

        then: 'we submit a runnable to the threadPool'
        1 * mockExecutorService.submit(_ as Runnable) >> { Runnable runnable ->
            runnable.run() // run everything synchronously
        }

        and: 'running the runnable calls the sends the notice to airbrake'
        1 * mockAirbrakeNotifier.sendToAirbrake(notice)
    }

    def 'config option of asyncThreadPoolSize with default of 5'() {
        given: 'a mocked threadPool'
        GroovySpy(Executors, global: true)

        when: 'we create a new configuration with a custom asyncThreadPoolSize'
        new Configuration( [async: true, asyncThreadPoolSize: asyncThreadPoolSize ])

        then: 'we create a new thread pool with the right size'
        1 * Executors.newFixedThreadPool(exepctedThreadPoolSize)

        where:
        asyncThreadPoolSize || exepctedThreadPoolSize
        null                || 5
        42                  || 42
    }

    def 'backwards compatibility for old filteredKeys configuration option'() {
        given:
        def configuration = new Configuration([filteredKeys: ['password']])

        expect:
        configuration.paramsFilteredKeys == ['password']
        configuration.sessionFilteredKeys == ['password']
        configuration.cgiDataFilteredKeys == ['password']
    }
}
