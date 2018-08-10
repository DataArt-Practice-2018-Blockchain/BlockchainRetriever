package decoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.crypto.Hash;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Class with methods for decoding input data in Ethereum transactions.
 */
public class Decoder {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Returns first 4 bytes of transaction input that correspond to method call.
     * Returns null if transaction has empty or incorrect input.
     * @param transaction entire transaction JSON string
     * @return hex string with method call data
     */
    public static String getInputMethodData(String transaction) {
        String input =  getInputData(transaction);
        if (    input.length() < 10
            || !input.substring(0,2).equals("0x"))
            return null;
        return input.substring(2, 10);
    }

    /**
     * Gets the transaction's called method from transaction and ABI.
     * @param abi contract ABI as JSON string
     * @param transaction transaction as JSON string
     * @return name of called method, null if method was not found
     */
    public static String getFunctionName(String abi, String transaction) {
        Map<String, String> functionNamesByHash = getContractFunctionNamesByHash(abi);

        String inputData = getInputMethodData(transaction);
        return functionNamesByHash.get(inputData);
    }

    /**
     * Converts contract ABI to mapping of method signature hash to method name.
     * @param abi contract ABI as JSON string
     * @return ABI mapping
     */
    public static Map<String, String> getContractFunctionNamesByHash(String abi) {
        Map<String, String> result = new HashMap<>();
        JSONArray obj = new JSONArray(abi);

        for (int i = 0; i < obj.length(); i++) {
            JSONObject item = obj.getJSONObject(i);
            if (item.getString("type").equals("function")) { //or there's no type key??
                String hash = getFunctionHash(item);
                result.put(hash, item.getString("name"));
            }
        }
        return result;
    }

    /**
     * Returns method's signature hash
     * @param item method from ABI as JSONObject
     * @return signature hash as hex string
     */
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

    /**
     * Gets input data from transaction.
     * @param transaction transaction as JSON string.
     * @return input data
     */
    private static String getInputData(String transaction) {
        return new JSONObject(transaction).getString("input");
    }

    /**
     * Converts byte array to hex string.
     * @param bytes byte array
     * @return hex string
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Converts method signature to its hash.
     * @param s method signature in form of string
     * @return hash as hex string
     */
    private static String signatureHash(String s) {
        String hash = bytesToHex(Hash.sha3(s.getBytes(StandardCharsets.UTF_8)));
        return hash.substring(0, 8);
    }
}