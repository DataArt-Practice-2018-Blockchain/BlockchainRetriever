package retriever.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlockchainRoute extends RouteBuilder {
    @Value("${node.address}")
    private String nodeAddress;

    @Value("${node.port}")
    private int nodePort;

    @Override
    public void configure() {
        from("web3j://http://35.228.59.11:8545?operation=BLOCK_OBSERVABLE&fullTransactionObjects=true")
                .marshal().json(JsonLibrary.Gson)
                .convertBodyTo(String.class)
                .to("stream:out")
                .to("mongodb:mongo?database=blockchain&collection=blocks&operation=insert");
    }
}
