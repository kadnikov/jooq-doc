package ru.doccloud.service.document.dto;

import org.jtransfo.DomainClass;

@DomainClass("ru.doccloud.document.model.Link")
public class LinkDTO {
    private  Long head_id;

    private Long tail_id;

    public LinkDTO() {
    }

    public LinkDTO(Long head_id, Long tail_id) {
        this.head_id = head_id;
        this.tail_id = tail_id;
    }

    public Long getHead_id() {
        return head_id;
    }

    public void setHead_id(Long head_id) {
        this.head_id = head_id;
    }

    public Long getTail_id() {
        return tail_id;
    }

    public void setTail_id(Long tail_id) {
        this.tail_id = tail_id;
    }

    @Override
    public String toString() {
        return "LinkDTO{" +
                "head_id=" + head_id +
                ", tail_id=" + tail_id +
                '}';
    }
}
