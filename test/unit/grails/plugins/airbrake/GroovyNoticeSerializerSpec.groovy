package grails.plugins.airbrake

import spock.lang.*

class GroovyNoticeSerializerSpec extends Specification {
    @Unroll
    def 'serialize filters #paramsMap'() {
        given: 'a notifier and a notice'
        def notifier = new AirbrakeNotifier()
        notifier.filteredKeys = ['ask' ]

        def notice = new Notice()
        notice.request."$paramsMap" = [ask: 'me', something: 'interesting']

        when: 'we serizlie'
        def serializer = new GroovyNoticeSerializer()
        def serialized = serializer.serialize(notifier, notice)

        then:
        def xml = new XmlParser().parseText(serialized)

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
        def serializer = new GroovyNoticeSerializer()
        def serialized = serializer.serialize(notifier, notice)

        then:
        def xml = new XmlParser().parseText(serialized)

        def vars = xml.request.params.var
        vars[0].'@key' == 'ask'
        vars[0].text() == 'FILTERED'
        vars[1].'@key' == 'something'
        vars[1].text() == 'interesting'
    }
}