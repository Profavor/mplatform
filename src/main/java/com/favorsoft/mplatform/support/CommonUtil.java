package com.favorsoft.mplatform.support;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

public class CommonUtil {

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static String safeString(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    public static String getFileNm(String browser, String fileNm) {
        String reFileNm = null;
        try {
            if (browser.equals("MSIE") || browser.equals("Trident") || browser.equals("Edge")) {
                reFileNm = URLEncoder.encode(fileNm, "UTF-8").replaceAll("\\+", "%20");
            } else {
                if (browser.equals("Chrome")) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < fileNm.length(); i++) {
                        char c = fileNm.charAt(i);
                        if (c > '~') {
                            sb.append(URLEncoder.encode(Character.toString(c), "UTF-8"));
                        } else {
                            sb.append(c);
                        }
                    }
                    reFileNm = sb.toString();
                } else {
                    reFileNm = new String(fileNm.getBytes("UTF-8"), "ISO-8859-1");
                }
                if (browser.equals("Safari") || browser.equals("Firefox")) reFileNm = URLDecoder.decode(reFileNm);
            }
        } catch (Exception e) {
        }
        return reFileNm;
    }

    public static String getBrowser(HttpServletRequest req) {
        String userAgent = req.getHeader("User-Agent");
        if (userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("Trident") > -1 || userAgent.indexOf("Edge") > -1) {
            return "MSIE";
        } else if (userAgent.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (userAgent.indexOf("Opera") > -1) {
            return "Opera";
        } else if (userAgent.indexOf("Safari") > -1) {
            return "Safari";
        } else if (userAgent.indexOf("Firefox") > -1) {
            return "Firefox";
        } else {
            return null;
        }
    }

    public static String pvColumn(String propId){
        return "PV_" + propId;
    }


}
