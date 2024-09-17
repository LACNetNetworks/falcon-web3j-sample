const { ethers } = require("ethers");
const { LacchainProvider, LacchainSigner } = require('@lacchain/gas-model-provider');
const contractAbi = require("../artifacts/contracts/Storage.sol/Storage.json");

async function main() {

  //LOCAL
  const yourRPCNode = "http://34.136.8.0";
  const nodeAddress = "0xb2e5ecebeb8e8617637d6364d9c6f6ee7508ac42";

  //TESTNET
  //const yourRPCNode = "http://{{YOUR_NODE_IP}}";
  //const nodeAddress = "{{YOUR_NODE_ADDRESS}}";

  //MAINNET
  //const yourRPCNode = "http://{{YOUR_NODE_IP}}";
  //const nodeAddress = "{{YOUR_NODE_ADDRESS}}";

  //Contract Owner
  const privateKey = "0xa0a2af404337c096113bc2c180df7a6636a88f8eb5da6160817f9315aaafee80";

  console.log("Starting deploy of contract: ", contractAbi.contractName);

  const provider = new LacchainProvider(yourRPCNode);
  const signer = new LacchainSigner(
    privateKey,
    provider,
    nodeAddress,
    Date.now()
  );


  console.log(`Create Factory ${contractAbi.contractName}...`);

  const contractFactory = new ethers.ContractFactory(
    contractAbi.abi,
    contractAbi.bytecode,
    signer
  );

  console.log(`Deploying ${contractAbi.contractName}...`);

  const contract = await contractFactory.deploy();
  const receipt = await contract.deploymentTransaction()?.wait();
  console.log("Contract deployed!");
  const contractAddress = receipt?.contractAddress;
  console.log(`${contractAbi.contractName} Contract Address: `, contractAddress );

}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});