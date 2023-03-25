package com.hh.task.qidian;

import com.hh.task.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jsoup.nodes.Document;

/**
 * @author ab875
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class RankTask extends Task<Object> {
    private String url;


    @Override
    public Object call() throws Exception {
        Document document = HTTP_CONNECTION_POOL.get(url, null, null);
        System.out.println(document.text());
        return null;
    }
}
