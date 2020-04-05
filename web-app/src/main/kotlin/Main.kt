import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import io.github.kartoffelsup.nuntius.client.NuntiusHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.css.BorderStyle
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.GridTemplateAreas
import kotlinx.css.GridTemplateColumns
import kotlinx.css.GridTemplateRows
import kotlinx.css.LinearDimension
import kotlinx.css.display
import kotlinx.css.gridTemplateAreas
import kotlinx.css.gridTemplateColumns
import kotlinx.css.gridTemplateRows
import kotlinx.css.minHeight
import kotlinx.css.properties.border
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import login.loginFormStyles
import react.buildElement
import react.dom.render
import styled.StyledComponents
import styled.injectGlobal
import kotlin.browser.document
import kotlin.coroutines.CoroutineContext

val styles = CSSBuilder().apply {
    root {
        setCustomProperty("mdc-theme-primary", Color("#0087EE"))
        setCustomProperty("mdc-theme-text-icon-on-background", Color("#0087EE"))
        setCustomProperty("mdc-theme-secondary", Color("#43DA03"))
        setCustomProperty("mdc-theme-background", Color.white)
        setCustomProperty("mdc-theme-surface", Color.white)
        setCustomProperty("mdc-theme-on-primary", Color.white)
        setCustomProperty("mdc-theme-on-secondary", Color.black)
        setCustomProperty("mdc-theme-on-surface", Color.black)
        setCustomProperty("mdc-theme-error", Color("#B00020"))
    }
    media("only screen and (width > 900px)") {
        +"container" {
            display = Display.grid
            gridTemplateRows = GridTemplateRows("98vh")
            gridTemplateColumns = GridTemplateColumns("15vw 80vw")
            gridTemplateAreas = GridTemplateAreas(
                """
                "sidebar main"
                """
            )
        }
    }

    media("only screen and (width < 900px)") {
        +"container" {
            display = Display.grid
            gridTemplateRows = GridTemplateRows("20vh 80vh")
            gridTemplateColumns = GridTemplateColumns("85vw")
            gridTemplateAreas = GridTemplateAreas(
                """
                "sidebar"
                "main"
                """
            )
        }
    }

    +"main" {
        put("grid-area", "main")
    }

    +"sidebar" {
        put("grid-area", "sidebar")
        minHeight = LinearDimension("100%")
        border(
            width = LinearDimension("0.5px"),
            style = BorderStyle.solid,
            borderRadius = LinearDimension("20px"),
            color = Color("#000000")
        )
    }

    loginFormStyles()
}

val jsonx: Json by lazy { Json(JsonConfiguration.Stable) }

private class Application : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()
    private val nuntiusApi: NuntiusApiService = NuntiusApiService(NuntiusHttpClient(), jsonx)

    fun start() {
        document.getElementById("root")?.let {
            render(buildElement {
                child(App::class) {
                    attrs.coroutineScope = this@Application
                    attrs.nuntiusApi = nuntiusApi
                }
            }, it)
        }
    }
}

fun main() {
    js("require('@material/list/dist/mdc.list.min.css')")
    js("require('@material/textfield/dist/mdc.textfield.min.css')")
    js("require('@material/button/dist/mdc.button.min.css')")
    StyledComponents.injectGlobal(styles.toString())

    Application().start()
}
