package ru.doccloud.document.model;

public class Link {
	private final Long head_id;
	
	private final Long tail_id;

	public Link(Long head_id, Long tail_id) {
		super();
		this.head_id = head_id;
		this.tail_id = tail_id;
	}

	public Long getTail_id() {
		return tail_id;
	}

	public Long getHead_id() {
		return head_id;
	}
	
}
