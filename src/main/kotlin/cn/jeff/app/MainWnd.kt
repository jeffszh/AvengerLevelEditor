package cn.jeff.app

import tornadofx.*

class MainWnd : View("复仇魔神角色等级编辑器") {

	override val root = hbox {
		button("刷新") {
			action {
				println("很好！")
			}
		}
	}

}
