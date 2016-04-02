//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;

/**
 *  CryptoTools class is the basis for all crypto related parts of the
 *  chat protocol.
 */

public class CryptoTools{
    private String keySeed;
    private byte[] seedBytes;
    private byte[] keyBytes;
    private SecretKeySpec secKeySpec;
    private SecureRandom randNum;
    private Cipher aesCipher;

    private byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private IvParameterSpec ivSpec = new IvParameterSpec(iv);
    private int iterations = 30000;

    /**
     *  Default constructor sets up a Cipher for an AES-CBC encryption
     *  mode.
     */
    public CryptoTools() throws Exception{
        aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        keyBytes = new byte[16];
        randNum = randNum.getInstance("SHA1PRNG");
    }

    /**
     *  encryptString takes in a string and a key and returns an
     *  encrypted string.
     */
    public String encryptString(String strIn, String keySeedIn) throws Exception{
        //return new String(encryptMessage(strIn.getBytes(), keySeedIn));
        return Arrays.toString(decryptMessage(strIn.getBytes(), keySeedIn));
    }

    /**
     *  encryptMessage takes in an array of bytes and a key, then users
     *  the aes
     */
    public byte[] encryptMessage(byte[] messageIn, String keySeed) throws Exception{
        secKeySpec = getKey(keySeed);
        aesCipher.init(Cipher.ENCRYPT_MODE, secKeySpec, ivSpec);
        byte[] ciphertext = aesCipher.doFinal(messageIn);
        return ciphertext;
    }

    public byte[] arrayStrToArray(String strIn){
        return new byte[10];
    }

    public String decryptString(String strIn, String keySeedIn) throws Exception{
        byte[] strArray = arrayStrToArray(strIn);
        return new String(decryptMessage(strArray, keySeedIn));
    }

    public byte[] decryptMessage(byte[] messageIn, String keySeed) throws Exception{
        secKeySpec = getKey(keySeed);
        aesCipher.init(Cipher.DECRYPT_MODE, secKeySpec, ivSpec);
        byte[] plaintext = aesCipher.doFinal(messageIn);
        return plaintext;
    }

    public SecretKeySpec getKey(String seedIn) throws Exception{
        randNum = randNum.getInstance("SHA1PRNG");
        seedBytes = seedIn.getBytes();
        randNum.setSeed(seedBytes);
        randNum.nextBytes(keyBytes);
        secKeySpec = new SecretKeySpec(keyBytes, "AES");
        return secKeySpec;
    }

    public String hashPassword(String password) throws Exception{
        byte[] salt = new byte[16];
        randNum.nextBytes(salt);
        String output = hashPasswordHelper(password, salt, iterations, 128);
        return password;        //For now
        //return output;        //for later
    }

    public String hashPasswordHelper(String password, byte[] salt, int iterations, int hashLength) throws Exception{
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, hashLength);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = keyFac.generateSecret(keySpec).getEncoded();

        byte[] outputArr = new byte[salt.length + hash.length];

        int arrCounter = 0;
        for(byte b : salt){
            outputArr[arrCounter] = b;
            arrCounter++;
        }for(byte b : hash){
            outputArr[arrCounter] = b;
            arrCounter++;
        }

        String output = new String(outputArr);
        byte[] utf8Output = output.getBytes("UTF8");
        String outputString = new String(utf8Output, "UTF8");
        return outputString;
    }

    public Boolean verifyPassword(String password, String saltyHash) throws Exception{
        byte[] salt = new byte[16];
        byte[] saltHashArr = saltyHash.getBytes();
        byte[] hashIn = new byte[32];

        int arrCounter = 0;
        for(int i = 0; i < salt.length; i++){
            salt[i] = saltHashArr[arrCounter];
            arrCounter++;
        }for(int i = 0; i < hashIn.length; i++){
            hashIn[i] = saltHashArr[arrCounter];
            arrCounter++;
        }
        System.out.println(hashIn.length);

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, 128);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] calcedHash = keyFac.generateSecret(keySpec).getEncoded();

        String hashInStr = new String(hashIn);
        String calcedHashStr = new String(calcedHash);

        if(password.equals(saltyHash))
            return true;
        return false;

        // if(hashInStr.equals(calcedHashStr))
        //     return true;
        // return false;
    }
}
