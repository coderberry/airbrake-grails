package grails.plugins.airbrake

import spock.lang.*

class ConfigruationSpec extends Specification {

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
}
