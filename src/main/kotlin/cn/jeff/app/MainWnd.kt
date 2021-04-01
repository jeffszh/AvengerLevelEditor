package cn.jeff.app

import javafx.fxml.FXMLLoader
import javafx.scene.layout.BorderPane
import tornadofx.*

class MainWnd : View("复仇魔神角色等级编辑器") {

	override val root: BorderPane
	private val j: MainWndJ

	private val roleProperties = MutableList(8) {
		RoleProperties()
	}.observable()

	init {
		primaryStage.isResizable = false

		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream(
				"/cn/jeff/app/MainWnd.fxml"
		))
		j = loader.getController()
		j.k = this

		j.mainPanel.center {
			listview(roleProperties) {
				cellCache {
					hbox {
						label("${it.name} - ${it.level} 級")
						button("設為1級")
						button("修改")
					}
				}
			}
		}
	}

	fun chooseFile() {
		information("标题", "选择文件吧。")
	}

	fun refresh() {
		information("刷新")
	}

}
