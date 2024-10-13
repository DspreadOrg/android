package com.dspread.demoui.utils;

/**
 * @author user
 */
public class JsonHandler {

    public static String parseJsonWithQuotes(String jsonString) {
        String processedJson = processQuotes(jsonString);
        try {
            return processedJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String processQuotes(String oldJson) {
        StringBuffer stringBuffer = new StringBuffer();
        String[] split = oldJson.split(",");
        for (int i = 0; i < split.length; i++) {
            if(i==5){
                String ModifyContent = split[i];
                String[] split1 = ModifyContent.split(":");
                if(split1.length>2){
                    StringBuffer sb = new StringBuffer();
                    for (int k = 0; k < split1.length; k++) {
                        if(k>0){
                          sb.append(split1[k].replaceAll("\"",""));

                        }else {
                            sb.append(split1[k]);
                            sb.append(":");
                            sb.append("\"");
                        }
                    }
                    stringBuffer.append(sb+"\"");
                    stringBuffer.append(",");
                }
            }else {
                stringBuffer.append(split[i]);
                if(i<split.length-1){
                    stringBuffer.append(",");
                }
            }
        }
        return  stringBuffer.toString();
    }
}
