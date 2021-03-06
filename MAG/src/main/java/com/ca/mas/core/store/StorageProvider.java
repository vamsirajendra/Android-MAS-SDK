/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.store;

import android.content.Context;
import android.util.Log;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoException;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceException;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.datasource.StringDataConverter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class to retrieve the Storage interface.
 */
public class StorageProvider {

    private static String TAG = "StorageProvider";
    private ConfigurationProvider configurationProvider;
    private Context context;
    private StorageConfig mStorageConfig;

    public StorageProvider(Context context, ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
        this.context = context;
        mStorageConfig = new StorageConfig(configurationProvider);
    }

    public StorageProvider(Context context) {
        this(context, ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider());
    }

    /**
     * Return the {@link TokenManager} that manage the share tokens and certificate.
     *
     * @return The {@link TokenManager}
     */
    public TokenManager createTokenManager() {
        String tm = configurationProvider.getProperty(MobileSsoConfig.PROP_SHARE_TOKEN_MANAGER);
        if (tm == null) {
            JSONObject params = new JSONObject();
            try {
                params = new JSONObject(mStorageConfig.getStorageConfig().toString());
                params.put(StorageConfig.PROP_SHARE_STATUS, Boolean.TRUE);
            } catch (JSONException e) {
                Log.w(TAG, "failed to set sharing property " + e);
            }
            DataSource storage = DataSourceFactory.getStorage(context, mStorageConfig.getStorageClass(), params, null);
            return new DefaultTokenManager(storage);
        } else {
            return (TokenManager) create(tm);
        }
    }

    /**
     * Return the {@link OAuthTokenContainer} that manage the private oauth tokens.
     *
     * @return The {@link OAuthTokenContainer}
     */

    public OAuthTokenContainer createOAuthTokenContainer() {
        String pt = configurationProvider.getProperty(MobileSsoConfig.PROP_PRIVATE_TOKEN_MANAGER);
        if (pt == null) {
            DataSource storage = DataSourceFactory.getStorage(context, mStorageConfig.getStorageClass(), mStorageConfig.getStorageConfig(), new StringDataConverter());
            return new PrivateTokenStorage(storage);
        } else {
            return (OAuthTokenContainer) create(pt);
        }
    }

    /**
     * Return the {@link ClientCredentialContainer} that manage the dynamic client id and client credentials.
     *
     * @return The {@link ClientCredentialContainer}
     */
    public ClientCredentialContainer createClientCredentialContainer() {
        String cc = configurationProvider.getProperty(MobileSsoConfig.PROP_CLIENT_CREDENTIAL_MANAGER);
        if (cc == null) {
            DataSource storage = DataSourceFactory.getStorage(context, mStorageConfig.getStorageClass(), mStorageConfig.getStorageConfig(), new StringDataConverter());
            return new ClientCredentialStorage(storage);
        } else {
            return (ClientCredentialContainer) create(cc);
        }
    }

    private Object create(String c) {
        try {
            Object o = Class.forName(c).newInstance();
            return o;
        } catch (Exception e) {
            throw new MssoException(e);
        }
    }

    /**
     * Checks if the Storage provider has a valid store to work with.
     *
     * @return True the storage is ready to use, False when the storage is not ready to use.
     */
    public boolean hasValidStore() {
        DataSource temp = DataSourceFactory.getStorage(context, mStorageConfig.getStorageClass(), mStorageConfig.getStorageConfig(), new StringDataConverter());
        if (temp != null && temp.isReady()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * The SDK's Storage Configuration. say
     * {@code
     * "storage": {
     * "class": "<Canonical name of the Storage class>",
     * "share": "true/false"
     * "bootprovider": "com.com.ca.mas.AStorageBootProvider"
     * }
     * }
     * <p/>
     * Responsibility:
     * - Parses the storage configuration from the JSON configuration file
     * - Validates the storage
     * - Falls back to the default, if there is no valid Storage configuration.
     */
    private static class StorageConfig {

        private String TAG = "StorageConfig";
        protected Class storageClass;
        protected JSONObject storageConfig = new JSONObject();

        /**
         * Common config properties expected for DataSource. Storage specif properties should be defined
         * in the individual DataSource files
         */
        public static String PROP_STORAGE_CLASS = "class";
        public static final String PROP_SHARE_STATUS = "share";

        public StorageConfig(ConfigurationProvider configurationProvider) {

            JSONObject storageJson = configurationProvider.getProperty(MobileSsoConfig.PROP_STORAGE);
            if (storageJson == null) {
                Log.i(TAG, "No storage configuration found in JSON config, falling back to DEFAULT ");
                storageClass = KeystoreDataSource.class;
                storageConfig = new JSONObject();
            } else {
                try {
                    storageClass = Class.forName("" + storageJson.get(PROP_STORAGE_CLASS));
                    storageConfig = storageJson;
                } catch (ClassNotFoundException e) {
                    throw new DataSourceException(String.format("Provided Storage configuration %s cannot be found ", storageJson.toString()), e);
                } catch (JSONException e) {
                    throw new DataSourceException("Invalid Storage Config" , e);
                }
            }

        }

        public Class getStorageClass() {
            return storageClass;
        }


        public JSONObject getStorageConfig() {
            return storageConfig;
        }
    }


}
