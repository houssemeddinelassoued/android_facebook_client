package fi.harism.facebook.dao;

import java.io.Serializable;
import java.util.Vector;

public class FBUser implements Serializable {

	private static final long serialVersionUID = 8817570826057668756L;
	
	private String id = null;
	private String jid = null;
	private String name = null;
	private String picture = null;
	private String status = null;
	private String hometownLocation = null;
	private String currentLocation = null;
	private String email = null;
	private String website = null;
	private Vector<String> affiliations = null;
	
	public enum Presence { CHAT, AWAY, GONE };
	private Presence presence;

	public FBUser() {
	}
	
	public Presence getPresence() {
		return presence;
	}
	
	public void setPresence(Presence presence) {
		this.presence = presence;
	}

	public Vector<String> getAffiliations() {
		return affiliations;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public String getEmail() {
		return email;
	}

	public String getHometownLocation() {
		return hometownLocation;
	}

	public String getId() {
		return id;
	}

	public String getJid() {
		return jid;
	}

	public String getName() {
		return name;
	}

	public String getPicture() {
		return picture;
	}

	public String getStatus() {
		return status;
	}

	public String getWebsite() {
		return website;
	}

	public void setAffiliations(Vector<String> affiliations) {
		this.affiliations = affiliations;
	}

	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setHometownLocation(String hometownLocation) {
		this.hometownLocation = hometownLocation;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

}
