/*
 * Copyright 2017 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wallettemplate;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import wallettemplate.utils.JsonUtils;

/**
 *
 * @author nghiatc
 * @since Aug 6, 2017
 */
public class NAuth {
    public static final String NAKEY_FILENAME = "nauth.key";
    public static final String issuer = "NAuth";
    public static final String account = "nghiatc@nauth.com";
    private GoogleAuthenticator ga;
//    private NCredentialRepository ncr;
    private GoogleAuthenticatorKey key;
    private String otpAuthURL;
    
    private static NAuth instance = null;

    private NAuth() {
        ga = new GoogleAuthenticator();
//        ncr = new NCredentialRepository();
//        ga.setCredentialRepository(ncr);
        key = ga.createCredentials();
        otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, account, key);
    }
    
    public String getUrlFull(){
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, account, key);
    }

    public GoogleAuthenticatorKey getKey() {
        return key;
    }

    public void setKey(GoogleAuthenticatorKey key) {
        this.key = key;
        otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, account, key);
    }

    public GoogleAuthenticator getGa() {
        return ga;
    }

    public void setGa(GoogleAuthenticator ga) {
        this.ga = ga;
    }

    public String getOtpAuthURL() {
        return otpAuthURL;
    }

//    public void setOtpAuthURL(String otpAuthURL) {
//        this.otpAuthURL = otpAuthURL;
//    }
    
    public boolean verifyCode(int code){
        return ga.authorize(key.getKey(), code);
    }
    
    public static NAuth getInstance(){
        if(instance == null){
            instance = new NAuth();
            try {
                File wdir = new File("");
                String storePath = wdir.getAbsolutePath() + File.separator + "key" + File.separator + NAKEY_FILENAME;
                File keystore = new File(storePath);
                if(!keystore.getParentFile().exists()){
                    keystore.getParentFile().mkdirs();
                }
                if(keystore.isFile()){
                    System.out.println("Read file key...");
                    try {
                        FileInputStream fis = new FileInputStream(storePath);
                        ObjectInputStream in = new ObjectInputStream(fis);
                        String skey = in.readLine();
                        NKey nkey = JsonUtils.Instance.getObject(NKey.class, skey);
                        // GoogleAuthenticatorKey key = new GoogleAuthenticatorKey(nkey.getKey(), nkey.getVerificationCode(), nkey.getScratchCodes());
                        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(nkey.getKey())
                                .setVerificationCode(nkey.getVerificationCode())
                                .setScratchCodes(nkey.getScratchCodes()).build();
                        instance.setKey(key);
                        in.close();
                        fis.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Create and save new key...");
                    NKey nkey = new NKey(instance.getKey().getKey(), instance.getKey().getVerificationCode(), instance.getKey().getScratchCodes());
                    String sKey = JsonUtils.Instance.toJson(nkey);
                    FileOutputStream fileOut = new FileOutputStream(keystore);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeBytes(sKey);
                    out.close();
                    fileOut.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public String toString() {
        return "NAuth{" + "ga=" + ga + ", key=" + key + ", otpAuthURL=" + otpAuthURL + '}';
    }
    
    public static class NKey implements Serializable {
        private String key;
        private int verificationCode;
        private List<Integer> scratchCodes;

        public NKey() {
        }

        public NKey(String key, int verificationCode, List<Integer> scratchCodes) {
            this.key = key;
            this.verificationCode = verificationCode;
            this.scratchCodes = scratchCodes;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getVerificationCode() {
            return verificationCode;
        }

        public void setVerificationCode(int verificationCode) {
            this.verificationCode = verificationCode;
        }

        public List<Integer> getScratchCodes() {
            return scratchCodes;
        }

        public void setScratchCodes(List<Integer> scratchCodes) {
            this.scratchCodes = scratchCodes;
        }

        @Override
        public String toString() {
            return "NKey{" + "key=" + key + ", verificationCode=" + verificationCode + ", scratchCodes=" + scratchCodes + '}';
        }
    }
    
    public static void main(String[] args) {
        NAuth na = NAuth.getInstance();
        System.out.println(JsonUtils.Instance.toJson(na));
        System.out.println("getUrlFull: " + na.getUrlFull());
        
        int code = 911416;
        System.out.println("na.verifyCode: " + na.verifyCode(code));
        
    }
}









