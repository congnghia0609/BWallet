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

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.ICredentialRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import wallettemplate.utils.JsonUtils;

/**
 *
 * @author nghiatc
 * @since Aug 6, 2017
 */
public class NCredentialRepository implements ICredentialRepository {
    public static final String NAKEY_FILENAME = "nauth.key";

    @Override
    public String getSecretKey(String userName) {
        System.out.println("read file key...");
        try {
            File wdir = new File("");
            String storePath = wdir.getAbsolutePath() + File.separator + "key" + File.separator + NAKEY_FILENAME;
//            File keystore = new File(storePath);
            FileInputStream fis = new FileInputStream(storePath);
            ObjectInputStream in = new ObjectInputStream(fis);
            String skey = in.readUTF();
            NCredentialRepository.NKey nkey = JsonUtils.Instance.getObject(NCredentialRepository.NKey.class, skey); //(NKey) in.readObject();
            if(nkey != null && nkey.getUserName().equalsIgnoreCase(userName)){
                return nkey.getKey();
            }
            in.close();
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated catch block
        return "";
    }

    @Override
    public void saveUserCredentials(String userName, String secretKey, int validationCode, List<Integer> scratchCodes) {
        try {
            System.out.println("create new key...");
            NCredentialRepository.NKey nkey = new NCredentialRepository.NKey(userName, secretKey, validationCode, scratchCodes);
            
            File wdir = new File("");
            String storePath = wdir.getAbsolutePath() + File.separator + "key" + File.separator + NAKEY_FILENAME;
            File keystore = new File(storePath);
            if(!keystore.getParentFile().exists()){
                keystore.getParentFile().mkdirs();
            }
            String sKey = JsonUtils.Instance.toJson(nkey);
            FileOutputStream fileOut = new FileOutputStream(keystore);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeBytes(sKey);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class NKey {
        private String userName;
        private String key;
        private int verificationCode;
        private List<Integer> scratchCodes;

        public NKey() {
        }

        public NKey(String userName, String key, int verificationCode, List<Integer> scratchCodes) {
            this.userName = userName;
            this.key = key;
            this.verificationCode = verificationCode;
            this.scratchCodes = scratchCodes;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
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
            return "NKey{" + "userName=" + userName + ", key=" + key + ", verificationCode=" + verificationCode + ", scratchCodes=" + scratchCodes + '}';
        }
    }
}
