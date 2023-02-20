package com.mikront.util.data;

import com.mikront.util.Utils;

import java.util.HashMap;


/**
 * Provides you the only method to obtain a specific communicator object anywhere
 */
public class CommunicatorProvider {
    private static final HashMap<String, Communicator> COMMUNICATORS = new HashMap<>();


    public synchronized static <T extends Communicator> T get(Class<T> communicator) {
        var key = communicator.getCanonicalName();

        var instance = COMMUNICATORS.get(key);
        if (instance == null) {
            instance = Utils.getNewInstanceOrThrow(communicator);

            COMMUNICATORS.put(key, instance);
        }

        return communicator.cast(instance);
    }
}