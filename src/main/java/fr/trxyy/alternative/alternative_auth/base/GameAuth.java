package fr.trxyy.alternative.alternative_auth.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import fr.trxyy.alternative.alternative_api.GameEngine;
import fr.trxyy.alternative.alternative_api.GameSize;
import fr.trxyy.alternative.alternative_auth.account.AccountType;
import fr.trxyy.alternative.alternative_auth.account.Session;
import fr.trxyy.alternative.alternative_auth.microsoft.MicrosoftAuth;
import fr.trxyy.alternative.alternative_auth.microsoft.ParamType;
import fr.trxyy.alternative.alternative_auth.microsoft.model.MicrosoftModel;
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
public class GameAuth {
	/**
	 * Is player authed ?
	 */
	public boolean isAuthenticated = false;
	/**
	 * The session
	 */
	private Session session = new Session();
	
	private AuthConfig authConfig;

	/**
	 * The Constructor
	 * 
	 * @param user The username/mail
	 * @param pwd  The password
	 * @param type The account Type (Mojang/Offline)
	 */
	public GameAuth(String user, String pwd, AccountType type) {
		AuthConstants.displayCopyrights();
		if (type.equals(AccountType.MOJANG)) {
			this.connectMinecraft(user, pwd);
		} else if (type.equals(AccountType.OFFLINE)) {
			this.setSession(user, TokenGenerator.generateToken(user), UUID.randomUUID().toString().replace("-", ""));
		}
	}

	public GameAuth(AccountType type) {
		AuthConstants.displayCopyrights();
	}

	/**
	 * Connect to minecraft with a Microsoft account
	 * 
	 * @param root The parent to show in
	 * @return The result in a WebView
	 */
	public WebView connectMicrosoft(GameEngine engine, Pane root) {
			this.authConfig = new AuthConfig(engine);
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
							if (this.authConfig.canRefresh()) {
								Logger.log("Trying to logIn with RefreshToken.");
								this.authConfig.loadConfiguration();
								MicrosoftModel model = null;
								try {
									model = new MicrosoftAuth().getAuthorizationCode(ParamType.REFRESH, authConfig.microsoftModel.getRefresh_token());
								} catch (Exception e) {
									e.printStackTrace();
								}
								authConfig.updateValues(model);
								Session result = null;
								try {
									result = new MicrosoftAuth().getLiveToken(model.getAccess_token());
								} catch (Exception e) {
									e.printStackTrace();
									if (entry.getUrl().startsWith(AuthConstants.MICROSOFT_RESPONSE_URL)) {
										String authCode = entry.getUrl().substring(entry.getUrl().indexOf("=") + 1, entry.getUrl().indexOf("&"));
										MicrosoftModel model2 = new MicrosoftAuth().getAuthorizationCode(ParamType.AUTH, authCode);
										authConfig.createConfigFile(model2);
										Session result2 = new MicrosoftAuth().getLiveToken(model2.getAccess_token());
										this.setSession(result2.getUsername(), result2.getToken(), result2.getUuid());
										Stage stage = (Stage) root.getScene().getWindow();
										stage.close();
									} else {
										this.isAuthenticated = false;
									}
								}
								this.setSession(result.getUsername(), result.getToken(), result.getUuid());
								Stage stage = (Stage) root.getScene().getWindow();
								stage.close();
							}
							else {
								if (entry.getUrl().startsWith(AuthConstants.MICROSOFT_RESPONSE_URL)) {
									String authCode = entry.getUrl().substring(entry.getUrl().indexOf("=") + 1, entry.getUrl().indexOf("&"));
									MicrosoftModel model = new MicrosoftAuth().getAuthorizationCode(ParamType.AUTH, authCode);
									authConfig.createConfigFile(model);
									Session result = new MicrosoftAuth().getLiveToken(model.getAccess_token());
									this.setSession(result.getUsername(), result.getToken(), result.getUuid());
									Stage stage = (Stage) root.getScene().getWindow();
									stage.close();
								} else {
									this.isAuthenticated = false;
								}
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
	 * Set the session credentials
	 * @param user
	 * @param token
	 * @param id
	 */
	private void setSession(String user, String token, String id) {
		this.session.setUsername(user);
		this.session.setToken(token);
		this.session.setUuid(id);
		this.isAuthenticated = true;
		Logger.log("Connected Successfully !");
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
			if (!jsonResponse.contains("\"name\"")) {
				this.isAuthenticated = false;
			}
			MojangAuthResult authResult = AuthConstants.getGson().fromJson(jsonResponse, MojangAuthResult.class);
			this.setSession(authResult.getSelectedProfile().getName(), authResult.getAccessToken(), authResult.getSelectedProfile().getId());
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
	 * @return  If the user is successfully authenticated
	 */
	public boolean isLogged() {
		return this.isAuthenticated;
	}

	/**
	 * @return The session of the user
	 */
	public Session getSession() {
		return this.session;
	}
}
