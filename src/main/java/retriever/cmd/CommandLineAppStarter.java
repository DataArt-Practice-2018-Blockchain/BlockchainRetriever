package retriever.cmd;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import retriever.rpc.RPCCaller;
import java.util.ArrayList;

@Component
public class CommandLineAppStarter implements CommandLineRunner {
    @Autowired
    private RPCCaller rpcCaller;

    @Produce
    private ProducerTemplate producerTemplate;

    private static final int LOG_FREQUENCY = 1000;

    @Override
    public void run(String... args) throws Exception {
        int lastBlockIndex = rpcCaller.getLastBlockIndex();

        //int minBlockIndex = getMinBlockIndex();
        //System.out.println("Min found block is " + minBlockIndex + ", last block is " + lastBlockIndex);

        int startBlockIndex = Integer.parseInt(args[0]);

        System.out.println("Starting copying from " + startBlockIndex + " to " + lastBlockIndex);

        for (int i = 292000; i <= 293000; i++) {
            copyBlockToDB(i);
            if (i % LOG_FREQUENCY == 0)
                System.out.println("Copied " + i + " blocks");
        }

    }

    private void copyBlockToDB(int blockNumber) {
        String block = rpcCaller.getBlock(blockNumber);
        if (block != null)
            producerTemplate.sendBody("direct:dbInsert", block);
    }

    private int getMinBlockIndex() { //[ { $group: { _id: {}, minBlockNumber: { $max:"$decNumber"}}}]
        DBObject aggregation =
            new BasicDBObject("$group",
                new BasicDBObject("_id", 1)
                    .append("minBlockNumber",
                            new BasicDBObject("$min", "$decNumber")
                    )
            );

        ArrayList<DBObject> result = (ArrayList<DBObject>) producerTemplate.requestBody("direct:query", aggregation);

        if (result.size() == 0)
            return 0;

        return (int) result.get(0).get("minBlockNumber");
    }
}
