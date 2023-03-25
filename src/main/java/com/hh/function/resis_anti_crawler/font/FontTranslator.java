package com.hh.function.resis_anti_crawler.font;

import lombok.Data;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Map;

/**
 * @author ab875
 */
@Data
public class FontTranslator {
    private String textUrl;


    /**
     * 翻译字体文件
     * @param doc
     * @return
     */
    public Document translate(Document doc) {
        // ....
        return doc;
    }

    /**
     * 处理字体文件，并返回对应值；
     * @return map
     */
    private Map<String, String> disposeFontFile() {
        return null;
    }
}
