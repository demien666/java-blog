package com.demien.es.domain.client;

import com.demien.es.system.event.Event;
import com.demien.es.system.event.EventType;

public class ClientCRUDEvent extends Event<ClientCRUDRequest, ClientEntity> {
    public ClientCRUDEvent(EventType type, ClientCRUDRequest payload) {
        super(type, payload);
    }
}
