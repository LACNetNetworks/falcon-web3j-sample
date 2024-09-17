// SPDX-License-Identifier: Apache-2.0
pragma solidity 0.7.4;

import "./BaseRelayRecipient.sol";

/**
 * @title Storage
 * @dev Store & retreive value in a variable
 */
contract Storage is BaseRelayRecipient{

    uint256 number;
    address owner;
  
    constructor() public {
        owner = _msgSender();
    }

    function verify(
        bytes calldata signature,//firma postquant
        bytes calldata publicKey,// llave publica falcon
        bytes calldata message,// message Plano
        address falconVerifier // address falcon compilado 
    ) public returns (bool isValid) {
        (bool success, bytes memory verifies) = address(falconVerifier).call(
            abi.encodeWithSignature(
                "verify(bytes,bytes,bytes)",
                signature,
                publicKey,
                message
            )
        );
       // if (success && verifies.length == 32)
       //     revert("Invalid signature Reason");
        require(success && verifies[31] == 0, "Invalid signature");

        return verifies[31] == 0;
    }
    
    /**
     * @dev Store value in variable
     * @param num value to store
     */
    function store(
         bytes calldata signature,//firma postquant
        bytes calldata publicKey,// llave publica falcon
        bytes calldata message,// message Plano
        address falconVerifier, // address falcon compilado 
        uint256 num // valor del message)
    ) public {

        (bool success, bytes memory verifies) = address(falconVerifier).call(
            abi.encodeWithSignature(
                "verify(bytes,bytes,bytes)",
                signature,
                publicKey,
                message
            )
        );
       
        require(success && verifies[31] == 0, "Invalid signature");

        number = num;

        emit ValueSeted(_msgSender(),num);
    }

    /**
     * @dev Return value 
     * @return value of 'number'
     */
    function retreive() public view returns (uint256){
        return number;
    }

    function getOwner() public view returns (address){
        return owner;
    }

    event ValueSeted(address sender, uint256 value);

}