package decoder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import org.web3j.crypto.Hash;

public class Decoder {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String getInputMethodData(String transaction) {
        String input =  getInputData(transaction);
        if (    input.length() < 10
            || !input.substring(0,2).equals("0x"))
            return null;
        return input.substring(2, 10);
    }

    private static String getInputData(String transaction) {
        JSONObject obj = new JSONObject(transaction);
        return obj
                .getString("input");
    }

    public static String getFunctionName(String abi, String transaction) {
        Map<String, String> functionNamesByHash = getContractFunctionNamesByHash(abi);

        String inputData = getInputMethodData(transaction);
        //System.out.println(inputData.substring(2, 10));
        return functionNamesByHash.get(inputData);
    }

    public static Map<String, String> getContractFunctionNamesByHash(String abi) {
        Map<String, String> result = new HashMap<>();
        JSONArray obj = new JSONArray(abi);

        for (int i = 0; i < obj.length(); i++) {
            JSONObject item = obj.getJSONObject(i);
            if (item.getString("type").equals("function")) { //or there's no type key??
                String hash = getFunctionHash(item);
               // System.out.println(hash + " : " + item.getString("name"));
                result.put(hash, item.getString("name"));
            }
        }
        return result;
    }

    private static String getFunctionHash(JSONObject item) {
        String name = item.getString("name");
        StringJoiner joiner = new StringJoiner(",");

        JSONArray inputs = item.getJSONArray("inputs");
        for (int i = 0; i < inputs.length(); i++) {
            JSONObject input = inputs.getJSONObject(i);
            joiner.add(input.getString("type"));
        }

        return signatureHash(name + "(" + joiner.toString() + ")");
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static String signatureHash(String s) {
        String hash = bytesToHex(Hash.sha3(s.getBytes(StandardCharsets.UTF_8)));
        return hash.substring(0, 8);
    }

    /*public static void main(String[] args) throws IOException {
        String abi = Files.lines(Paths.get("abi.json")).findFirst().orElse("");
        String transaction = Files.lines(Paths.get("transaction.json")).findFirst().orElse("");
        System.out.println(getFunctionName(abi, transaction));
    }
    */
}