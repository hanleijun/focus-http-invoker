package cn.focus.dc.focusaudit.common.httputil.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * Copyright (C) 1998 - 2017 SOHU Inc., All Rights Reserved.
 * <p>
 * encrypt utilities
 * @Author: leijunhan (leijunhan@sohu-inc.com)
 * @Date: 2017/11/16
 */
public class EncryptUtil {

    public static final String SPLITTER = "#";

    /**
     * encode text with md5
     * @param plainTxt
     * @return
     */
    public static String encodeMd5(String plainTxt){
        String str = "";
        int len = plainTxt.length(), i, j;
        for (i = 0, j = len / 2; i < len / 2; ++i, ++j) {
            str += plainTxt.substring(i, i + 1) + plainTxt.substring(j, j + 1);
        }
        if (j < len - 1) {
            str += plainTxt.substring(j, j + 1);
        }
        String m1 = Hashing.md5().newHasher().putString(str, Charsets.UTF_8).hash().toString();
        return Hashing.md5().newHasher().putString(m1, Charsets.UTF_8).hash().toString();
    }

    /**
     * encode text with base64
     * @param plainTxt
     * @return
     */
    public static String encodeBase64(String plainTxt){
        String encodeStr = StringUtils.EMPTY;
        try {
            encodeStr = new String(Base64.getEncoder().encode(plainTxt.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * decode base64
     * @param encodeTxt
     * @return
     */
    public static String decodeBase64(String encodeTxt){
        String decodeStr = StringUtils.EMPTY;
        try {
            decodeStr = new String(Base64.getDecoder().decode(encodeTxt), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodeStr;
    }

    /**
     * encode a http request string
     * @param hostPrefix
     * @param reqPath
     * @param paramStr
     * @return
     */
    public static String encodeRequest(String hostPrefix, String env, String reqPath, String paramStr){
        if(StringUtils.isNotBlank(hostPrefix)){
            return encodeBase64(hostPrefix + SPLITTER + env + SPLITTER + reqPath + SPLITTER + paramStr);
        }else{
            return StringUtils.EMPTY;
        }
    }

    public static String extractHostPrefixFromEncode(String encode){
        String decode = decodeBase64(encode);
        return StringUtils.substringBefore(decode, SPLITTER);
    }

    public static String extractReqPathFromEncode(String encode){
        String decode = decodeBase64(encode);
        String first = StringUtils.substringAfter(decode, SPLITTER);
        return StringUtils.substringBeforeLast(first, SPLITTER);
    }

    public static String extractParamStrFromEncode(String encode){
        String decode = decodeBase64(encode);
        return StringUtils.substringAfterLast(decode, SPLITTER);
    }
}
