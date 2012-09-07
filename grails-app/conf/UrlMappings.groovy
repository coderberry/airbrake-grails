class UrlMappings {

	static mappings = {
        "/airbrakeTest"(controller: 'airbrakeTest', action: 'throwException')

		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
