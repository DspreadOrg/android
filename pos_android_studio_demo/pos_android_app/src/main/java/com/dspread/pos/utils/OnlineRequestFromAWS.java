package com.dspread.pos.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP request utility class implemented using Java standard library, no external dependencies
 */


public class OnlineRequestFromAWS {

    private static final String KEK_ALIAS = "alias/tr34-key-import";  //0123456789ABCDEFFEDCBA9876543210
    private static final String DUKPT_ALIAS = "alias/MerchantTerminal_TDES_BDK"; //0123456789ABCDEFFEDCBA9876543210

    private static final String AES_KEK_ALIAS = "alias/tr34-aes-key-import";  //0123456789ABCDEFFEDCBA9876543210
    private static final String AES_DUKPT_ALIAS = "alias/MerchantTerminal_BDK_AES_128"; //0123456789ABCDEFFEDCBA9876543210

    private static final String FUNCTION_URL = "https://ypparbjfugzgwijijfnb.supabase.co/functions/v1/dukpt-decrypt";
    private static final String EXPORT_FUNCTION_URL = "https://ypparbjfugzgwijijfnb.supabase.co/functions/v1/export-tr31";
    
    // Set connection timeout (milliseconds)
    private static final int CONNECT_TIMEOUT = 30000;
    // Set read timeout (milliseconds)
    private static final int READ_TIMEOUT = 30000;

    // Simple JSON building utility method
    private static String buildJsonString(Map<String, String> simpleFields, Map<String, Map<String, String>> nestedObjects) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        
        boolean first = true;
        
