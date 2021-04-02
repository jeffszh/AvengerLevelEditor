package cn.jeff.app;

import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class MainWndJ {

	public MainWnd k;
	public BorderPane mainPanel;
	public TextField filenameText;

	public void chooseFile() {
		k.chooseFile();
	}

	public void refresh() {
		k.refresh();
	}

}
