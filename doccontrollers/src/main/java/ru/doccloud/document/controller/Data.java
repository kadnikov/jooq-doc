package ru.doccloud.document.controller;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JacksonXmlRootElement(localName = "data") public class Data {
	@JacksonXmlElementWrapper(localName = "criterion", useWrapping = false)
	public queryParam[] criterion;

	public queryParam[] getCriterions() {
		return criterion;
	}

	public void setCriterions(queryParam[] criterions) {
		this.criterion = criterions;
	}

	public Data(queryParam[] criterions) {
		super();
		this.criterion = criterions;
	}

	public Data() {
	}
}
