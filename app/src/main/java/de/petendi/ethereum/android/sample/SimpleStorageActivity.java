package de.petendi.ethereum.android.sample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.HashMap;

import de.petendi.ethereum.android.EthereumAndroid;
import de.petendi.ethereum.android.EthereumAndroidFactory;
import de.petendi.ethereum.android.EthereumNotInstalledException;
import de.petendi.ethereum.android.contract.PendingTransaction;
import de.petendi.ethereum.android.sample.contract.SimpleOwnedStorage;
import de.petendi.ethereum.android.service.model.RpcCommand;
import de.petendi.ethereum.android.service.model.WrappedRequest;
import de.petendi.ethereum.android.service.model.WrappedResponse;

public class SimpleStorageActivity extends AppCompatActivity {

    private enum State {
        NO_CONTRACT_DEPLOYED,
        CONTRACT_NOT_MINED_YET,
        CONTRACT_DEPLOYED
    }

    private final static String CONTRACT_BYTECODCE = "6060604052604051610485380380610485833981016040528080518201919060200150505b5b33600060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908302179055505b8060016000509080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100a057805160ff19168380011785556100d1565b828001600101855582156100d1579182015b828111156100d05782518260005055916020019190600101906100b2565b5b5090506100fc91906100de565b808211156100f857600081815060009055506001016100de565b5090565b505033600060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908302179055505b5061034c806101396000396000f360606040526000357c0100000000000000000000000000000000000000000000000000000000900480634ed3885e1461004f5780636d4ce63c146100a5578063b387ef92146101205761004d565b005b6100a36004808035906020019082018035906020019191908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050909091905050610215565b005b6100b26004805050610159565b60405180806020018281038252838181518152602001915080519060200190808383829060006004602084601f0104600f02600301f150905090810190601f1680156101125780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b61012d600480505061031d565b604051808273ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b602060405190810160405280600081526020015060016000508054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102065780601f106101db57610100808354040283529160200191610206565b820191906000526020600020905b8154815290600101906020018083116101e957829003601f168201915b50505050509050610212565b90565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415610319578060016000509080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102ba57805160ff19168380011785556102eb565b828001600101855582156102eb579182015b828111156102ea5782518260005055916020019190600101906102cc565b5b50905061031691906102f8565b8082111561031257600081815060009055506001016102f8565b5090565b50505b5b50565b6000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050610349565b9056";
    private final static String CONTRACT_ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"d\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"currentOwner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"type\":\"function\"},{\"inputs\":[{\"name\":\"d\",\"type\":\"string\"}],\"type\":\"constructor\"}]";

    private static final String SIMPLE_STORAGE_PREFS = "simple_storage";
    private static final String CONTRACT_ADDRESS = "contractAddress";
    private static final String TRANSACTION = "transaction";
    private final static int REQUEST_CODE_DEPLOY = 753;
    private final static int REQUEST_CODE_WRITE = 754;


    private EthereumAndroid ethereumAndroid;
    private State currentState = State.NO_CONTRACT_DEPLOYED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this is a hack to disable the signature check so that it also connects
        //to development versions of Ethereum Android
        try {
            Field devField = EthereumAndroidFactory.class.getDeclaredField("DEV");
            devField.setAccessible(true);
            devField.set(null, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        EthereumAndroidFactory ethereumAndroidFactory = new EthereumAndroidFactory(this);
        try {
            ethereumAndroid = ethereumAndroidFactory.create();
        } catch (EthereumNotInstalledException e) {
            Toast.makeText(this, R.string.ethereum_ethereum_not_installed, Toast.LENGTH_LONG).show();
            finish();
        }
        setContentView(R.layout.activity_simple_storage);
        applyState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DEPLOY) {
            if (resultCode == RESULT_OK) {
                String transaction = data.getStringExtra(TRANSACTION);
                Toast.makeText(this, "deploy contract transaction:  " + transaction, Toast.LENGTH_LONG).show();
                getSharedPreferences(SIMPLE_STORAGE_PREFS, MODE_PRIVATE).edit().putString(TRANSACTION, transaction).commit();
                applyState();
            } else {
                String error = data.getStringExtra("error");
                Toast.makeText(this, "deploying contract failed " + error, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CODE_WRITE) {
            if (resultCode == RESULT_OK) {
                String transaction = data.getStringExtra(TRANSACTION);
                Toast.makeText(this, "write value transaction " + transaction, Toast.LENGTH_LONG).show();
                getSharedPreferences(SIMPLE_STORAGE_PREFS, MODE_PRIVATE).edit().putString(TRANSACTION, transaction).commit();
            } else {
                String error = data.getStringExtra("error");
                Toast.makeText(this, "write value failed " + error, Toast.LENGTH_LONG).show();
            }
        }
    }


    private void applyState() {
        SharedPreferences prefs = getSharedPreferences(SIMPLE_STORAGE_PREFS, MODE_PRIVATE);
        String transaction = prefs.getString(TRANSACTION, null);
        if (transaction == null) {
            currentState = State.NO_CONTRACT_DEPLOYED;
        } else {
            String contractAddress = prefs.getString(CONTRACT_ADDRESS, null);
            if (contractAddress == null) {
                currentState = State.CONTRACT_NOT_MINED_YET;
            } else {
                currentState = State.CONTRACT_DEPLOYED;
            }
        }

        Button buttonRead = (Button) findViewById(R.id.read);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readValue();
            }
        });

