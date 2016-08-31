# Ethereum Android Samples
This repo contains samples how to build applications using the blockchain via Ethereum Android.

## Account Balance Sample

This sample shows how to use the low level RPC commands to interact with an Ethereum node


The general approach is always the following:

* create a ```WrappedRequest``` with the desired RPC command and the corresponding parameters (see https://github.com/ethereum/wiki/wiki/JSON-RPC)
* either use the synchronous ```send``` or ```sendAsync``` method to retrieve a ```WrappedResponse```
* check ```WrappedResponse.isSuccess()``` and use ```WrappedResponse.getErrorMessage()``` in case it failed
* if it succeeded ```WrappedResponse.getResponse()``` contains the response of the RPC call

## Simple Storage Sample

This sample guides you through all steps to interact with a smart contract.

### Compile the smart contract code

To interact with an existing contract you will need to have the ABI, if you want to deploy a new contract you also need its bytecode.
You can use any compiler you want to achieve this. For the sake of simplicity we misuse Junit test functionality.

Check ```SimpleOwnedStorageTest.compile()``` which print both bytecode and ABI to ```System.out```. These are used as constants in ```SimpleStorageActivity```.

### Deploy the smart contract

Deploying a smart contract involves 2 steps:

#### Create the transaction


    ethereumAndroid.contracts().create(String contractBytecode, String contractAbi, Object... constructorParams)

This will create a hex encoded unsigned transaction String  which already includes the right nonce for the users identity.

#### Submit the transaction

As this is a write operation, it will cost the user Ether and the user will need to approve the transaction by signing it and submitting it to the network

    ethereumAndroid.submitTransaction(Activity parentActivity, int requestCode, String transactionString)

This will open an Activity where the user can approve the transaction. You will receive the outcome of the operation via

    parentActivity.onActivityResult(int requestCode, int resultCode, Intent data)

In case ```resultCode == RESULT_OK``` the result Intent will contain the transaction hash:

     String transaction = data.getStringExtra("transaction");

It the result was not OK the Intent will contain the error message instead:

     String error = data.getStringExtra("error");

### Check for the transaction receipt

Retrieve the transaction receipt which contains the address of the created contract.


    WrappedRequest wrappedRequest = new WrappedRequest();
    wrappedRequest.setCommand(RpcCommand.eth_getTransactionReceipt.toString());
    wrappedRequest.setParameters(new Object[]{transaction});


The response will contain a map containing  key-value-pairs of the transaction receipt, where you can read the contract address from.

    HashMap<String, String> transactionObject = (HashMap<String, String>) response.getResponse();
    String contractAddress = transactionObject.get("contractAddress");

### Interact with an existing contract

In order to easily interact with an existing contract you need the contract ABI and a Java interface containing the operations you would like to use.
This Java interface can contain a subset of the contract ABI, the method needs to be named exactly as the ABI function name and have the same input and output parameters.


For read operations (functions which are marked with ___constant___) this is all you need to do.


Write operations are represented as ```PendingTransaction``` which gives the ability to create the unsigned transaction and decode the result of the transaction once it is picked up by the network.


#### Read the stored value

     SimpleOwnedStorage simpleOwnedStorage = ethereumAndroid.contracts().bind(contractAddress, CONTRACT_ABI, SimpleOwnedStorage.class);
     String value = simpleOwnedStorage.get();

#### Write a new value

___Create the transaction___

      SimpleOwnedStorage simpleOwnedStorage = ethereumAndroid.contracts().bind(contractAddress, CONTRACT_ABI, SimpleOwnedStorage.class);
      PendingTransaction<Void> pendingWrite = simpleOwnedStorage.set("a new value");

___Submit the transaction___

      ethereumAndroid.submitTransaction(Activity parentActivity, int requestCode, pendingWrite.getUnsignedTransaction())

___Check the activity result___

     parentActivity.onActivityResult(int requestCode, int resultCode, Intent data)

In the sample case instead of waiting for the transaction receipt you can also just read the value again and check if it has already changed.

