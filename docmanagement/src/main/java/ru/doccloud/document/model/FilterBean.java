package ru.doccloud.document.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilterBean {
	@JsonProperty("groupOp")
	private String mgroupOp;
	
	@JsonProperty("rules")
	private List<QueryParam> mrules;

	public FilterBean() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getMgroupOp() {
		return mgroupOp;
	}

	public void setMgroupOp(String mgroupOp) {
		this.mgroupOp = mgroupOp;
	}

	public List<QueryParam> getMrules() {
		return mrules;
	}

	public void setMrules(List<QueryParam> mrules) {
		this.mrules = mrules;
	}
	
	
}
