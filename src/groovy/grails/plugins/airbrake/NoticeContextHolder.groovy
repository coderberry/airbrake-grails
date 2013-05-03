package grails.plugins.airbrake

import org.springframework.core.NamedThreadLocal


/**
 * Simple holder class that associates a Notice Request instance
 * with the current thread.
 */
abstract class NoticeContextHolder {

    private static final ThreadLocal<Map> requestContextHolder = new NamedThreadLocal<Map>("Notice context")

    static void addNoticeContext(String component, String action, Map params = [:]) {
        initializedContext << [component: component, action: action, params: params]
    }

    static void addNoticeContext(Map context) {
        initializedContext << context
    }

    static Map getNoticeContext() {
        requestContextHolder.get()
    }

    static void clearNoticeContext() {
        requestContextHolder.remove()
    }

    private static Map getInitializedContext() {
        def context = noticeContext
        if (!context) {
            context = [:]
            requestContextHolder.set(context)
        }
        context
    }
}
