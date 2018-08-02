package retriever.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class BlockProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String block = (String)exchange.getIn().getBody();
        JSONArray transactions = new JSONObject(block).getJSONArray("transactions");

        ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();

        transactions.forEach(transaction ->
                producerTemplate.sendBody("direct:dbInsertTransaction", transaction.toString()));
    }
}
