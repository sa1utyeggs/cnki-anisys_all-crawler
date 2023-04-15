package com.hh.function.resis_anti_crawler.verify.image.yunma;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hh.function.resis_anti_crawler.verify.image.ImageIdentifier;
import com.hh.utils.JsonUtils;
import lombok.Data;

import java.io.File;
import java.net.URL;

@Data
public class YunmaImageIdentifier implements ImageIdentifier {
    private Integer Type;
    private YunmaBase yunmaBase;


    @Override
    public String identify(URL url) {
        return identifyBase64(YunmaBase.ImageToBase64ByOnline(url));
    }

    @Override
    public String identify(String filepath) {
        // String baseUrl = JsonUtils.class.getResource("/").getPath() + "test/img/";
        // to base64 and identify
        return identifyBase64(YunmaBase.ImageToBase64ByLocal(filepath));
    }


    private String identifyBase64(String imgContent) {
        // transfer
        String s = yunmaBase.commonVerify(imgContent);
        // response
        JSONObject json = JSON.parseObject(s);
        Integer code = json.getInteger("code");
        if (code == 10000) {
            JSONObject data = json.getJSONObject("data");
            return data.getString("data");
        } else {
            throw new RuntimeException(json.toJSONString());
        }
    }

}
