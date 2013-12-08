/*
* Copyright (C) 2013 - Gareth Llewellyn
*
* This file is part of Bowdlerize - https://bowdlerize.co.uk
*
* This program is free software: you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License
* for more details.
*
* You should have received a copy of the GNU General Public License along with
* this program. If not, see <http://www.gnu.org/licenses/>
*/
package uk.bowdlerize;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class API
{
    private Context mContext;
    public API(){}
    public static String USER_STATUS_PENDING = "pending";
    public static String USER_STATUS_OK = "ok";
    public static String USER_STATUS_FAILED = "failed";

    public static final String SETTINGS_EMAIL_ADDRESS = "emailAddress";
    public static final String SETTINGS_USER_PRIVATE_KEY = "userPrivKey";
    public static final String SETTINGS_PROBE_PRIVATE_KEY = "probePrivKey";
    public static final String SETTINGS_GCM_PREFERENCE = "gcm_pref";
    public static final String SETTINGS_UUID = "probe_uuid";
    public static final String SETTINGS_FREQUENCY = "frequency";
    public static final String SETTINGS_FREQUENCY_SEEK = "frequency_seek";

    public static final String EXTRA_POLL = "poll_for_url";
    public static final String EXTRA_GCM_TICKLE = "gcm_tickle";

    public static final String PROPERTY_REG_ID = "registration_id";

    public static final int SETTINGS_GCM_FULL = 0;
    public static final int SETTINGS_GCM_PARTIAL = 1;
    public static final int SETTINGS_GCM_DISABLED = 2;

    private SharedPreferences settings = null;

    public API(Context context)
    {
        this.mContext = context;
        settings = mContext.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    public String registerUser(String emailAddress, String password)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1.1/register/user");

        httpost.setHeader("Accept", "application/json");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", emailAddress));
        nvps.add(new BasicNameValuePair("password", password));

        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            if(json.getBoolean("success"))
            {
                return json.getString("private_key");
            }
            else
            {
                return null;
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserStatus() throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1.1/status/user");

        httpost.setHeader("Accept", "application/json");

        String emailAddress = settings.getString(SETTINGS_EMAIL_ADDRESS,"");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", emailAddress));
        nvps.add(new BasicNameValuePair("signature", SignHeaders(emailAddress)));

        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            //Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            if(json.getBoolean("success"))
            {
                return json.getString("status");
            }
            else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String getURLBasic() throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        /*HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1.1/request/httpt");

        httpost.setHeader("Accept", "application/json");
        String uuid = settings.getString(SETTINGS_UUID,"");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("probe_uuid", uuid));
        nvps.add(new BasicNameValuePair("basic", "true"));
        nvps.add(new BasicNameValuePair("signature", SignHeaders(uuid)));*/

        HttpGet httpGet = new HttpGet("https://bowdlerize.co.uk/api/1.1/request/httpt");

        httpGet.setHeader("Accept", "application/json");

        try
        {
            //httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpGet);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            if(json.getBoolean("success"))
            {
                return json.getString("url");
            }
            else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean submitURL(String url) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1.1/submit/url");

        httpost.setHeader("Accept", "application/json");

        String emailAddress = settings.getString(SETTINGS_EMAIL_ADDRESS,"");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", emailAddress));
        nvps.add(new BasicNameValuePair("url", url));
        nvps.add(new BasicNameValuePair("signature", SignHeaders(url)));

        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            return json.getBoolean("success");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String prepareProbe() throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1.1/prepare/probe");

        httpost.setHeader("Accept", "application/json");

        String emailAddress = settings.getString(SETTINGS_EMAIL_ADDRESS,"");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", emailAddress));
        nvps.add(new BasicNameValuePair("signature", SignHeaders(emailAddress)));

        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            if(json.getBoolean("success"))
            {
                return json.getString("probe_hmac");
            }
            else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String registerProbe(String probeSeed, String probe_uuid, String country) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1.1/register/probe");

        httpost.setHeader("Accept", "application/json");

        String emailAddress = settings.getString(SETTINGS_EMAIL_ADDRESS,"");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", emailAddress));
        nvps.add(new BasicNameValuePair("probe_seed", probeSeed));
        nvps.add(new BasicNameValuePair("probe_uuid", probe_uuid));
        nvps.add(new BasicNameValuePair("type", "android"));
        nvps.add(new BasicNameValuePair("cc", country));
        nvps.add(new BasicNameValuePair("signature", SignHeaders(probe_uuid)));

        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            if(json.getBoolean("success"))
            {
                return json.getString("private_key");
            }
            else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Pair<String,String> getISPMeta()
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpGet httpget = new HttpGet("http://wtfismyip.com/json");
        httpget.setHeader("Accept", "application/json");

        try
        {
            HttpResponse response = httpclient.execute(httpget);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            return new Pair(json.getString("YourFuckingLocation"),json.getString("YourFuckingISP"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return new Pair(null,null);
        }
    }

    public Boolean updateGCM(int type, int frequency) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1.1/update/gcm");

        httpost.setHeader("Accept", "application/json");

        String emailAddress = settings.getString(SETTINGS_EMAIL_ADDRESS,"");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("probe_uuid", settings.getString(API.SETTINGS_UUID,"")));
        nvps.add(new BasicNameValuePair("frequency", Integer.toString(frequency)));
        nvps.add(new BasicNameValuePair("gcm_id", settings.getString(API.PROPERTY_REG_ID,"")));
        nvps.add(new BasicNameValuePair("gcm_type", Integer.toString(type)));
        nvps.add(new BasicNameValuePair("signature", SignHeaders(settings.getString(API.PROPERTY_REG_ID,""),false)));

        Log.e("GCM",settings.getString(API.PROPERTY_REG_ID,""));
        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("update gcm json",rawJSON);
            json = new JSONObject(rawJSON);

            try
            {
                return json.getBoolean("success");
            }
            catch (Exception e)
            {
                return false;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private String SignHeaders(String dataToSign) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchProviderException, SignatureException
    {
        Log.e("SignHeaders",dataToSign);
        return SignHeaders(dataToSign,true);
    }

    private String SignHeaders(String dataToSign, boolean isUser) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchProviderException, SignatureException
    {
        PKCS8EncodedKeySpec spec;
        if(isUser)
        {
            spec = new PKCS8EncodedKeySpec(Base64.decode(settings.getString(SETTINGS_USER_PRIVATE_KEY,"").getBytes(), 0));
        }
        else
        {
            spec = new PKCS8EncodedKeySpec(Base64.decode(settings.getString(SETTINGS_PROBE_PRIVATE_KEY,"").getBytes(), 0));
        }

        KeyFactory kf = KeyFactory.getInstance("RSA","BC");
        PrivateKey pk = kf.generatePrivate(spec);
        byte[] signed = null;

        //Log.e("algorithm", pk.getAlgorithm());

        Signature instance = Signature.getInstance("SHA1withRSA");
        instance.initSign(pk);
        instance.update(dataToSign.getBytes());
        signed = instance.sign();

        Log.e("privateKey",settings.getString(SETTINGS_USER_PRIVATE_KEY,""));
        Log.e("privateKey",settings.getString(SETTINGS_PROBE_PRIVATE_KEY,""));
        //Log.e("Signature",Base64.encodeToString(signed, Base64.NO_WRAP));

        return Base64.encodeToString(signed, Base64.NO_WRAP);
    }


}
