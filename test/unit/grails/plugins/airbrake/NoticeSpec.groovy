package grails.plugins.airbrake

import spock.lang.*

class NoticeSpec extends Specification {

    def 'toMap is inverse of constructor'() {
        // toMap is a useful tool for serialization.
        //
        given:
        def args = [
            apiKey: 'ABCD',
            projectRoot: '/my/project',
            notifierName: 'notifier',
            notifierVersion: '0.1.2',
            notifierUrl: 'http://my.notifier.com/about',
            filteredKeys: ['password'],
            url: 'http://myhost.com/controller/action',
            component:'customComponent',
            action: 'customAction',
            params: [one: 'two'],
            env: 'development',
            cgiData: ['User-Agent': 'Mozilla', Referer: 'http://your.referrer.com'],
            session: [three: 'four'],
            backtrace: [
                [className: 'com.acme.RabbitTraps', methodName: 'catch', fileName: 'RabbitTraps.groovy', lineNumber: 10],
                [className: 'com.acme.RabbitTrapsController', methodName: 'net', fileName: 'RabbitTrapsController.groovy', lineNumber: 5]
            ],
            errorClass: 'RuntimeException',
            errorMessage: 'That rascally rabbit escaped',
            hostname: 'myhost.com',
            user: [id: '12345']
        ]

        def notice = new Notice(args)

        expect:
        notice.toMap() == args
    }
}
