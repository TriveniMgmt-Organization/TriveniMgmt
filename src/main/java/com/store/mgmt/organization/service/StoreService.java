package com.store.mgmt.organization.service;

import com.store.mgmt.organization.model.dto.*;

import java.util.UUID;

public interface StoreService {

    StoreDTO getStoreById(UUID id);
    StoreDTO createStore(CreateStoreDTO dto);
    StoreDTO updateStore(UUID id, UpdateStoreDTO dto);
    void deleteStore(UUID id);

}
