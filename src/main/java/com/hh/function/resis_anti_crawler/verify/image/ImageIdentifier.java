package com.hh.function.resis_anti_crawler.verify.image;

import java.io.File;
import java.net.URL;

public interface ImageIdentifier {
    /**
     * 直接根据 URL 返回
     *
     * @param url URL
     * @return text
     */
    public String identify(URL url);

    /**
     * 读取本地文件返回
     *
     * @param filepath file path
     * @return text
     */
    public String identify(String filepath);
}
