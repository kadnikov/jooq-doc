package ru.doccloud.document.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class filterBean {
	@JsonProperty("groupOp")
	public String mgroupOp;
	
	@JsonProperty("rules")
	public List<queryParam> mrules;

	public filterBean() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getMgroupOp() {
		return mgroupOp;
	}

	public void setMgroupOp(String mgroupOp) {
		this.mgroupOp = mgroupOp;
	}

	public List<queryParam> getMrules() {
		return mrules;
	}

	public void setMrules(List<queryParam> mrules) {
		this.mrules = mrules;
	}
	
	
}
