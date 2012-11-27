import grails.plugins.airbrake.test.AirbrakeNotifyJob

// configuration for plugin testing - will not be included in the plugin zip
grails.plugins.airbrake.apiKey = 'YOUR_API_KEY'
grails.plugins.airbrake.filteredKeys = ['password']
grails.plugins.airbrake.includeEventsWithoutExceptions = true
grails.plugins.airbrake.async = { notice, grailsApplication ->
    AirbrakeNotifyJob.triggerNow(notice: notice)
}

log4j = {

    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%c %m%n')
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
    debug  'grails.plugins.airbrake'

    root {
        warn 'stdout'
    }
}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
