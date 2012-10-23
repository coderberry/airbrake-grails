grails.project.work.dir = "target"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
    }

    plugins {
        build(":release:2.0.4", ":rest-client-builder:1.0.2") {
            export = false
        }
        compile( ":quartz:1.0-RC2") {
            export = false
        }
        test(':spock:0.6') {
            export = false
        }
    }
}
