grails.project.work.dir = "target"

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
	checksums true // Whether to verify checksums on resolve
	legacyResolve false
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
    	inherits true // Whether to inherit repository definitions from plugins
    	grailsPlugins()
        grailsHome()
    	grailsCentral()
    	mavenCentral()
    }
//	 dependencies {
//		compile 'org.apache.commons:commons-lang3:3.4'
//	}
	 
    plugins {
        build(":release:3.1.2",	":rest-client-builder:2.1.1") {
		  export = false
		}
        test(':spock:0.7') {
            export = false
        }
    }
}
