import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import kotlinx.coroutines.CoroutineScope
import nav.sidebar
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.router.dom.browserRouter

interface ApplicationProps : RProps {
    var coroutineScope: CoroutineScope
    var nuntiusApi: NuntiusApiService
}

class App : RComponent<ApplicationProps, RState>() {
    override fun RBuilder.render() {
        browserRouter {
            div(classes = "container") {
                sidebar(props.coroutineScope, props.nuntiusApi) {
                }
            }
        }
    }
}
