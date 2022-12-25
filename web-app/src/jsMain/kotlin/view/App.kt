package view

import csstype.ClassName
import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import kotlinx.coroutines.CoroutineScope
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import service.user.UserService
import view.nav.Sidebar

external interface ApplicationProps : Props {
    var coroutineScope: CoroutineScope
    var nuntiusApi: NuntiusApiService
    var userService: UserService
}

val App = FC<ApplicationProps> { props ->
    div {
        className = ClassName("container")
        Sidebar {
            coroutineScope = props.coroutineScope
            nuntiusApi = props.nuntiusApi
            userService = props.userService
        }
    }
}

