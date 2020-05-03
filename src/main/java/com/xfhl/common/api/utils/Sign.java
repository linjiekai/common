package com.xfhl.common.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
public class Sign {

    //生成待签名串
    public static String getPlain(Map<String, Object> requestMap, String[] keys) throws Exception {
        StringBuffer plain = new StringBuffer("");

        //数组按KEY排序
        Arrays.sort(keys);

        Object value = null;
        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && !(value instanceof List) && !(value instanceof Map)) {
                plain.append(key + "=" + value.toString().trim() + "&");
            }
        }
        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();
    }

    //生成待签名串
    public static String getPlain(Map<String, Object> requestMap) throws Exception {
        return getPlain(requestMap, false);
    }

    /**
     * 生成待签名串
     *
     * @param requestMap
     * @param composite  false去掉复合类型  true保留复合类型
     * @return
     * @throws Exception
     */
    public static String getPlain(Map<String, Object> requestMap, boolean composite) throws Exception {
        StringBuffer plain = new StringBuffer("");

        List<String> keys = new ArrayList<String>(requestMap.keySet());
        Collections.sort(keys);

        Object value = null;

        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && (composite || (!(value instanceof List) && !(value instanceof Map)))
            ) {
                plain.append(key + "=" + value.toString().trim() + "&");
            }
        }

        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();
    }

    public static String getPlainURLEncoder(Map<String, Object> requestMap, String charset) throws IOException {
        StringBuffer plain = new StringBuffer("");

        List<String> keys = new ArrayList<String>(requestMap.keySet());
        Collections.sort(keys);

        Object value = null;

        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && !(value instanceof List) && !(value instanceof Map)
            ) {
                plain.append(key + "=" + URLEncoder.encode(value.toString().trim(), charset) + "&");
            }
        }

        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();

    }

    //签名
    public static String sign(Map<String, Object> requestMap, String key) throws Exception {
        String plain = Sign.getPlain(requestMap);
        plain += "&key=" + key;
        log.info("plain[{}]", plain);
        String sign = Sign.sign(plain);
        log.info("sign[{}]", sign);
        return sign;
    }

    //签名
    public static String signToHex(String plain) throws Exception {

        byte[] data;
        data = MD5Sign.encode(plain.getBytes("UTF-8"));
        return HexStr.bytesToHexString(data);
    }

    //签名
    public static String sign(String plain) throws Exception {
        return Base64.encodeBase64String(signToHex(plain).getBytes());
    }

    //验证签名
    public static boolean verify(String plain, String sign) throws Exception {
        if (sign.equalsIgnoreCase(sign(plain))) {
            return true;
        }

        return false;
    }

    //验证签名
    public static boolean verifyToHex(String plain, String sign) throws Exception {
        if (sign.equalsIgnoreCase(signToHex(plain))) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) throws Exception {
    	
    	String plain = "X-MPMALL-SignVer=v1&address=广州银行大厦&areaId=440106&authType=B&cardNo=3PerpjNa3sgF97x1F9s5KGbcTvyFkTIJ2DaGaGch7+Y=&cardType=0&cityId=440100&imgFront=http://static-xfyinli.yinli.gdxfhl.com/idCard/xfyinli/q68bttepnt52bktmzmas.png&mercId=888000000000003&mobile=13302874141&name=谭珺&platform=XFYLMALL&provinceId=440000&sysCnl=IOS&timestamp=1574760331&wxNo=junzhu8951&key=HsB1ZqZUr6UDG553e37e8S06JN3vj1ng";
    	
    	System.out.println(sign(plain));
    	
//鉴权失败:Get Sign:YjQ4MjkzMzhjOTY2NTU2NmJlNTNkNGM3NGVmZTNjYzM=, Server Sign:Y2IyNmViZjc5MDAwYzQxZGI1ODgwMjQ3ZGRhYmVjMDA=
    		
    		
    	System.out.println();
    	
    	
        try {
        	/**
            String before = "asdf";
            byte[] plainText = before.getBytes("UTF8");
//形成RSA公钥对 
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair key = keyGen.generateKeyPair();
//使用私钥签名********************************************************** 
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(key.getPrivate());
//sig对象得到私钥 
//签名对象得到原始数据 
            sig.update(plainText);
//sig对象得到原始数据(现实中用的是原始数据的摘要，摘要的是单向的，即摘要算法后无法解密) 
            byte[] signature = sig.sign();
//sig对象用私钥对原始数据进行签名，签名后得到签名signature 
            System.out.println(sig.getProvider().getInfo());
            String after1 = new String(signature, "UTF8");
            System.out.println("用私钥签名后:" + after1);
//使用公钥验证 
            sig.initVerify(key.getPublic());
//sig对象得到公钥 
//签名对象得到原始信息 
            sig.update(plainText);
*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
