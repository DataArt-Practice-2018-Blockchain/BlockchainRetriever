package retriever.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import decoder.Decoder;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retriever.apicaller.RPCCaller;

import java.util.ArrayList;
import java.util.Map;

@Component
public class DBHelper {

    @Autowired
    private RPCCaller rpcCaller;

    @Produce
    private ProducerTemplate producerTemplate;

    public void parseBlockToDB(int blockNumber) {
        String block = getBlockFromDB(blockNumber);
        if (block != null) {
            producerTemplate.sendBody("direct:parseBlock", block);
        }

    }

    public String getBlockFromDB(int blockNumber) {
        DBObject query = new BasicDBObject(
                "number",
                String.format("0x%h", blockNumber)
        );

        DBObject response = (DBObject) producerTemplate.requestBody(
                "direct:dbFindOneByQuery",
                query
        );
        return response.toString();
    }

    public int getLastBlockIndex() {
        return rpcCaller.getLastBlockIndex();
    }

    public void copyBlockToDB(int blockNumber) {
        String block = rpcCaller.getBlock(blockNumber);
        if (block != null) {
            producerTemplate.sendBody("direct:dbInsertBlock", block);
        }
    }

    public int getMinBlockIndex() { //[ { $group: { _id: {}, minBlockNumber: { $max:"$decNumber"}}}]
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

    public String findMethodInContract(String address, String methodKey) {
        String queryTemplate = "{\"address\":\"%s\", \"%s\": {$exists : true}}";
        DBObject query = BasicDBObject.parse(
                String.format(queryTemplate, address, methodKey));

        DBObject response = (DBObject) producerTemplate.requestBody(
                "direct:dbContractFindOneByQuery",
                query
        );

        if (response == null)
            return null;

        JSONObject json = new JSONObject(response.toString());
        try {
            return json.getString(methodKey);
        } catch (JSONException e) {
            return null;
        }
    }

    public void addContractToDB(String address, String abi) {
        Map<String, String> methods = Decoder.getContractFunctionNamesByHash(abi);

        JSONObject json = new JSONObject();
        json.put("address", address);
        methods.forEach(json::put);

        producerTemplate.sendBody("direct:dbInsertContract", json.toString());
    }
}
