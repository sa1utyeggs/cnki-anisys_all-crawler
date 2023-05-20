package test.task.book;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Book {
    private String url;
    private String name;
    private String author;
    private String intro;
    private String posterUrl;
    private List<String> tags;

    private Date lastUpadate;
    private Long wordCount;
    private Long recommend;

    private String introduction;
    private List<Chapter> chapters;

}
