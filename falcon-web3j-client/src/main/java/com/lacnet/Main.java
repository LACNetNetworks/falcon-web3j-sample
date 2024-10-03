package com.lacnet;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.falcon.*;
import org.bouncycastle.util.Strings;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        try {
            // Connect to the private network
            Web3j web3 = Web3j.build(new HttpService("http://34.136.8.0"));

            // Load credentials from the private key
            Credentials credentials = Credentials.create("a0a2af404337c096113bc2c180df7a6636a88f8eb5da6160817f9315aaafee80");

            // Contract address to interact with
            String contractAddress = "0xFEf7c78793dF5aC704C023C6bC19d672cF8dED75";

            // Transaction data
            BigInteger nonce = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameter.valueOf("pending"))
                    .send().getTransactionCount();

            BigInteger gasPrice = BigInteger.ZERO;  // Gas price is 0 in the private network
            BigInteger gasLimit = BigInteger.valueOf(500000); // Gas limit
            //BigInteger chainId = BigInteger.valueOf(648560); // ChainId of the private network
            BigInteger msgValue= BigInteger.valueOf(100);
            Uint256     valueStorage = new Uint256(msgValue);
            //FLACON POST-QUANTUM//////////////////////////////////////////////////////////////////////////////////////
            String falconVerifier = "0x0000000000000000000000000000000000000065";
            byte[] msg = Strings.toByteArray(msgValue.toString());
            byte[] zero = Strings.toByteArray("0");

            // Generating Falcon keys
            FalconKeyPairGenerator keyGen = new FalconKeyPairGenerator();
            SecureRandom random = new SecureRandom();
            keyGen.init(new FalconKeyGenerationParameters(random, FalconParameters.falcon_512));
            AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();

            // Sign message with Falcon
            FalconPrivateKeyParameters skparam = (FalconPrivateKeyParameters)keyPair.getPrivate();
            ParametersWithRandom skwrand = new ParametersWithRandom(skparam, random);
            FalconSigner signer = new FalconSigner();
            signer.init(true, skwrand);

            byte[] sigGenerated = signer.generateSignature(msg);

            // Public key
            ByteArrayOutputStream pub = new ByteArrayOutputStream( );
            pub.write(zero);
            pub.write(skparam.getPublicKey());

            System.out.println("sigGenerated :" + org.bouncycastle.util.encoders.Hex.toHexString( sigGenerated));
            System.out.println("public key :" +  org.bouncycastle.util.encoders.Hex.toHexString(  pub.toByteArray()  ));
            System.out.println("msg  " + org.bouncycastle.util.encoders.Hex.toHexString(msg));

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Parameters

           // byte[] msgFake = Strings.toByteArray("fake");

            // Encode the method and parameters to send to the contract
            Function function = new Function(
                    "store",
                    Arrays.asList(
                            new DynamicBytes(sigGenerated),
                            new DynamicBytes(pub.toByteArray()),
                            new DynamicBytes(msg),
                            new Address(falconVerifier),
                            valueStorage), // The value to store
                    Collections.emptyList()
            );
            // Encode the function using FunctionEncoder
            String encodedFunction = FunctionEncoder.encode(function);

            String nodeAddress = "0xb2e5ecebeb8e8617637d6364d9c6f6ee7508ac42";
            BigInteger expiration = BigInteger.valueOf(1898156266);

            // Create the corresponding types
            Address address = new Address(nodeAddress);
            Uint256 uint256 = new Uint256(expiration);

            // Encode the parameters
            String encodedParameters = FunctionEncoder.encodeConstructor(List.of(address, uint256));

            String encodedData = encodedFunction + encodedParameters;

            // Create the transaction
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce, gasPrice, gasLimit, contractAddress, encodedData);

            // Sign the transaction (without using chainId)
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);

            // If you want to sign with chainId (EIP-155), use this:
            //byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId.longValue(), credentials);

            // Convert the signed message to hexadecimal
            String hexValue = Numeric.toHexString(signedMessage);

            // Send the transaction
            EthSendTransaction response = web3.ethSendRawTransaction(hexValue).send();
            if (response.hasError()) {
                // Capture the error if the transaction fails
                String errorMessage = response.getError().getMessage();
                System.out.println("Transaction failed with error: " + errorMessage);
            } else {
                String transactionHash = response.getTransactionHash();
                System.out.println("Transaction hash: " + transactionHash);

                Thread.sleep(5000);
                // Transaction transactionTX = web3.ethGetTransactionByHash(transactionHash).send().getTransaction().get();
                //System.out.println("Transaction block: " + transactionTX.getBlockHash());

                EthGetTransactionReceipt receipt = web3.ethGetTransactionReceipt(transactionHash).send();
                if (receipt.getTransactionReceipt().isPresent()) {
                    if (receipt.getTransactionReceipt().get().isStatusOK()) {
                        System.out.println("Transaction succeeded!");
                    } else {
                        System.out.println("Transaction failed.");
                        System.out.println("Error message: " + decodeRevertReason( receipt.getTransactionReceipt().get().getRevertReason()).substring(4,22) );
                        // Here you could check the event logs for additional information about the failure
                    }
                }
            }
        }  catch (InterruptedException e) {

            e.printStackTrace();
        } catch (Exception e) {
            System.out.println( e.getMessage());

        }
    }

    public static String decodeRevertReason(String encodedString) {
        // The ABI encoded message
        String messageHex = encodedString.substring(128); // Skip the first 128 characters (64 bytes)

        // Convert from hexadecimal to bytes
        byte[] messageBytes = Numeric.hexStringToByteArray(messageHex);

        // Convert the bytes to a UTF-8 string
        return new String(messageBytes);
    }
}