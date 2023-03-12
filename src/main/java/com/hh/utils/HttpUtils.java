package com.hh.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ab875
 */
public class HttpUtils {

    public static Document getDocument(CloseableHttpResponse response) throws IOException {
        Document document = null;
        InputStream in = null;
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                document = Jsoup.parse(IOUtils.toString(in, StandardCharsets.UTF_8));
                // 消费 entity
                EntityUtils.consume(entity);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            // 将 response 关闭后，就能将连接放回连接池
            response.close();
        }
        return document;
    }
}
