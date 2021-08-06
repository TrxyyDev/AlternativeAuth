package fr.trxyy.alternative.alternative_auth.mojang.model;

public class Profile {
	public String id;
	public String name;
	public boolean legacy;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isLegacy() {
		return legacy;
	}
}