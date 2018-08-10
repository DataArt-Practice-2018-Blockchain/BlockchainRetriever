package retriever.route;

import org.apache.camel.Body;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Component for block converting operations.
 */
@Component
public class BlockConverterService {

    /**
     * Converts block number from hex string to decimal number (for Mongo indexing)
     * @param input block
     * @return transformed block
     */
    public String convertBlockNumberToDec(@Body String input) {
        JSONObject object = new JSONObject(input);
        String hexNumber = object.getString("number");
        long decNumber = Long.parseLong(hexNumber.substring(2), 16);
        return object.put("decNumber", decNumber).toString();
    }

    /**
     * Converts block timestamp from hex string to decimal number (for Mongo indexing)
     * @param input block
     * @return transformed block
     */
    public String convertTimestampToDec(@Body String input) {
        JSONObject object = new JSONObject(input);
        String hexNumber = object.getString("timestamp");
        long decNumber = Long.parseLong(hexNumber.substring(2), 16);
        return object.put("decTimestamp", decNumber).toString();
    }
}
