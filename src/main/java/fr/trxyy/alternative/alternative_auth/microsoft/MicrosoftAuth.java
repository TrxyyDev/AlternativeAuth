package fr.trxyy.alternative.alternative_auth.microsoft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;

import fr.trxyy.alternative.alternative_auth.account.Session;
import fr.trxyy.alternative.alternative_auth.base.AuthConstants;
import fr.trxyy.alternative.alternative_auth.base.Logger;
import fr.trxyy.alternative.alternative_auth.microsoft.model.MicrosoftModel;
import fr.trxyy.alternative.alternative_auth.microsoft.model.MinecraftMicrosoftModel;
import fr.trxyy.alternative.alternative_auth.microsoft.model.MinecraftProfileModel;
import fr.trxyy.alternative.alternative_auth.microsoft.model.MinecraftStoreModel;
import fr.trxyy.alternative.alternative_auth.microsoft.model.XboxLiveModel;

public class MicrosoftAuth {
	
    public Session getAuthorizationCode(String authCode) throws Exception
    {
            MicrosoftModel model = AuthConstants.getGson().fromJson(this.connectMicrosoft(authCode), MicrosoftModel.class);
            Logger.log("authorizationCode: " + model.getAccess_token());
            return this.getLiveToken(model.getAccess_token());
    }
    
    private Session getLiveToken(String accessToken) throws Exception
    {
    		XboxLiveModel model = AuthConstants.getGson().fromJson(this.postInformations(ParamType.XBL, AuthConstants.MICROSOFT_AUTHENTICATE_XBOX, accessToken, null), XboxLiveModel.class);
        	Logger.log("XBLive Token: " + model.getToken());
        	return this.getXsts(model.getToken());
    }

    private Session getXsts(String liveToken) throws Exception
    {
            XboxLiveModel model = AuthConstants.getGson().fromJson(this.postInformations(ParamType.XSTS, AuthConstants.MICROSOFT_AUTHORIZE_XSTS, liveToken, null), XboxLiveModel.class);
            Logger.log("Xsts Token: " + model.getToken());
            Logger.log("UserHash:  " + model.getDisplayClaims().getUsers()[0].getUhs());
            return this.getMinecraftToken(model.getDisplayClaims().getUsers()[0].getUhs(), model.getToken());
    }
    
    private Session getMinecraftToken(String userHash, String xsts) throws Exception
    {
    	MinecraftMicrosoftModel model = AuthConstants.getGson().fromJson(this.postInformations(ParamType.MC, AuthConstants.MICROSOFT_LOGIN_XBOX, userHash, xsts), MinecraftMicrosoftModel.class);
		Logger.log("access_token: " +  model.getAccess_token());
		Logger.log("expires: " +  model.getExpires_in());
		Logger.log("token type: " +  model.getToken_type());
		Logger.log("username: " +  model.getUsername());
		return this.checkMinecraftStore(model.getToken_type(), model.getAccess_token());
    }

    private Session checkMinecraftStore(String tokenType, String mcAccessToken)
    {
    	 MinecraftStoreModel model = AuthConstants.getGson().fromJson(this.connectToMinecraft(AuthConstants.MICROSOFT_MINECRAFT_STORE, tokenType + " " + mcAccessToken), MinecraftStoreModel.class);
    	 Logger.log("keyId: " + model.getKeyId());
    	 Logger.log("signature: " + model.getSignature());
    	 Logger.log("items: " + model.getItems());
    	 return this.getMinecraftProfile(tokenType, mcAccessToken);
    }

    private Session getMinecraftProfile(String tokenType, String mcAccessToken)
    {
    	MinecraftProfileModel model = AuthConstants.getGson().fromJson(this.connectToMinecraft(AuthConstants.MICROSOFT_MINECRAFT_PROFILE, tokenType + " " + mcAccessToken), MinecraftProfileModel.class);
    	Logger.log("id: " + model.getId());
    	Logger.log("name: " + model.getName());
    	Logger.log("skins: " + model.getSkins());
		final String uuidValid = model.getId().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
		return new Session(model.getName(), mcAccessToken, uuidValid);
    }
    
    public String formatMicrosoft(String authCode) {
    	final StringBuilder builder = new StringBuilder();
        try
        {
            for (Entry<Object, Object> entry : getAuthParameters(ParamType.AUTH, authCode, null).entrySet())
            {
                if (builder.length() > 0) builder.append("&");
                builder.append(URLEncoder.encode(entry.getKey().toString(), AuthConstants.UTF_8.name()));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.getValue().toString(), AuthConstants.UTF_8.name()));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return builder.toString();
    }
    