        Button buttonWrite = (Button) findViewById(R.id.write);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeValue();
            }
        });

        Button buttonDeploy = (Button) findViewById(R.id.deploy_contract);
        buttonDeploy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deployContract();
            }
        });

        Button buttonReadReceipt = (Button) findViewById(R.id.read_receipt);
        buttonReadReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readContractAddress();
            }
        });

        AutoCompleteTextView valueTextview = (AutoCompleteTextView) findViewById(R.id.storage_input);

        switch (currentState) {
            case NO_CONTRACT_DEPLOYED:
                buttonWrite.setVisibility(View.GONE);
                buttonRead.setVisibility(View.GONE);
                buttonDeploy.setVisibility(View.VISIBLE);
                buttonReadReceipt.setVisibility(View.GONE);
                valueTextview.setVisibility(View.GONE);
                break;
            case CONTRACT_NOT_MINED_YET:
                buttonWrite.setVisibility(View.GONE);
                buttonRead.setVisibility(View.GONE);
                buttonDeploy.setVisibility(View.GONE);
                buttonReadReceipt.setVisibility(View.VISIBLE);
                valueTextview.setVisibility(View.GONE);
                break;
            case CONTRACT_DEPLOYED:
                buttonWrite.setVisibility(View.VISIBLE);
                buttonRead.setVisibility(View.VISIBLE);
                buttonDeploy.setVisibility(View.GONE);
                buttonReadReceipt.setVisibility(View.GONE);
                valueTextview.setVisibility(View.VISIBLE);
                break;
        }

    }


    private void readValue() {
        SharedPreferences prefs = getSharedPreferences(SIMPLE_STORAGE_PREFS, MODE_PRIVATE);
        String contractAddress = prefs.getString(CONTRACT_ADDRESS, null);
        final SimpleOwnedStorage simpleOwnedStorage = ethereumAndroid.contracts().bind(contractAddress, CONTRACT_ABI, SimpleOwnedStorage.class);
        Runnable readTask = new Runnable() {
            @Override
            public void run() {
                final String value = simpleOwnedStorage.get();
                Runnable showResult = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SimpleStorageActivity.this, "stored value: " + value, Toast.LENGTH_LONG).show();
                    }
                };
                SimpleStorageActivity.this.runOnUiThread(showResult);
            }
        };
        new Thread(readTask, "read contract data thread").start();
    }

    private void writeValue() {
        AutoCompleteTextView valueTextview = (AutoCompleteTextView) findViewById(R.id.storage_input);
        final String value = valueTextview.getText().toString();
        if (TextUtils.isEmpty(value)) {
            valueTextview.setError(getString(R.string.error_field_required));
        } else {
            SharedPreferences prefs = getSharedPreferences(SIMPLE_STORAGE_PREFS, MODE_PRIVATE);
            final String contractAddress = prefs.getString(CONTRACT_ADDRESS, null);
            final SimpleOwnedStorage simpleOwnedStorage = ethereumAndroid.contracts().bind(contractAddress, CONTRACT_ABI, SimpleOwnedStorage.class);
            Runnable writeTask = new Runnable() {
                @Override
                public void run() {
                    final PendingTransaction<Void> pendingWrite = simpleOwnedStorage.set(value);
                    Runnable transactionTask = new Runnable() {
                        @Override
                        public void run() {
                            ethereumAndroid.submitTransaction(SimpleStorageActivity.this, REQUEST_CODE_WRITE, pendingWrite.getUnsignedTransaction());
                        }
                    };
                    SimpleStorageActivity.this.runOnUiThread(transactionTask);
                }
            };
            new Thread(writeTask, "write contract data thread").start();
        }
    }

    private void readContractAddress() {
        final SharedPreferences prefs = getSharedPreferences(SIMPLE_STORAGE_PREFS, MODE_PRIVATE);
        final String transaction = prefs.getString(TRANSACTION, null);
        Runnable readTask = new Runnable() {
            @Override
            public void run() {
                WrappedRequest wrappedRequest = new WrappedRequest();
                wrappedRequest.setCommand(RpcCommand.eth_getTransactionReceipt.toString());
                wrappedRequest.setParameters(new Object[]{transaction});
                final WrappedResponse response = ethereumAndroid.send(wrappedRequest);
                if (response.isSuccess()) {
                    HashMap<String, String> transactionObject = (HashMap<String, String>) response.getResponse();
                    final String contractAddress = transactionObject.get(CONTRACT_ADDRESS);
                    if (contractAddress != null) {
                        prefs.edit().putString(CONTRACT_ADDRESS, contractAddress).commit();
                        Runnable updateStateTask = new Runnable() {
                            @Override
                            public void run() {
                                applyState();
                            }
                        };
                        runOnUiThread(updateStateTask);
                    }
                } else {
                    Runnable showErrorTask = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SimpleStorageActivity.this, "reading address failed " + response.getErrorMessage(), Toast.LENGTH_LONG).show();
                        }
                    };
                    runOnUiThread(showErrorTask);
                }
            }
        };
        new Thread(readTask, "read contract address thread").start();
    }

    private void deployContract() {
        Runnable deployContractTask = new Runnable() {
            @Override
            public void run() {
                final String transaction = ethereumAndroid.contracts().create(CONTRACT_BYTECODCE, CONTRACT_ABI, "initial value");
                Runnable submitTransactionTask = new Runnable() {
                    @Override
                    public void run() {
                        ethereumAndroid.submitTransaction(SimpleStorageActivity.this, REQUEST_CODE_DEPLOY, transaction);
                    }
                };
                runOnUiThread(submitTransactionTask);
            }
        };
        new Thread(deployContractTask, "create contract thread").start();
    }
}
