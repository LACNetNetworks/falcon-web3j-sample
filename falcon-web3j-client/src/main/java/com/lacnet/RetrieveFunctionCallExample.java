package com.lacnet;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class RetrieveFunctionCallExample {

    private static final String CONTRACT_ADDRESS = "0xFEf7c78793dF5aC704C023C6bC19d672cF8dED75";
    private static final String RPC_URL = "http://34.136.8.0";
    private static final String FROM_ADDRESS = "0x2d48B0E5468d690FB77454552B118584d09690F5"; // Any valid address.

    public static void main(String[] args) throws Exception {
        // Connect to the Web3j client
        Web3j web3j = Web3j.build(new HttpService(RPC_URL));
        // Retrieve
        // Generate the payload (ABI of the function you want to call)
        String encodedFunction = FunctionEncoder.encode(new Function(
                "retreive",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {})));

        // Create an "eth_call" transaction to the contract function
        Transaction callTransaction = Transaction.createEthCallTransaction(FROM_ADDRESS, CONTRACT_ADDRESS, encodedFunction);

        // Execute the "eth_call"
        EthCall response = web3j.ethCall(callTransaction, DefaultBlockParameterName.LATEST).send();

        // Check if there was an error in the response
        if (response.hasError()) {
            System.out.println("Error in call: " + response.getError().getMessage());
            return;
        }

        // Display the raw response value
        String rawResponse = response.getValue();
        System.out.println("Raw contract response: " + rawResponse);

        // Decode the response
        List<Type> result = FunctionReturnDecoder.decode(rawResponse,
                org.web3j.abi.Utils.convert(Collections.singletonList(new TypeReference<Uint256>() {})));

        // Display the result
        if (!result.isEmpty()) {
            BigInteger retrievedValue = (BigInteger) result.get(0).getValue();
            System.out.println("Result of retrieve(): " + retrievedValue);
        } else {
            System.out.println("Could not retrieve the value.");
        }
    }

}