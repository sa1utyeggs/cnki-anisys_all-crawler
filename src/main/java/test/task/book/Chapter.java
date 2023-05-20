package test.task.book;

import lombok.Data;

import java.util.Date;

@Data
public class Chapter {
    private String name;
    private Long index;
    private Boolean isFree;
    private String url;
    private Long wordCount;
    private Date releaseDate;
}
