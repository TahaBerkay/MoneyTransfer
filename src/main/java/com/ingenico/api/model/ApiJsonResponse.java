package com.ingenico.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public class ApiJsonResponse {
	private String error;
	private boolean success;
	private Object data;

	@JsonProperty(required = false)
	@ApiModelProperty(notes = "Error Message", required = false)
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@JsonProperty(required = true)
	@ApiModelProperty(notes = "Result", required = true)
	public boolean getSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@JsonProperty(required = false)
	@ApiModelProperty(notes = "Data", required = false)
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
