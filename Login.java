package com.zeptoh.benchmarking.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Login")
public class Login {
	@Id
	private String id;

	private String userId;
	private String clientId;
	private String reportId;
	private String password;
	private String firstName;
	private String lastName;
	private String role;
	private int wellUnitAccess;
	public int getWellUnitAccess() {
		return wellUnitAccess;
	}

	public void setWellUnitAccess(int wellUnitAccess) {
		this.wellUnitAccess = wellUnitAccess;
	}

	private boolean isLoggedIn = false;
	
	public Login(){
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public String getClientId() {
		return (this.clientId == null) ? "" : this.clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
}