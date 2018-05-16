package cn.focus.dc.focusaudit.common.httputil.converter;

import cn.focus.dc.focusaudit.common.httputil.constants.Code;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (C) 2015 - 2016 SOHU FOCUS Inc., All Rights Reserved.
 * <p>
 * restTemplate 自定义收发转换器
 *
 * @Author: junlanli@sohu-inc.com
 * @Date: 2016-11-03
 */
public class FormHttpJSONMessageConverter implements HttpMessageConverter<JSONObject> {


    private static final Logger logger = Logger.getLogger(FormHttpJSONMessageConverter.class);

    private List<MediaType> supportedMediaTypes;

    public FormHttpJSONMessageConverter() {
        supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
    }

    @Override
    public boolean canRead(Class<?> aClass, MediaType mediaType) {
        return canProcess(aClass, mediaType);
    }

    private boolean canProcess(Class<?> clazz, MediaType mediaType) {
        if (!JSONObject.class.isAssignableFrom(clazz)) {
            return false;
        } else if (mediaType == null) {
            return true;
        } else {
            Iterator iterator = this.getSupportedMediaTypes().iterator();
            MediaType supportedMediaType;
            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                supportedMediaType = (MediaType) iterator.next();
            } while (!supportedMediaType.includes(mediaType));
            return true;
        }
    }

    @Override
    public boolean canWrite(Class<?> aClass, MediaType mediaType) {
        return canProcess(aClass, mediaType);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.unmodifiableList(this.supportedMediaTypes);
    }

    @Override
    public JSONObject read(Class<? extends JSONObject> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        MediaType contentType = httpInputMessage.getHeaders().getContentType();
        Charset charset = contentType.getCharSet() != null ? contentType.getCharSet() : Charset.forName("utf-8");
        String body = StreamUtils.copyToString(httpInputMessage.getBody(), charset);
        JSONObject result = JSON.parseObject(body);
        if (result.containsKey("code")) {
            if (result.getIntValue("code") == 1) {
                result.put("code", Code.SUCCESS);
            }
        } else if (result.containsKey("errorCode")) {
            if (result.getIntValue("errorCode") == 0) {
                result.put("code", Code.SUCCESS);
            } else {
                result.put("code", result.getInteger("errorCode"));
            }
            result.put("message", result.get("errorMessage"));
            result.remove("errorCode");
            result.remove("errorMessage");
        }
        return result;
    }

    @Override
    public void write(JSONObject jsonObject, MediaType mediaType, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        Charset charset;
        if (mediaType != null) {
            httpOutputMessage.getHeaders().setContentType(mediaType);
            charset = mediaType.getCharSet() != null ? mediaType.getCharSet() : Charset.forName("utf-8");
        } else {
            httpOutputMessage.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
            charset = Charset.forName("utf-8");
        }
        OutputStream os = httpOutputMessage.getBody();
        byte[] data = jsonObject.toJSONString().getBytes(charset);
        os.write(data);
        httpOutputMessage.getHeaders().setContentLength(data.length);
//        logger.info("Send HTTP Request:" + jsonObject.toJSONString());
    }
}
