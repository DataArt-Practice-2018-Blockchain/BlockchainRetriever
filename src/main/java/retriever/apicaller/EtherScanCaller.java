package retriever.apicaller;

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
import java.util.HashSet;
import java.util.Set;

@Component
public class EtherScanCaller {
    private static final int MAX_CACHE_SIZE = 1500000;
    private final String url = "https://api-rinkeby.etherscan.io/api?module=contract&action=getabi" +
            "&address=%s" +
            "&apikey=%s";

    private RestTemplate template;
    private HttpHeaders headers;
    private String apiKey;

    private Set<String> notVerified;

    private EtherScanCaller() throws IOException {
        template = new RestTemplate();

        headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        apiKey = Files.lines(Paths.get("apikey")).findFirst().orElse("");

        notVerified = new HashSet<>();
    }

    public String getABI(String address) {
        if (notVerified.contains(address))
            return null;

        HttpEntity<?> entity = new HttpEntity<>(headers);
        HttpEntity<String> response = template.exchange(
                String.format(url, address, apiKey),
                HttpMethod.GET,
                entity,
                String.class
        );

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            JSONObject result = new JSONObject(response.getBody());
            String abi = result.getString("result");
            if (abi.equals("Contract source code not verified")) {
                notVerified.add(address);
                if (notVerified.size() > MAX_CACHE_SIZE)
                    notVerified.clear();
                return null;
            }
            return abi;
        } catch (NullPointerException | JSONException e) {
            return null;
        }
    }
}
