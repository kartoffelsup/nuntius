import browser.document
import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import io.github.kartoffelsup.nuntius.client.NuntiusHttpClient
import kotlinx.coroutines.MainScope
import kotlinx.serialization.json.Json
import react.create
import react.dom.client.createRoot
import service.user.UserService
import view.App

val jsonx: Json by lazy { Json }

val mainScope = MainScope()

private class Application {
    private val nuntiusApi: NuntiusApiService = NuntiusApiService("http://localhost:8080", NuntiusHttpClient(), jsonx)
    private val userService: UserService = UserService(nuntiusApi)

    fun start() {
        document.getElementById("root")?.let {
            createRoot(it).render(App.create() {
                coroutineScope = mainScope
                nuntiusApi = this@Application.nuntiusApi
                userService = this@Application.userService
            })
        }
    }
}

fun main() {
    js("require('@material/list/dist/mdc.list.min.css')")
    js("require('@material/textfield/dist/mdc.textfield.min.css')")
    js("require('@material/button/dist/mdc.button.min.css')")

    Application().start()
}
