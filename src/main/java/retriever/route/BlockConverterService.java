package retriever.route;

import org.apache.camel.Body;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class BlockConverterService {
    public String convertBlockNumberToDec(@Body String input) {
        JSONObject object = new JSONObject(input);
        String hexNumber = object.getString("number");
        long decNumber = Long.parseLong(hexNumber.substring(2), 16);
        return object.put("decNumber", decNumber).toString();
    }
}
