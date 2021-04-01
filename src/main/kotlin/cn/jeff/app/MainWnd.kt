package cn.jeff.app

import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import tornadofx.*
import java.io.RandomAccessFile

class MainWnd : View("复仇魔神角色等级编辑器") {

	companion object {
		const val defaultFileName = "F:\\FavGames\\超时空英雄传说2复仇魔神完美典藏版\\" +
				"qskfcms\\games\\super2\\SAVE\\UJ01.SAV"
	}

	override val root: BorderPane
	private val j: MainWndJ

	private val roleProperties = MutableList(10) {
		RoleProperties("角色${it + 1}")
	}.observable()

	private lateinit var listView: ListView<RoleProperties>

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
						label("${it.name} 等級=${it.level}")
						button("設為1級")
						button("修改")
						alignment = Pos.CENTER_LEFT
						paddingHorizontal = 30
						spacing = 20.0
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
		listView.refresh()
	}

	fun refresh() {
//		information("刷新")
		RandomAccessFile(defaultFileName, "r").use { file ->
			roleProperties.forEachIndexed { index, roleProperties ->
				roleProperties.loadFromFile(file, index)
			}
		}
		listView.refresh()
	}

}
