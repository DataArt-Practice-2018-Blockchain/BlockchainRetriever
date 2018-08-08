package retriever.apicaller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RPCCaller {

    @Value("${node.address}")
    private String nodeAddress;

    @Value("${node.port}")
    private int nodePort;

    private RestTemplate template;
    private HttpHeaders headers;

    private RPCCaller() {
        template = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String url() {
        return "http://" + nodeAddress + ":" + nodePort;
    }

    public int getLastBlockIndex() {
        String lastBlockQuery =
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_blockNumber\"," +
                        "\"params\":[],\"id\":1}";
        String queryResult = sendQueryForResult(lastBlockQuery);

        String hexValue = new JSONObject(queryResult).getString("result");

        return Integer.parseInt(hexValue.substring(2), 16);
    }

    public String getBlock(int blockNumber) {
        String blockQuery =
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBlockByNumber\"," +
                        "\"params\":[\"0x%h\", true],\"id\":1}";
        String result = sendQueryForResult(String.format(blockQuery, blockNumber));

        JSONObject object = new JSONObject(result);

        if (object.get("result") instanceof  JSONObject)
            return object.getJSONObject("result").toString();
        else {
            System.out.println("Null on block " + blockNumber);
            return null;
        }
    }

    private String sendQueryForResult(String query) {
        HttpEntity<String> entity = new HttpEntity<>(query, headers);
        return template.postForObject(url(), entity, String.class);
    }
}