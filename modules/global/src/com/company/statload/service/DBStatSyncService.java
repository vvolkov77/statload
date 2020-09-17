package com.company.statload.service;

public interface DBStatSyncService {
    String NAME = "statload_DBStatSyncService";

    String FullSync();

    String FullSyncStatPokaz();

    String FullSyncStatForm();

    String FullSyncStatSprav();

    String FullSyncStatBanks();

    String Sync();

    String SyncStatPokaz();

    String SyncStatForm();

    String SyncStatSprav();

    String SyncStatBanks();

}