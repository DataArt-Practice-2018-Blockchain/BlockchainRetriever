package retriever.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Component for parsing Ethereum blocks and producing transactions.
 */
@Component
public class TransactionParser implements Processor {

    /**
     * Processes a block and sends transactions to transaction routes.
     * @param exchange route exchange
     */
    @Override
    public void process(Exchange exchange) {
        String block = (String)exchange.getIn().getBody();
        JSONObject blockJSON = new JSONObject(block);

        long timestamp = Long.parseLong(blockJSON.getString("timestamp").substring(2), 16);

        String hexTimestamp = blockJSON.getString("timestamp");
        JSONArray transactions = blockJSON.getJSONArray("transactions");

        ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();

        transactions.forEach(transaction ->
            producerTemplate.sendBody("direct:dbInsertTransaction",
                    ((JSONObject)transaction)
                            .put("decTimestamp", timestamp)
                            .put("timestamp", hexTimestamp)
                            .toString())
        );
    }
}
