package com.hh.function.resis_anti_crawler.verify.image.yunma;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hh.function.base.DataSource;
import lombok.Data;
import net.dongliu.requests.Requests;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//dependencies: net.dongliu.requests,com.alibaba.fastjson
//net.dongliu.requests 这个jar请看 https://blog.csdn.net/dxh0823/article/details/81137400/

@Data
public class YunmaBase implements InitializingBean {
    private YunmaConfig yunmaConfig;
    private Map<String, String> headers = new HashMap<>();
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(YunmaBase.class);

    public YunmaBase() {
        headers.put("Content-Type", "application/json");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    // 所有image 参数皆为 图片字节流base64之后的的字符串

    public String commonVerify(String imageContent) {
        String verify_type;
        verify_type = "10110";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image", imageContent);
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        return resp;
    }

    public String slideVerify(String slideImage, String backgroundImage) {
        // # 通用滑块
        // # 请保证购买相应服务后请求对应 verify_type
        // # verify_type="20111" 单次积分
        // # slide_image 需要识别图片的小图片的base64字符串
        // # background_image 需要识别图片的背景图片的base64字符串(背景图需还原)
        String verify_type;
        verify_type = "20101";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("slide_image", slideImage);
        jsonObject.put("background_image", backgroundImage);
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(resp);
        return resp;
    }

    public String sinSlideVerify(String Image) {
        // # 通用单图滑块(截图)  20110
        // # 请保证购买相应服务后请求对应 verify_type
        // # verify_type="20110" 单次积分
        String verify_type;
        verify_type = "20110";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image", Image);
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(resp);
        return resp;
    }


    public String trafficSlideVerify(String seed, String data, String href) {
        // # 定制-滑块协议slide_traffic  900010
        String verify_type;
        verify_type = "900010";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("seed", seed);
        jsonObject.put("data", data);
        jsonObject.put("href", href);
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(resp);
        return resp;
    }


    public String clickVerify(String image, String extra) {
        // # 通用任意点选1~4个坐标 30009
        // # 通用文字点选1(extra,点选文字逗号隔开,原图) 30100
        // # 定制-文字点选2(extra="click",原图) 30103
        // # 定制-单图文字点选 30102
        // # 定制-图标点选1(原图) 30104
        // # 定制-图标点选2(原图,extra="icon") 30105
        // # 定制-语序点选1(原图,extra="phrase") 30106
        // # 定制-语序点选2(原图) 30107
        // # 定制-空间推理点选1(原图,extra="请点击xxx") 30109
        // # 定制-空间推理点选1(原图,extra="请_点击_小尺寸绿色物体。") 30110
        // # 定制-tx空间点选(extra="请点击侧对着你的字母") 50009
        // # 定制-tt_空间点选 30101
        //# 定制-推理拼图1(原图,extra="交换2个图块") 30108
        //# 定制-xy4九宫格点选(原图,label_image,image) 30008
        //# 如有其他未知类型,请联系我们
        String verify_type;
        verify_type = "30100";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image", image);
        if (extra != null) {
            jsonObject.put("extra", extra);
        }
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(resp);
        return resp;
    }

    public String Rotate(String image) {
        //# 定制-X度单图旋转  90007

        String verify_type;
        verify_type = "90007";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image", image);
//        # 定制-Tt双图旋转,2张图,内圈图,外圈图  90004
//        verify_type = "90004";
//        jsonObject.put("out_ring_image", image);
//        verify_type = "90004";
//        jsonObject.put("inner_circle_image", image);
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(resp);
        return resp;

    }

    public String googleVerify(String googlekey, String pageurl) throws InterruptedException {
        //# googleVerify
        String data = null;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", yunmaConfig.getToken());
        jsonObject.put("type", "40010"); // v2
//        jsonObject.put("type", "40011");// v3
        jsonObject.put("googlekey", googlekey);
        jsonObject.put("pageurl", pageurl);
        jsonObject.put("enterprise", 0);
        jsonObject.put("invisible", 1);
        jsonObject.put("data-s", ""); //## V2+企业如果能找到，找不到传空字符串
        jsonObject.put("action", ""); //#V3必传
        jsonObject.put("min_score", ""); //#V3才支持的可选参数
        String jsonData;
        jsonData = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(jsonData);
        // # {'msg': '识别成功', 'code': 10000, 'data': {'code': 0, 'captchaId': '51436618130', 'recordId': '74892'}}
//        jsonData = "["+"{'msg': '识别成功', 'code': 10000, 'data': {'code': 0, 'captchaId': '51436618130', 'recordId': '74892'}}"+"]";
        jsonData = "[" + "{'msg': '识别成功', 'code': 10000, 'data': {'code': 0, 'captchaId': '51436618130', 'recordId': '74892'}}" + "]";
        JSONArray jsonArray = new JSONArray(jsonData);
        jsonData = "[" + jsonArray.getJSONObject(0).getObj("data") + "]";
        JSONArray jsonArray1 = new JSONArray(jsonData);
        String captchaId = (String) jsonArray1.getJSONObject(0).getObj("captchaId");
        String recordId = (String) jsonArray1.getJSONObject(0).getObj("recordId");
        System.out.println(captchaId);
        System.out.println(recordId);
        int times = 0;
        int is_solved = 0;
        while (times < 120 || is_solved == 0) {

            String url = "http://api.jfbym.com/api/YmServer/funnelApiResult";
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("token", yunmaConfig.getToken());
            jsonObject1.put("captchaId", captchaId);
            jsonObject1.put("recordId", recordId);
            String readToText = Requests.post(url).headers(headers).jsonBody(jsonObject).send().readToText();
//            String readToText = "{'msg': '请求成功', 'code': 10001, 'data': {'data': '03AGdBq2611GTOg'}}";
            if (readToText.contains("结果准备中，请稍后再试")) {
                times += 5;
                Thread.sleep(5000);
                continue;

            }
            if (readToText.contains("请求成功")) {
                JSONArray jsonArray2 = new JSONArray("[" + readToText + "]");
                jsonData = "[" + jsonArray2.getJSONObject(0).getObj("data") + "]";
                data = (String) new JSONArray(jsonData).getJSONObject(0).getObj("data");
                is_solved = 1;
                System.out.println(data);
                break;

            }


        }

        return data;
    }


    public String HcaptchaVerify(String site_key, String site_url) {
        //# Hcaptcha
        //# 请保证购买相应服务后请求对应 verify_type
        //# verify_type="50001"
        String verify_type;
        verify_type = "50001";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("site_key", site_key);
        jsonObject.put("site_url", site_url);
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(resp);
        return resp;

    }


    public String FunCaptchaVerify(String site_key, String site_url) {
        //# Hcaptcha
        //# 请保证购买相应服务后请求对应 verify_type
        //# verify_type="40007"
        String verify_type;
        verify_type = "40007";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("publickey", site_key);
        jsonObject.put("pageurl", site_url);
        jsonObject.put("type", verify_type);
        jsonObject.put("token", yunmaConfig.getToken());
        String resp = Requests.post(yunmaConfig.getUrl()).headers(headers).jsonBody(jsonObject).send().readToText();
        System.out.println(resp);
        return resp;

    }

    public static String ImageToBase64ByOnline(URL url) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            // 创建URL
            byte[] by = new byte[1024];
            // 创建链接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();
            // 将内容读取内存中
            int len = -1;
            while ((len = is.read(by)) != -1) {
                data.write(by, 0, len);
            }
            // 关闭流
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data.toByteArray());
    }


    public static String ImageToBase64ByLocal(String path) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            File img = new File(path);
            FileInputStream input = new FileInputStream(img);
            byte[] by = new byte[1024];

            // 将内容读取内存中
            int len = -1;
            while ((len = input.read(by)) != -1) {
                data.write(by, 0, len);
            }
            // 关闭流
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data.toByteArray());
    }
}
