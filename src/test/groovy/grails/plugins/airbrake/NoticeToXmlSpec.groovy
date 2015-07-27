package grails.plugins.airbrake

import spock.lang.*

class NoticeToXmlSpec extends Specification {

    def 'serialize apiKey'() {
        given: 'a notice'
        def notice = new Notice(apiKey: 'abcd')

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        xml.'api-key'.text() == 'abcd'
    }

    def 'should serialize the notifier data'() {
        given: 'a notice'
        def notice = new Notice(notifierName: 'notifier', notifierUrl: 'http://my.notifier.com/about', notifierVersion: '0.1.2')

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        xml.notifier.name.text() == 'notifier'
        xml.notifier.version.text() == '0.1.2'
        xml.notifier.url.text() == 'http://my.notifier.com/about'
    }

    def 'serialize request params'() {
        given: 'a notice with a request'
        def args = [url: 'http://elmerfudd.com/traps/rabbits/net', component: 'RabbitTraps', action: 'net']
        def notice = new Notice(args)

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        xml.request.url.text() == 'http://elmerfudd.com/traps/rabbits/net'
        xml.request.component.text() == 'RabbitTraps'
        xml.request.action.text() == 'net'
    }

    def 'serialize error'() {
        given: 'a notice with an error'
        def args = [errorMessage: 'That rascally rabbit escaped', errorClass: 'EscapeException']
        args.backtrace = [
                new StackTraceElement('com.acme.RabbitTraps', 'catch', 'RabbitTraps.groovy', 10 ),
                new StackTraceElement('com.acme.RabbitTrapsController', 'net', 'RabbitTrapsController.groovy', 5 )
        ]
        def notice = new Notice(args)

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        xml.error.message.text() == 'That rascally rabbit escaped'
        xml.error.'class'.text() == 'EscapeException'
        def lines = xml.error.backtrace.line
        lines[0].'@file' == 'RabbitTraps.groovy'
        lines[0].'@number' == '10'
        lines[0].'@method' == 'com.acme.RabbitTraps.catch'

        lines[1].'@file' == 'RabbitTrapsController.groovy'
        lines[1].'@number' == '5'
        lines[1].'@method' == 'com.acme.RabbitTrapsController.net'
    }

    def 'notice XML should contain <backtrace><line/></backtrace> when no exception, see issue #33'() {

        given: 'a notice without an error'
        def args = [errorMessage: 'That rascally rabbit escaped']
        def notice = new Notice(args)

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        1 == xml.error.backtrace.line.size()
    }

    @Unroll
    def 'serialize #paramsMap'() {
        given: 'a notice'
        def notice = new Notice((paramsMap): [ask: 'me', something: 'interesting'] )

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        def vars = xml.request."$varsMap".var
        vars[0].'@key' == 'ask'
        vars[0].text() == 'me'
        vars[1].'@key' == 'something'
        vars[1].text() == 'interesting'

        where:
        paramsMap << ['params', 'session', 'cgiData']
        varsMap << ['params', 'session', 'cgi-data']
    }

    def 'serialize server-environment include env'() {
        given: 'a notice with environment'
        def notice = new Notice(env: 'development')

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        xml.'server-environment'.'environment-name'.text() == 'development'
    }

    def 'serialize server-environment include notice.serverEnvironment'() {
        given: 'a notice'
        def notice = new Notice(env: 'development')
        notice.hostname = 'myhost'
        notice.projectRoot = 'my/root'
        notice.appVersion = '1.2.3'

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        xml.'server-environment'.hostname.text() == 'myhost'
        xml.'server-environment'.'project-root'.text() == 'my/root'
        xml.'server-environment'.'app-version'.text() == '1.2.3'
    }

    def 'current-user'() {
        def notice = new Notice(user:  [id: '1234', name: 'Bugs Bunny', email: 'bugsbunny@acem.com', username: 'bugsbunny'])

        when: 'we serialize'
        def xml = getXmlFromSerializer(notice)

        then:
        xml.'current-user'.id.text() == '1234'
        xml.'current-user'.name.text() == 'Bugs Bunny'
        xml.'current-user'.email.text() == 'bugsbunny@acem.com'
        xml.'current-user'.username.text() == 'bugsbunny'
    }

    private getXmlFromSerializer(Notice notice) {
        StringWriter writer = new StringWriter()
        notice.toXml(writer)
        new XmlParser().parseText(writer.toString())
    }
}