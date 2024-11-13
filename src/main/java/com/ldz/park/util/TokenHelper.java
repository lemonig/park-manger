package com.ldz.park.util;

import java.util.UUID;

public class TokenHelper {
    public static String getGUID(){
        String guid = UUID.randomUUID().toString();
        return "a" + guid.replace("-", "");
    }
}
