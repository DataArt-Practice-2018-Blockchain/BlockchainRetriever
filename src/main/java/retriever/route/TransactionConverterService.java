package retriever.route;

import decoder.Decoder;
import org.apache.camel.Body;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import retriever.apicaller.EtherScanCaller;
import retriever.db.DBHelper;

/**
 * Component for transaction converting operations.
 */
@Component
public class TransactionConverterService {

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private EtherScanCaller etherScanCaller;

    /**
     * Adds method name to transaction document.
     * @param transaction transaction
     * @return transformed transaction
     */
    public String addMethodName(@Body String transaction) {
        JSONObject object = new JSONObject(transaction);

        if (object.isNull("to"))
            return transaction;

        String address = object.getString("to");
        String inputMethodData = Decoder.getInputMethodData(transaction);

        if (inputMethodData == null)
            return transaction;

        String methodName = dbHelper.findMethodInContract(address, inputMethodData);

        if (methodName == null) {
            try {
                String abi = etherScanCaller.getABI(address);
                if (abi == null) {
                    return transaction;
                }
                dbHelper.addContractToDB(address, abi);
                methodName = Decoder.getFunctionName(abi, transaction);
            } catch (HttpClientErrorException e) {
                System.out.println("API ERROR:\n" + e.getLocalizedMessage());
                System.out.println("TRANSACTION:\n" + transaction);
                return transaction;
            }
        }

        if (methodName == null)
            return transaction;

        object.put("methodName", methodName);
        object.put("method", methodName + "@" + address);
        return object.toString();
    }
}

