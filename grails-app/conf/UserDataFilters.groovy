class UserDataFilters {
    def airbrakeService

    def filters = {
        all(uri: '/**') {
            before = {
                def milli = System.currentTimeMillis().toString()
                airbrakeService.addNoticeContext(user: [id: milli, name: 'Mock User', email: 'mockuser@domain.com', username: 'mockuser' ])
            }
        }
    }
}