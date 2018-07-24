package retriever.cmd;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CommandLineAppStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        RestTemplate template = new RestTemplate();
        String url = "http://35.228.59.11:8545";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String queryTemplate = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_blockNumber\"," +
                "\"params\":[],\"id\":1}";
        HttpEntity<String> entity = new HttpEntity<>(queryTemplate, headers);
        String result = template.postForObject(url, entity, String.class);

        System.out.println(result);
    }
}
