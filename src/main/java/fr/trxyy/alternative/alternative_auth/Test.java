package fr.trxyy.alternative.alternative_auth;

import fr.trxyy.alternative.alternative_auth.account.AccountType;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Test extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	private static Parent createMicrosoftPanel() {
		Pane contentPane = new Pane();
		Authentication gameAuth = new Authentication(null, null, AccountType.MICROSOFT);
		gameAuth.connectMicrosoft(contentPane);
		return contentPane;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Scene scene = new Scene(createMicrosoftPanel());
		Stage stage = new Stage();
		stage.setResizable(false);
		stage.setTitle("Microsoft Authentication");
		stage.setWidth(500);
		stage.setHeight(600);
		stage.setScene(scene);
		stage.show();
	}
}
