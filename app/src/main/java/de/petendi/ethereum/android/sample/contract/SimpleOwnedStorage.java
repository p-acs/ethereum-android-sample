package de.petendi.ethereum.android.sample.contract;

import java.math.BigInteger;

import de.petendi.ethereum.android.contract.PendingTransaction;


public interface SimpleOwnedStorage {

    BigInteger currentOwner();

    String get();

    PendingTransaction<Void> set(String data);
}
