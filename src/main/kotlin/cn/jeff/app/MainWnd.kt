package cn.jeff.app

import javafx.fxml.FXMLLoader
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import tornadofx.*

class MainWnd : View("复仇魔神角色等级编辑器") {

	override val root: BorderPane
	private val j: MainWndJ

	private val roleProperties = MutableList(8) {
		RoleProperties("角色$it")
	}.observable()

	private var listView: ListView<RoleProperties>? = null

	init {
		primaryStage.isResizable = false

		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream(
				"/cn/jeff/app/MainWnd.fxml"
		))
		j = loader.getController()
		j.k = this

		j.mainPanel.center {
			listView = listview(roleProperties) {
				cellFormat {
					graphic = hbox {
						//						label("${it.name} - ${it.level} 級")
						button(it.name)
						label("等級=${it.level}")
						button("設為1級")
						button("修改")
					}
				}
			}
		}
	}

	fun chooseFile() {
//		information("标题", "选择文件吧。")
		roleProperties[0].level = 123
		roleProperties[1].level = 456
		roleProperties[2].level = 789
		listView?.refresh()
	}

	fun refresh() {
//		information("刷新")
		roleProperties[0].level = 44
		roleProperties[1].level = 66
		roleProperties[2].level = 33
		listView?.refresh()
	}

}
