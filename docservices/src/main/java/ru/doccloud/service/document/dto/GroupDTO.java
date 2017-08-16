package ru.doccloud.service.document.dto;


public class GroupDTO {
	private String title;
    private String id;
    
    public GroupDTO(String title, String id) {
        this.title = title;
        this.id = id;
    }

	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}
    
    
}
