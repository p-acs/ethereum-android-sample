package de.petendi.ethereum.android.sample.contract;


import de.petendi.ethereum.android.contract.PendingTransaction;


public interface SimpleOwnedStorage {

    String currentOwner();

    String get();

    PendingTransaction<Void> set(String data);
}
