# Airbrake Plugin for Grails

This is the notifier plugin for integrating apps with [Airbrake](http://airbrake.io).

When an uncaught exception occurs, Airbrake will POST the relevant data to the Airbrake server specified in your environment.

# Installation

```compile ":airbrake:0.3"```

# Compatability

This plugin is compatible with Grails version 2.1 or greater.

# Configuration

Once the plugin is installed, you only need to add a few lines of code into the `Config.groovy` file, all within the log4j map:

```groovy
log4j = {
    appenders {
      console name:'stdout', layout:pattern(conversionPattern: '%c %m%n')

// -------> Add the Airbrake appender
      appender name: 'airbrake', new grails.plugins.airbrake.AirbrakeAppender(
          api_key: 'API_KEY', // Place your API key here
          env: ((Environment.current == Environment.PRODUCTION) ? 'prod' : 'dev')
      )
// <-------
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

    warn   'org.mortbay.log', 'org.apache'

    root {
// -------> Add the 'airbrake' appender to the root debug level logging
      debug 'stdout', 'airbrake' // Add airbrake to debug
// <-------
    }
}
```

# Kudos

Most of the source code has been written by Phuong LeCong ([https://github.com/plecong/grails-airbrake](https://github.com/plecong/grails-airbrake)).