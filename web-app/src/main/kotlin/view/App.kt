package view

import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import kotlinx.coroutines.CoroutineScope
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.router.dom.browserRouter
import service.user.UserService
import view.nav.sidebar

interface ApplicationProps : RProps {
    var coroutineScope: CoroutineScope
    var nuntiusApi: NuntiusApiService
    var userService: UserService
}

class App : RComponent<ApplicationProps, RState>() {
    override fun RBuilder.render() {
        browserRouter {
            div(classes = "container") {
                sidebar(props.coroutineScope, props.nuntiusApi, props.userService) {
                }
            }
        }
    }
}
