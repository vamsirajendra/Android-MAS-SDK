/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.DeviceIdentifier;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationUtil;

/**
 * <p>The <b>MASDevice</b> class is a local representation of device data.</p>
 */
public abstract class MASDevice implements Device {
    private static MASDevice current;

    private MASDevice() {
    }

    public static MASDevice getCurrentDevice() {
        if (current == null) {
            current = new MASDevice() {
                @Override
                public void deregister(final MASCallback<Void> callback) {
                    final MobileSso mobileSso = MobileSsoFactory.getInstance();
                    if (mobileSso != null && mobileSso.isDeviceRegistered()) {
                        Thread t = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    mobileSso.removeDeviceRegistration();
                                    TokenManager manager = createTokenManager();
                                    if (manager.getSecureIdToken() != null) {
                                        manager.deleteSecureIdToken();
                                    }
                                    Callback.onSuccess(callback, null);
                                } catch (Exception e) {
                                    Callback.onError(callback, e);
                                }
                            }
                        });
                        t.start();
                    } else {
                        Callback.onError(callback, new IllegalStateException("Device is not registered"));
                    }
                }

                @Override
                public boolean isRegistered() {
                    MobileSso mobileSso = FoundationUtil.getMobileSso();
                    return mobileSso.isDeviceRegistered();
                }

                @Override
                public void resetLocally() {
                    FoundationUtil.getMobileSso().destroyAllPersistentTokens();
                }

                @Override
                public String getIdentifier() {
                    return (new DeviceIdentifier(MAS.getContext())).toString();
                }

                @Override
                public void startAsBluetoothPeripheral(MASProximityLoginBLEPeripheralListener listener) {
                    MobileSsoFactory.getInstance().startBleSessionSharing(listener);
                }

                @Override
                public void stopAsBluetoothPeripheral() {
                    MobileSsoFactory.getInstance().stopBleSessionSharing();
                }
            };
        }
        return current;
    }

    static StorageProvider createStorageProvider() {
        ConfigurationProvider configurationProvider = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();
        StorageProvider storageProvider = new StorageProvider(MAS.getContext(), configurationProvider);
        return storageProvider;
    }

    static TokenManager createTokenManager() {
        return createStorageProvider().createTokenManager();
    }

}