        // Add simple fields
        if (simpleFields != null) {
            for (Map.Entry<String, String> entry : simpleFields.entrySet()) {
                if (!first) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\"").append(entry.getKey()).append("\":\"")
                           .append(escapeJsonString(entry.getValue())).append("\"");
                first = false;
            }
        }
        
        // Add nested objects
        if (nestedObjects != null) {
            for (Map.Entry<String, Map<String, String>> entry : nestedObjects.entrySet()) {
                if (!first) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\"").append(entry.getKey()).append("\":{");
                
                boolean nestedFirst = true;
                Map<String, String> nestedMap = entry.getValue();
                for (Map.Entry<String, String> nestedEntry : nestedMap.entrySet()) {
                    if (!nestedFirst) {
                        jsonBuilder.append(",");
                    }
                    jsonBuilder.append("\"").append(nestedEntry.getKey()).append("\":\"")
                               .append(escapeJsonString(nestedEntry.getValue())).append("\"");
                    nestedFirst = false;
                }
                jsonBuilder.append("}");
                first = false;
            }
        }
        
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
    
    // Simple JSON string escaping
    private static String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    // Generic method to send HTTP POST request
    private static String sendPostRequest(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Set connection properties
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            // Write request body
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(jsonBody);
                wr.flush();
            }
            
            // Read response
        StringBuilder response = new StringBuilder();
            int responseCode = connection.getResponseCode();
            
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(responseCode >= 400 ? 
                            connection.getErrorStream() : connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
    
    // Simple JSON parsing method, replacing org.json library
    private static Map<String, String> parseSimpleJson(String jsonString) {
        Map<String, String> result = new HashMap<>();
        
        // Simple JSON parsing, only supports first-level object key-value pairs
        if (jsonString == null || jsonString.isEmpty()) {
            return result;
        }
        
        // Remove surrounding curly braces
        jsonString = jsonString.trim();
        if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            jsonString = jsonString.substring(1, jsonString.length() - 1);
        }
        
        // Split key-value pairs
        String[] pairs = jsonString.split(",");
        for (String pair : pairs) {
            pair = pair.trim();
            int colonIndex = pair.indexOf(":");
            if (colonIndex > 0 && colonIndex < pair.length() - 1) {
                String key = pair.substring(0, colonIndex).trim();
                String value = pair.substring(colonIndex + 1).trim();
                
                // Remove quotes
            if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                    // Simple escape character handling
                value = value.replace("\\\"", "\"")
                               .replace("\\\\", "\\")
                               .replace("\\n", "\n")
                               .replace("\\r", "\r")
                               .replace("\\t", "\t");
                }
                
                result.put(key, value);
            }
        }
        
        return result;
    }

    /**
     * Synchronously call AWS API to decrypt data
     * @param ksn Key Serial Number
     * @param cipherText Encrypted text
     * @return HashMap of decryption results
     * @throws IOException If network request fails
     */
    public static HashMap<String, String> decrytDataWithAWS(String ksn, String cipherText) throws IOException {
        HashMap<String, String> result = new HashMap<>();
//        String keyIdentifier = "arn:aws:payment-cryptography:us-east-1:750226982526:key/rmzbbqjxd7euatrh";

        String keyIdentifier = DUKPT_ALIAS;

        // Build simple fields
        Map<String, String> simpleFields = new HashMap<>();
        simpleFields.put("keyIdentifier", keyIdentifier);
        simpleFields.put("ciphertext", cipherText);
        
        // Build nested objects
        Map<String, String> dukptDataMap = new HashMap<>();
        dukptDataMap.put("ksn", ksn);
        dukptDataMap.put("mode", "CBC");
        dukptDataMap.put("keyDerivationType", "TDES_2KEY");
        
        Map<String, Map<String, String>> nestedObjects = new HashMap<>();
        nestedObjects.put("dukptData", dukptDataMap);
        
        // Build JSON request body
        String jsonBody = buildJsonString(simpleFields, nestedObjects);

        try {
            // Send HTTP request
            String responseText = sendPostRequest(FUNCTION_URL, jsonBody);
            
            // Parse response results
            result.putAll(parseSimpleJson(responseText));
            System.out.println("Decryption result:\n" + responseText);
        } catch (IOException e) {
            result.put("result", "request failed:" + e.getMessage());
            System.out.println("Request failed:\n" + e.getMessage());
            throw e; 
        }
        
        return result; 
    }

    /**
     * Synchronously call AWS API to get TR31 block data
     * @param ksn Key Serial Number
     * @return HashMap of TR31 block data
     * @throws IOException If network request fails
     */
    public static HashMap<String, String> getTR31BlockFromAWS(String ksn) throws IOException {
        HashMap<String, String> result = new HashMap<>();

        String exportKeyIdentifier = AES_DUKPT_ALIAS; //可以alias或者KRN
//        String exportKeyIdentifier = "arn:aws:payment-cryptography:us-east-1:750226982526:key/77dfmhmgw6kxgw4a";
        String wrappingKeyIdentifier = AES_KEK_ALIAS;
//        String wrappingKeyIdentifier = "arn:aws:payment-cryptography:us-east-1:750226982526:key/k5luyc2r3h6p53ii";

        Map<String, String> simpleFields = new HashMap<>();
        simpleFields.put("exportKeyIdentifier", exportKeyIdentifier);
        simpleFields.put("wrappingKeyIdentifier", wrappingKeyIdentifier);
        simpleFields.put("keySerialNumber", ksn);
        
        String jsonBody = buildJsonString(simpleFields, null);

        try {
            // Send HTTP request
            String responseText = sendPostRequest(EXPORT_FUNCTION_URL, jsonBody);
            
            // Parse response results
            result.putAll(parseSimpleJson(responseText));
            System.out.println("Export result:\n" + responseText);
        } catch (IOException e) {
            result.put("result", "request failed:" + e.getMessage());
            System.out.println("Request failed:\n" + e.getMessage());
            throw e; 
        }
        
        return result; 
    }


    // Keep original method signature, but use custom parsing method internally
    public static HashMap<String, String> parseJsonToHashtable(String jsonResponse) {
        Map<String, String> simpleJsonMap = parseSimpleJson(jsonResponse);
        return new HashMap<>(simpleJsonMap);
    }

    public static void main(String[] args)  {
        try {
            
            String cipherText = "BF54BB023B9A29A07EE2F8C6FC7B57AFFE66D7F861A7D2F9518BFD5C4A4AD64B816B176639E59E6CF431E9C605D05E7806580682F5B75698CE49DFC5671A384176A9A13063EDC0A0839897ABA9DC81E229A9C1FDCD54D5457BD4E9D8E4C9147207F9929A033FC26E32E79DEDD07935687A03ECA31815566101BB65418BE1BB5741AA04766CF323B11A4AD7C6F43397298A51A386787361658A9E7E4C7C8A771D189D23432C7AAF4ACCFD505EF0631BC7DB77D49002F81B4A11DFAD67A68D04A785ECA625391940AD83FC0C4F1B64FFB7AB92FF7638753413C66BC36E47A9AF014110C16C8FB9A040F66C435D72A8761662310DC88E12A023F196A641C7CE78B7E75250B049E42F9124D643FFB3DB8F23C1240E320F1AD9EC131C873E1E85E67B813A4231B8BB714AE030B94F6D27F365566876552D637CE8916EEA9DE3477C447C26EC72D51AB625017DBECA92C324C5C68A23B0EF1D651D93DD6D0006268A39FE33F66121AB36A493BE02E87A4D6AEBD275FD3D0BA4AB716E1B0EC02577769E2455B86519DE3DFEAE41EC5523C284A02586CEEDD2C0215ACB17C6BC89E02CA1";
            String KSN = "01202408070118E00008";
            
            // Synchronously call decryption method and get results
            System.out.println("Calling decryption method...");
            HashMap<String, String> decryptResult = decrytDataWithAWS(KSN, cipherText);
            System.out.println("Decryption method return result: " + decryptResult);

            String keySerialNumber = "00E111111112222200000000";
            
            // Synchronously call TR31 block method and get results
            System.out.println("Calling TR31 block method...");
            HashMap<String, String> tr31Result = getTR31BlockFromAWS(keySerialNumber);
            System.out.println("TR31 block method return result: " + tr31Result);
            
            System.out.println("Test completed!");
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
