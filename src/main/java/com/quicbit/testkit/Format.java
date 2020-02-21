package com.quicbit.testkit;

import java.util.Iterator;
import java.util.Map;

public class Format {
    public static String formatArr(Object[] a, boolean withBraces) {
        StringBuilder sb = new StringBuilder();
        if (withBraces) { sb.append("["); }
        int lim = a.length-1;
        for(int i=0; i<=lim; i++) {
            sb.append(format(a[i]));
            if (i != lim) {
                sb.append(",");
            }
        }
        if(withBraces) { sb.append("]"); }
        return sb.toString();
    }

    public static String formatMap(Map m) {
        if (m.size() == 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator keys = m.keySet().iterator();
        while(keys.hasNext()) {
            Object k = keys.next();
            sb.append(k);
            sb.append(":");
            sb.append(format(m.get(k)));
            if(keys.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static String format (Object obj) {
        if (obj == null) {
            return "null";
        }
        String ret;
        Class c = obj.getClass();
        if (c.isArray()) {
            ret = formatArr((Object[])obj, true);
        } else if (c == String.class) {
            ret = "\"" + obj + "\"";
        } else if (obj instanceof Map) {
            return formatMap((Map) obj);
        } else {
            ret = String.valueOf(obj);
        }
        ret = ret.replaceAll("\\r", "");
        ret = ret.replaceAll("\\n", " ");
        return ret;
    }
}
