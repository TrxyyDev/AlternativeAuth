![Logo](https://nsa40.casimages.com/img/2020/05/07/200507103021373167.png)

<h4 align="center">A javaFX library to Authenticate Minecraft users with Mojang/Microsoft account.</h4>

## Downloads

- Download latest version [HERE](https://github.com/TrxyyDev/AlternativeAuth/releases/latest)
- You can use my launcher sources [HERE](https://github.com/TrxyyDev/AlternativeAPI-launcher)

## How to use (Microsoft)

```
		Authentication gameAuth = new Authentication(AccountType.MICROSOFT);
		gameAuth.connectMicrosoft(contentPane); // contentPane == your Parent pane
		
		if (gameAuth.isLogged()) {
			// your action when the auth successful.
		}
		
```

## How to use (Mojang / Offline)

```
		Authentication gameAuth = new Authentication("username", "password", AccountType.MOJANG); // MOJANG / OFFLINE
		
		if (gameAuth.isLogged()) {
			// your action when the auth successful.
		}
		
```

## Example class

```
public class Example extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	private static Parent createMicrosoftPanel() {
		Pane contentPane = new Pane();
		Authentication gameAuth = new Authentication(AccountType.MICROSOFT);
		gameAuth.connectMicrosoft(contentPane);
		
		if (gameAuth.isLogged()) {
			System.out.println("Your username is " + gameAuth.getSession().getUsername());
			System.out.println("Your token is " + gameAuth.getSession().getToken());
			System.out.println("Your userID is " + gameAuth.getSession().getUuid());
		}
		
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
```