    private String connectMicrosoft(String authCode)
    {
        HttpsURLConnection httpUrlConnection = null;
        byte[] bytes = null;
        try
        {
        	bytes = this.formatMicrosoft(authCode).getBytes(AuthConstants.UTF_8);
            httpUrlConnection = (HttpsURLConnection) new URL(AuthConstants.MICROSOFT_AUTH_TOKEN).openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Content-Type", AuthConstants.URL_ENCODED);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);

            final OutputStream outputStream = httpUrlConnection.getOutputStream();
            outputStream.write(bytes, 0, bytes.length);
            outputStream.close();
            BufferedReader bufferedReader = null;
            String body = "";
            if(httpUrlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), AuthConstants.UTF_8));
            }
            else if(httpUrlConnection.getErrorStream() != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getErrorStream(), AuthConstants.UTF_8));
            }
			if (bufferedReader != null) {
				body =  bufferedReader.lines().collect(Collectors.joining());
				bufferedReader.close();
			}
			return body;
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            if (httpUrlConnection != null)
                httpUrlConnection.disconnect();
        }
        return "";
    }
    
    private String postInformations(ParamType type, String url, String authCode, String code2)
    {
        HttpsURLConnection httpUrlConnection = null;
        BufferedReader bufferedReader = null;
        byte[] bytes = null;
        String json = "";
        try {
        	bytes = new JSONObject(this.getAuthParameters(type, authCode, code2)).toJSONString().getBytes(AuthConstants.UTF_8);
			httpUrlConnection = (HttpsURLConnection) new URL(url).openConnection();
			httpUrlConnection.setRequestMethod("POST");
			httpUrlConnection.setRequestProperty("Content-Type", AuthConstants.APP_JSON);
			httpUrlConnection.addRequestProperty("Accept", AuthConstants.APP_JSON);
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setDoInput(true);

            final OutputStream outputStream = httpUrlConnection.getOutputStream();
            outputStream.write(bytes, 0, bytes.length);
            outputStream.close();
            if(httpUrlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), AuthConstants.UTF_8));
            } 
            else if(httpUrlConnection.getErrorStream() != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getErrorStream(), AuthConstants.UTF_8));
            }
			if (bufferedReader != null) {
				String line;
                final StringBuilder stringBuilder = new StringBuilder();
                while((line = bufferedReader.readLine()) != null)
                    stringBuilder.append(line);
                json = stringBuilder.toString();
			}
			return json;
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally {
            if (httpUrlConnection != null)
				httpUrlConnection.disconnect();
        }
        return "";
    }
    
	private String connectToMinecraft(String url, String fullAuthorization) {
		BufferedReader bufferedReader = null;
		HttpURLConnection httpUrlConnection = null;
		String json = "";
		try {
			httpUrlConnection = (HttpsURLConnection) new URL(url).openConnection();
			httpUrlConnection.setRequestProperty("Accept", AuthConstants.APP_JSON);
			httpUrlConnection.setRequestProperty("Authorization", fullAuthorization);
			httpUrlConnection.setDoOutput(false);
			httpUrlConnection.setDoInput(true);
			if(httpUrlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
			    bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), AuthConstants.UTF_8));
			} 
			else if(httpUrlConnection.getErrorStream() != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getErrorStream(), AuthConstants.UTF_8));
			}

			if (bufferedReader != null) {
				String line;
				final StringBuilder stringBuilder = new StringBuilder();
				try {
					while ((line = bufferedReader.readLine()) != null)
						stringBuilder.append(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
				json = stringBuilder.toString();
			}
			
			Logger.log("logged:    " + json);

	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		if (httpUrlConnection != null)
			httpUrlConnection.disconnect();
	}
	return json;
	}
    
    protected Map<Object, Object> getAuthParameters(ParamType param, String code, String code2)
    {
        Map<Object, Object> parameters = new HashMap<Object, Object>();
        
        if (param.equals(ParamType.AUTH)) {
            parameters.put("client_id", "00000000402b5328");
            parameters.put("code", code);
            parameters.put("grant_type", "authorization_code");
            parameters.put("redirect_uri", "https://login.live.com/oauth20_desktop.srf");
            parameters.put("scope", "service::user.auth.xboxlive.com::MBI_SSL");
        }
        
        if (param.equals(ParamType.XBL)) {
            final Map<Object, Object> properties = new HashMap<Object, Object>();
            properties.put("AuthMethod", "RPS");
            properties.put("SiteName", "user.auth.xboxlive.com");
            properties.put("RpsTicket", code);
            parameters.put("Properties", properties);
            parameters.put("RelyingParty", "http://auth.xboxlive.com");
            parameters.put("TokenType", "JWT");
        }
        
        if (param.equals(ParamType.XSTS)) {
            final Map<Object, Object> properties = new HashMap<Object, Object>();
            properties.put("SandboxId", "RETAIL");
            properties.put("UserTokens", Collections.singletonList(code));
            parameters.put("Properties", properties);
            parameters.put("RelyingParty", "rp://api.minecraftservices.com/");
            parameters.put("TokenType", "JWT");
        }
        
        if (param.equals(ParamType.MC)) {
            parameters.put("identityToken", "XBL3.0 x=" + code + ";" + code2);
        }
        return parameters;
    }
    
	public static void main(String[] args) {
		try {
			new MicrosoftAuth().getAuthorizationCode("M.R3_BL2.41dd4d80-7e6b-9e97-52a7-6ee1ab67af4c");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
