package retriever.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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

/**
 * Class for working with MongoDB database and data flow.
 */
@Component
public class DBHelper {

    @Autowired
    private RPCCaller rpcCaller;

    @Produce
    private ProducerTemplate producerTemplate;

    /**
     * Gets block from database (or copies it from node) and sends it to parsing route.
     * @param blockNumber block number
     */
    public void parseBlockToDB(int blockNumber) {
        String block = getBlockFromDB(blockNumber);
        if (block != null) {
            producerTemplate.sendBody("direct:parseBlock", block);
        } else {
            copyBlockToDB(blockNumber);
            block = getBlockFromDB(blockNumber);
            if (block != null) {
                producerTemplate.sendBody("direct:parseBlock", block);
            } else {
                System.out.println("COULD NOT FIND BLOCK " + blockNumber);
            }
        }

    }

    /**
     * Gets block from database.
     * @param blockNumber block number
     * @return block as JSON object or null if the block was not found
     */
    public String getBlockFromDB(int blockNumber) {
        DBObject query = new BasicDBObject(
                "number",
                String.format("0x%h", blockNumber)
        );

        DBObject response = (DBObject) producerTemplate.requestBody(
                "direct:dbFindOneByQuery",
                query
        );
        if (response == null)
            return null;
        return response.toString();
    }

    /**
     * Returns the number of top block in blockchain.
     * @return the number of top block in blockchain
     */
    public int getLastBlockIndex() {
        return rpcCaller.getLastBlockIndex();
    }

    public void copyBlockToDB(int blockNumber) {
        String block = rpcCaller.getBlock(blockNumber);
        if (block != null) {
            producerTemplate.sendBody("direct:dbInsertBlock", block);
        }
    }

    /**
     * Gets method name of contract by signature hash.
     * @param address contract address
     * @param methodKey method's signature hash
     * @return method name
     */
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

    /**
     * Adds contract with ABI to database.
     * @param address contract address
     * @param abi contract ABI
     */
    public void addContractToDB(String address, String abi) {
        Map<String, String> methods = Decoder.getContractFunctionNamesByHash(abi);

        JSONObject json = new JSONObject();
        json.put("address", address);
        methods.forEach(json::put);

        producerTemplate.sendBody("direct:dbInsertContract", json.toString());
    }
}
