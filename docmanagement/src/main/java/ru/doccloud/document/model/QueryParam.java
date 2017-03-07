package ru.doccloud.document.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryParam {
	@JsonProperty("field")
	private String mfield;
	@JsonProperty("op")
	private String moperand;
	@JsonProperty("data")
	private String mvalue;
	
	public QueryParam(){
		super();
	}
	public QueryParam(String field, String operand, String value) {
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
