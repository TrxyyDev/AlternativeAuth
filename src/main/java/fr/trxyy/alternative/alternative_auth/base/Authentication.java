package fr.trxyy.alternative.alternative_auth.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import fr.trxyy.alternative.alternative_auth.account.AccountType;
import fr.trxyy.alternative.alternative_auth.account.Session;
import fr.trxyy.alternative.alternative_auth.microsoft.MicrosoftAuth;
import fr.trxyy.alternative.alternative_auth.mojang.model.MojangAuthResult;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * @author Trxyy
 */
public class Authentication {
	/**
	 * Is player authed ?
	 */
	public boolean isAuthed = false;
	/**
	 * The session
	 */
	private Session session = new Session();

	/**
	 * The Constructor
	 * 
	 * @param user The username/mail
	 * @param pwd  The password
	 * @param type The account Type (Mojang/Offline)
	 */
	public Authentication(String user, String pwd, AccountType type) {
		if (type.equals(AccountType.MOJANG)) {
			this.session.setUsername(user);
			this.connectMinecraft(user, pwd);
		} else if (type.equals(AccountType.OFFLINE)) {
			this.isAuthed = true;
			this.session.setUsername(user);
			this.session.setToken(TokenGenerator.generateToken(user));
			this.session.setUuid(UUID.randomUUID().toString().replace("-", ""));
		}
	}

	/**
	 * Connect to minecraft with a Microsoft account
	 * 
	 * @param root The parent to show
	 * @return The result in a WebView
	 */
	public WebView connectMicrosoft(Pane root) {
		final WebView webView = new WebView();
		final WebEngine webEngine = webView.getEngine();
		webEngine.load(AuthConstants.MICROSOFT_BASE_URL);
		webEngine.setJavaScriptEnabled(true);
		webView.setPrefWidth(500);
		webView.setPrefHeight(600);
		root.getChildren().add(webView);
		webEngine.getHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) c -> {
			if (c.next() && c.wasAdded()) {
				c.getAddedSubList().forEach(entry -> {
					try {
						if (entry.getUrl().startsWith(AuthConstants.MICROSOFT_RESPONSE_URL)) {
							String authCode = entry.getUrl().substring(entry.getUrl().indexOf("=") + 1,
									entry.getUrl().indexOf("&"));
							Session session = new MicrosoftAuth().getAuthorizationCode(authCode);
							this.session.setUsername(session.getUsername());
							this.session.setToken(session.getToken());
							this.session.setUuid(session.getUuid());
							Stage stage = (Stage) root.getScene().getWindow();
							stage.close();
							this.isAuthed = true;
						} else {
							this.isAuthed = false;
						}
					} catch (Exception e) {
						e.printStackTrace();
						;
					}
				});
			}
		});
		return webView;
	}

	/**
	 * Connect to minecraft servers using POST request
	 * 
	 * @param username The username
	 * @param password The password
	 * @return The result
	 */
	public void connectMinecraft(String username, String password) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost httpPost = new HttpPost(AuthConstants.MOJANG_BASE_URL);
			StringEntity parameters = new StringEntity("{\"agent\":{\"name\":\"Minecraft\",\"version\":1},\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", ContentType.create(AuthConstants.APP_JSON));
			httpPost.addHeader("content-type", AuthConstants.APP_JSON);
			httpPost.setEntity(parameters);
			CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
			String jsonResponse = bufferedReader.readLine();
			Logger.log("Authentication Result: " + jsonResponse);
			if (!jsonResponse.contains("\"name\"")) {
				this.isAuthed = false;
			}
			MojangAuthResult authResult = AuthConstants.getGson().fromJson(jsonResponse, MojangAuthResult.class);
			this.session.setUsername(authResult.getSelectedProfile().getName());
			this.session.setToken(authResult.getAccessToken());
			this.session.setUuid(authResult.getSelectedProfile().getId());
			this.isAuthed = true;
		} catch (Exception exception) {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return If the user is Authed
	 */
	public boolean isLogged() {
		return this.isAuthed;
	}

	/**
	 * @return The session of the user
	 */
	public Session getSession() {
		return this.session;
	}
}
