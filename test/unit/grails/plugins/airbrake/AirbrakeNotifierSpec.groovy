package grails.plugins.airbrake

import spock.lang.*
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestMixin

@TestMixin(GrailsUnitTestMixin)
class AirbrakeNotifierSpec extends Specification {

    def 'sendNotice calls configuration.async closure if it is supplied'() {
        given:

        def notice = new Notice()
        def asyncNotices = []
        def asyncGrailsApplications = []
        def configuration = Mock(Configuration)
//        def asyncClosure = { notice, grailsApplication -> }
        configuration.getAsync() >>  {
            { n, a ->
                asyncNotices << n
                asyncGrailsApplications << a
            }
        }
        def airbrakeNotifier = Spy(AirbrakeNotifier, constructorArgs: [configuration])
        airbrakeNotifier.grailsApplication = grailsApplication


        when:
        airbrakeNotifier.sendNotice(notice)

        then:
        asyncNotices == [notice]
        asyncGrailsApplications == [grailsApplication]
    }

    def 'sendNotice calls sendToAirbrake configuration.asyncis false'() {
        given:
        def configuration = Mock(Configuration)
        configuration.getProperty('aync') >> false
        def airbrakeNotifier = Spy(AirbrakeNotifier, constructorArgs: [configuration])

        def notice = new Notice()

        when:
        airbrakeNotifier.sendNotice(notice)

        then:
        1 * airbrakeNotifier.sendToAirbrake(notice) >> true // return something to prevent underlying implementation from being called
    }
}
