package retriever.apicaller;

import decoder.Decoder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class EtherScanCaller {
    private final String url = "https://api-rinkeby.etherscan.io/api?module=contract&action=getabi" +
            "&address=%s" +
            "&apikey=%s";

    private RestTemplate template;
    private HttpHeaders headers;
    private String apiKey;

    private EtherScanCaller() throws IOException {
        template = new RestTemplate();

        headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        apiKey = Files.lines(Paths.get("apikey")).findFirst().orElse("");
    }

    public String getABI(String address) {
        HttpEntity<?> entity = new HttpEntity<>(headers);

        HttpEntity<String> response = template.exchange(
                String.format(url, address, apiKey),
                HttpMethod.GET,
                entity,
                String.class
        );

        try {
            JSONObject result = new JSONObject(response.getBody());
            return result.getString("result");
        } catch (NullPointerException | JSONException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            String abi = new EtherScanCaller().getABI("0xE43a244d802Fc5EB74BD877701db8fC3904C854D");

            /*System.out.println(abi);
            JSONArray arr = new JSONArray(abi);
            for (Object object : arr)
                System.out.println(object.toString());
            */

            Map<String, String> methods = Decoder.getContractFunctionNamesByHash(abi);
            methods.forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
