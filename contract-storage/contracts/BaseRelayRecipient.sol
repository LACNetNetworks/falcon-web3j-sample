// SPDX-License-Identifier: Apache-2.0

pragma solidity 0.7.4;

/**
 * A base contract to be inherited by any contract that want to receive relayed transactions
 * A subclass must use "_msgSender()" instead of "msg.sender"
 */
abstract contract BaseRelayRecipient{

    /*
     * Forwarder singleton we accept calls from
     */
    
    address internal trustedForwarder = 0x0d0AfaB02AFD0C37c60Cd8a930f16184fD212874; //dev
   // address internal trustedForwarder = 0xa4B5eE2906090ce2cDbf5dfff944db26f397037D; //Open-Protest
    //address internal trustedForwarder = 0xEAA5420AF59305c5ecacCB38fcDe70198001d147; //Main Net
    /**
     * return the sender of this call.
     * if the call came through our Relay Hub, return the original sender.
     * should be used in the contract anywhere instead of msg.sender
     */
    function _msgSender() internal virtual returns (address sender) {
        bytes memory bytesRelayHub;
        (,bytesRelayHub) = trustedForwarder.call(abi.encodeWithSignature("getRelayHub()"));

        if (msg.sender == abi.decode(bytesRelayHub, (address))){ //sender is RelayHub then return origin sender
            bytes memory bytesSender;
            (,bytesSender) = trustedForwarder.call(abi.encodeWithSignature("getMsgSender()"));
        
            return abi.decode(bytesSender, (address));
        } else { //sender is not RelayHub, so it is another smart contract 
            return msg.sender;
        }
    }
}