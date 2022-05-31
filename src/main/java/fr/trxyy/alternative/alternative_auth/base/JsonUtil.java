package fr.trxyy.alternative.alternative_auth.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Trxyy
 */
public class JsonUtil {

	/**
	 * @return A good Gson to read Minecraft version json
	 */
	public static Gson getGson() {
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.enableComplexMapKeySerialization();
		gsonBuilder.setPrettyPrinting();
		return gsonBuilder.create();
	}

	/**
	 * @param inUrl The url to load
	 * @return A String with the Json
	 * @throws IOException
	 */
	public static String loadJSON(String inUrl) throws IOException {
		URL url = new URL(inUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String json = new String();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			json = json + inputLine;
		}
		return json;
	}

}
