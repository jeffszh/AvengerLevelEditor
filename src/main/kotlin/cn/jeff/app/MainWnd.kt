package cn.jeff.app

import com.google.gson.GsonBuilder
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import tornadofx.*
import java.io.*

class MainWnd : View("复仇魔神角色等级编辑器") {

	companion object {
		const val defaultFileName = "F:\\FavGames\\超时空英雄传说2复仇魔神完美典藏版\\" +
				"qskfcms\\games\\super2\\SAVE\\UJ01.SAV"
		const val configFileName = "AppConfig.json"
		val gson = GsonBuilder().setPrettyPrinting().create()!!
	}

	override val root: BorderPane
	private val j: MainWndJ

	private val workFilename = SimpleObjectProperty(defaultFileName)

	private val roleProperties = MutableList(10) {
		RoleProperties(it, "角色${it + 1}")
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
				cellFormat { role ->
					graphic = hbox {
						label(role.name)
						val input = textfield(role.level.toString()) {
							maxWidth = 80.0
						}
						button("修改") {
							action {
								val s = input.text
								if (s.isInt()) {
									role.level = s.toInt()
									RandomAccessFile(workFilename.value, "rw").use { file ->
										role.saveLevelToFile(file)
									}
								} else {
									error("必須輸入數字！")
								}
							}
						}
						button("設為1級") {
							action {
								role.level = 1
								RandomAccessFile(workFilename.value, "rw").use { file ->
									role.saveLevelToFile(file)
								}
							}
						}
						alignment = Pos.CENTER_LEFT
						paddingHorizontal = 30
						spacing = 20.0
					}
				}
			}
		}

		j.filenameText.bind(workFilename)

		try {
			loadAppConfig()
		} catch (e: IOException) {
			e.printStackTrace()
			// 若读取不到配置，将默认配置写到文件，这样下次就能读到了。
			workFilename.value = defaultFileName
			saveAppConfig()
		}
	}

	fun chooseFile() {
		val filter = FileChooser.ExtensionFilter("复仇魔神存档文件", "*.SAV")
		val file = chooseFile("选择文件复仇魔神的存档文件", arrayOf(filter)) {
			initialDirectory = File(".")
		}
		if (file.isNotEmpty()) {
			workFilename.value = file[0].absolutePath
			listView.refresh()
			saveAppConfig()
		}
	}

	fun refresh() {
//		information("刷新")
		RandomAccessFile(workFilename.value, "r").use { file ->
			roleProperties.forEach { roleProperties ->
				roleProperties.loadFromFile(file)
			}
		}
		listView.refresh()
	}

	private class AppConfig {
		var workFilename = defaultFileName
	}

	private fun loadAppConfig() {
		FileReader(configFileName).use { reader ->
			val appConfig = gson.fromJson(reader, AppConfig::class.java)
			workFilename.value = appConfig.workFilename
		}
	}

	private fun saveAppConfig() {
		FileWriter(configFileName).use { writer ->
			val appConfig = AppConfig().apply {
				workFilename = this@MainWnd.workFilename.value
			}
			gson.toJson(appConfig, writer)
		}
	}

}
