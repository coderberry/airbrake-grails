package grails.plugins.airbrake

import spock.lang.*

class GroovyNoticeSerializerSpec extends Specification {

    def serializer = new GroovyNoticeSerializer()

    @Unroll
    def 'serialize filters #paramsMap'() {
        given: 'a notifier and a notice'
        def notifier = new AirbrakeNotifier()
        notifier.filteredKeys = ['ask' ]

        def notice = new Notice()
        notice.request."$paramsMap" = [ask: 'me', something: 'interesting']

        when: 'we serizlie'
        def xml = getXmlFromSerializer(notifier, notice)

        then:

        def vars = xml.request."$varsMap".var
        vars[0].'@key' == 'ask'
        vars[0].text() == 'FILTERED'
        vars[1].'@key' == 'something'
        vars[1].text() == 'interesting'

        where:
        paramsMap << ['params', 'session', 'cgiData']
        varsMap << ['params', 'session', 'cgi-data']
    }

    def 'serialize supports regular expression filters '() {
        given: 'a notifier and a notice'
        def notifier = new AirbrakeNotifier()
        notifier.filteredKeys = ['a.*' ]

        def notice = new Notice()
        notice.request.params = [ask: 'me', something: 'interesting']

        when: 'we serizlie'
        def xml = getXmlFromSerializer(notifier, notice)

        then:
        def vars = xml.request.params.var
        vars[0].'@key' == 'ask'
        vars[0].text() == 'FILTERED'
        vars[1].'@key' == 'something'
        vars[1].text() == 'interesting'
    }

    def 'serialize server-environment include env'() {
        given: 'a notifier and a notice'
        def notifier = new AirbrakeNotifier()
        notifier.env = 'development'

        def notice = new Notice()

        when: 'we serizlie'
        def xml = getXmlFromSerializer(notifier, notice)

        then:
        xml.'server-environment'.'environment-name'.text() == 'development'
    }

    def 'serialize server-environment include notice.serverEnvironment'() {
        given: 'a notifier and a notice'
        def notifier = new AirbrakeNotifier()
        notifier.env = 'development'

        def notice = new Notice()
        notice.serverEnvironment.hostname = 'myhost'
        notice.serverEnvironment.projectRoot = 'my/root'
        notice.serverEnvironment.appVersion = '1.2.3'

        when: 'we serizlie'
        def xml = getXmlFromSerializer(notifier, notice)

        then:
        xml.'server-environment'.hostname.text() == 'myhost'
        xml.'server-environment'.'project-root'.text() == 'my/root'
        xml.'server-environment'.'app-version'.text() == '1.2.3'
    }

    private getXmlFromSerializer(notifier, notice) {
        def serialized = serializer.serialize(notifier, notice)
        new XmlParser().parseText(serialized)
    }
}