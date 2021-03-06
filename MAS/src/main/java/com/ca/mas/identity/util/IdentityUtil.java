/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.util.FoundationConsts;
import com.ca.mas.foundation.util.FoundationUtil;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.user.MASPhoto;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p><b>IdentityUtil</b> implements helper methods used in creating {@link <a href="https://tools.ietf.org/html/rfc7644">SCIM</a>} URIs,
 * retreiving users and groups that have been retrieved via web service calls to SCIM, and for formatting date strings. Usages of
 * this class follow the static format of 'IdentityUtil.&lt;method name&gt;.</p>
 */
public class IdentityUtil extends FoundationUtil {

    private static final String TAG = IdentityUtil.class.getSimpleName();

    public static final Map<String, JSONObject> SCHEMA_MAP = new HashMap<>();

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> This helper method creates a user name filter used for retrieving 0 or more
     * users from the SCIM provider.
     *
     * @param date any Java util date.
     * @return String  the date formatted as '2011-08-01T21:32:44.882Z'.
     */
    public static String getMetaDateString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(IdentityConsts.META_DATE_FORMAT);
        return formatter.format(date);
    }

   public static Bitmap getThumbnail(List<MASPhoto> photoList) {
        return getPhoto(photoList);
    }

    /**
     * <b>Pre-Conditions: </b> Must be called after the meta-information has been populated.<br>
     * <b>Description: </b> Search for the attribute in the set of allowable attributes. The logic is as follows;
     * i. The exact match for the attribute must exist in the 'attributes' passed in, or
     * ii. The attribute must start with attrname. where the '.' precedes the fully qualified attribute name.
     *
     * @param providedAttributes
     * @param allowedAttributes
     * @return List<String> the list of attributes that exists in the attribute set.
     */
    public static List<String> normalizeAttributes(@NonNull List<String> providedAttributes, @NonNull List<String> allowedAttributes) {
        List<String> replacements = new ArrayList<>();
        for (String attribute : providedAttributes) {
            // if we have an exact match, then the attribute belongs
            if (allowedAttributes.contains(attribute)) {
                replacements.add(attribute);
            } else {
                for (String s : allowedAttributes) {
                    // if there is a set of attribute.subattributes that match, then they all must
                    // be part of the attribute set.
                    if (s.startsWith(attribute + IdentityConsts.DOT)) {
                        replacements.add(s);
                    }
                }
            }
        }
        return replacements;
    }

    /**
     * <b>Description:</b> Helper to turn the names from the users in the user map into a string array.
     *
     * @param usersList the List object representation of the MASUser array.
     * @return String[] the array containing the MASUser userNames, only.
     */
    public static String[] getUserNamesAsStrings(List<MASUser> usersList) {
        String[] userArr = new String[usersList.size()];
        for (int i = 0; i < usersList.size(); i++) {
            userArr[i] = usersList.get(i).getUserName();
        }
        return userArr;
    }

    /**
     * <b>Pre-Conditions:</b> The msso_config must be parsed.<br>
     * <b>Description:</b> Create and return a url of the form
     * https://somehost.com:1111/SCIM/MAS/v2/Schemas
     *
     * @param context
     * @return String the schemas Url.
     */
    public static String getSchemasUrl(Context context) {
        return getUrl(context, IdentityConsts.SCIM_SCHEMAS);
    }

    /**
     * <b>Description:</b> Create and return the path component of a URI of the form
     * /SCIM/MAS/v2/Schemas
     * @param context
     * @return String the schemas path.
     */
    public static String getSchemasPath(Context context){
        return getPath(context, IdentityConsts.SCIM_SCHEMAS);
    }

    /**
     * <b>Pre-Conditions:</b> The msso_config must be parsed.<br>
     * <b>Description:</b> This helper method returns the User's URI as defined in the msso_config.json endpoint description.
     *
     * @param context the Android runtime environment.
     * @return String the formatted URI for accessing Users, ex: 'https://host.com:8443/SCIM/[provider]/v2/Users.
     */
    public static String getUserUrl(Context context) {
        return getUrl(context, IdentityConsts.SCIM_USERS);
    }

    /**
     * <b>Pre-Conditions:</b> The msso_config must be parsed.<br>
     * <b>Description:</b> This helper method returns the User's URI as defined in the msso_config.json endpoint description.
     * @param context the Android runtime environment.
     * @return String the formatted URI for accessing Users, ex: SCIM/[provider]/v2/Users.
     */
    public static String getUserPath(Context context) {
        return getPath(context, IdentityConsts.SCIM_USERS);
    }

    /**
     * <b>Pre-Conditions:</b> The msso_config must be parsed.<br>
     * <b>Description:</b>  This helper method returns the URL used in creating a resource on the SCIM server.
     *
     * @param context the Android runtime environment.
     * @return the URL for creating an entity on the SCIM server.
     */
    public static String getCreateUrl(Context context) {
        return getUrl(context, null);
    }

    /**
     * <b>Pre-Conditions:</b> The msso_config must be parsed.<br>
     * <b>Description:</b> This helper method returns the URL used in deleting a resource from the SCIM server.
     *
     * @param context the Android runtime environment.
     * @return the URL for deleting an entity on the SCIM server.
     */
    public static String getDeleteUrl(Context context, String id) {
        return getUrl(context, id);
    }

    /**
     * <b>Pre-Conditions:</b> The msso_config must be parsed.<br>
     * <b>Description:</b> This helper method returns the URL used in updating an existing resource on the SCIM server.
     *
     * @param context the Android runtime environment.
     * @return the URL for updating an entity on the SCIM server.
     */
    public static String getUpdateUrl(Context context, String id) {
        return getUrl(context, id);
    }

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized.<br>
     * <b>Description:</b> This helper method returns the Groups's URI as defined in the msso_config.json endpoint description.
     *
     * @param context this application context.
     * @return String the formatted URI for accessing Users, ex: 'https://host.com:8443/SCIM/[provider]/v2/Groups.
     */
    public static String getGroupUrl(Context context) {
        return getUrl(context, IdentityConsts.SCIM_GROUPS);
    }

    public static String getGroupPath(Context context) {
        return getPath(context, IdentityConsts.SCIM_GROUPS);
    }

    /*
    Creates the general URL used by SCIM. This method attaches the provider described in the
    msso_config.json file.
     */
    private static String getUrl(Context context, String entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(FoundationUtil.getFqdn());
        String scimPath = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()
                .getProperty(FoundationConsts.KEY_CONFIG_SCIM_PATH);
        if (scimPath == null) {
            scimPath = "/SCIM/MAS/v2";
        }
        sb.append(scimPath);
        if (!TextUtils.isEmpty(entity)) {
            sb.append(FoundationConsts.FSLASH);
            sb.append(entity);
        }
        Log.d(TAG, "get URL: " + sb.toString());
        return sb.toString();
    }

    /**
     * Like {@link #getUrl(Context, String)} but only returns the path component of the URL.
     * @param context
     * @param entity
     * @return
     */
    private static String getPath(Context context, String entity) {
        StringBuilder sb = new StringBuilder();
        String scimPath = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()
                .getProperty(FoundationConsts.KEY_CONFIG_SCIM_PATH);
        if (scimPath == null) {
            scimPath = "/SCIM/MAS/v2";
        }
        sb.append(scimPath);
        if (!TextUtils.isEmpty(entity)) {
            sb.append(FoundationConsts.FSLASH);
            sb.append(entity);
        }
        Log.d(TAG, "get Path: " + sb.toString());
        return sb.toString();
    }

    /**
     * <b>Pre-Conditions:</b> The user must have been retrieved from SCIM.<br>
     * <b>Description:</b> Format the byte stream in the Photo attribute so it can be displayed as a
     *
     * @param photoList the list of photos from the {@link MASUser#getUserById(String, MASCallback)} call. See {@link MASPhoto}.
     * @return Drawable  the Android Drawable object for display. Could be null.
     */
    public static Bitmap getPhoto(List<MASPhoto> photoList) {
        if (photoList != null) {
            for (MASPhoto photo : photoList) {
                if (photo != null && photo.getType().equals(IdentityConsts.PHOTO_TYPE_THUMBNAIL)) {
                    String value = photo.getValue();
                    int offset = value.indexOf(IdentityConsts.BASE64) + IdentityConsts.BASE64.length();
                    byte[] subVal = Base64.decode(value.substring(offset), 0);
                    return BitmapFactory.decodeByteArray(subVal, 0, subVal.length);
                }
            }
        }
        return null;
    }

    /**
     * <b>Description:</b> Given a filter, an operator, and an attribute, initiate the existing
     * {@link MASFilteredRequest}. For example, to create a
     * UserIdentity MASFilteredRequest for retrieving all users with the userName starting with 'joe'
     * you would call it as follows;
     * <pre><code>
     *     MASFilteredRequest frb = createFilter(attributesList, IdentityConsts.KEY_USER_ATTRIBUTES, "joe", IdentityConsts.OP_SW, "userName");
     * </code></pre>
     *
     * @param attributes     the allowed attributes for this filter
     * @param attributesType the type of attributes such as USER or GROUP.
     * @param filter         the expression that is applied to the operator.
     * @param op             one of the operators supported in this implementation such as {@link IdentityConsts#OP_CONTAINS} or {@link IdentityConsts#OP_EQUAL}
     * @param attribute      the attribute that is used for matching the filter when applied to the operator.
     */
    public static MASFilteredRequest createFilter(List<String> attributes, String attributesType, String filter, String op, String attribute) {
        MASFilteredRequest frb = new MASFilteredRequest(attributes, attributesType);
        if (op.equals(IdentityConsts.OP_EQUAL)) {
            frb.isEqualTo(attribute, filter);
        }

        if (op.equals(IdentityConsts.OP_NOT_EQUAL)) {
            frb.isNotEqualTo(attribute, filter);
        }

        if (op.equals(IdentityConsts.OP_CONTAINS)) {
            frb.contains(attribute, filter);
        }

        if (op.equals(IdentityConsts.OP_STARTS_WITH)) {
            frb.startsWith(attribute, filter);
        }

        if (op.equals(IdentityConsts.OP_ENDS_WITH)) {
            frb.endsWith(attribute, filter);
        }

        // the only unary operator
        if (op.equals(IdentityConsts.OP_IS_PRESENT)) {
            frb.isPresent(attribute);
        }

        if (op.equals(IdentityConsts.OP_GREATER_THAN)) {
            frb.isGreaterThan(attribute, filter);
        }

        if (op.equals(IdentityConsts.OP_GREATER_THAN_OR_EQUAL)) {
            frb.isGreaterThanOrEqual(attribute, filter);
        }

        if (op.equals(IdentityConsts.OP_LESS_THAN)) {
            frb.isLessThan(attribute, filter);
        }
        if (op.equals(IdentityConsts.OP_LESS_THAN_OR_EQUAL)) {
            frb.isLessThanOrEqual(attribute, filter);
        }
        return frb;
    }

}
