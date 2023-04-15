package com.hh.function.resis_anti_crawler.verify.image.tess;

import com.hh.function.resis_anti_crawler.verify.image.ImageIdentifier;

import java.io.File;
import java.net.URL;

// Tess4j 是对 tesseract API 的 Java 封装
// 使用前需要安装 tesseract
// Tess4j: https://github.com/nguyenq/tess4j
// tesseract install: https://tesseract-ocr.github.io/tessdoc/Installation.html
public class Tess4jImageIdentifier implements ImageIdentifier {
    @Override
    public String identify(URL url) {
        return null;
    }

    @Override
    public String identify(String filename) {
        return null;
    }
}
