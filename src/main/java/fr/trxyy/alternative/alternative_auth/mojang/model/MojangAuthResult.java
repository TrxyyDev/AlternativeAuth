package fr.trxyy.alternative.alternative_auth.mojang.model;

import java.util.List;

public class MojangAuthResult {
	private String accessToken;
	private String clientToken;
	private Profile selectedProfile;
	private List<Profile> availableProfiles;

	public String getAccessToken() {
		return accessToken;
	}

	public String getClientToken() {
		return clientToken;
	}

	public Profile getSelectedProfile() {
		return selectedProfile;
	}

	public List<Profile> getAvailableProfiles() {
		return availableProfiles;
	}
}