package grails.plugins.airbrake

class UserDataSupplementer implements NoticeSupplementer {
    UserDataService userDataService

    UserDataSupplementer(UserDataService userDataService) {
        this.userDataService = userDataService
    }

    @Override
    void supplement(Notice notice) {
        notice.user = userDataService.userData
    }
}
