package ru.doccloud.document.controller;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class queryParam {
	@JsonProperty("name")
	public String mfield;
	@JsonProperty("operation")
	public String moperand;
	@JsonProperty("value")
	public String mvalue;
	
	public queryParam(){
		super();
	}
	public queryParam(String field, String operand, String value) {
		super();
		this.mfield = field;
		this.moperand = operand;
		this.mvalue = value;
	}
	public String getField() {
		return mfield;
	}
	public void setField(String field) {
		this.mfield = field;
	}
	public String getOperand() {
		return moperand;
	}
	public void setOperand(String operand) {
		this.moperand = operand;
	}
	public String getValue() {
		return mvalue;
	}
	public void setValue(String value) {
		this.mvalue = value;
	}
	
	
}
