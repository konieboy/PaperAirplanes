//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;
import javax.xml.bind.DatatypeConverter;

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
     *  encrypted string in the form of a byte array in string format.
     */
    public String encryptString(String strIn, String keySeedIn) throws Exception{
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

    /**
     *  arrayStrToArray takes in a array printed in string format
     *  and converts it to the byte array that it is representing.
     */
    public byte[] arrayStrToArray(String strIn){
        String strArr = strIn.replace("[", "");
        strArr = strArr.replace("]", "");
        strArr = strArr.replace(" ", "");
        String[] strArray = strArr.split(",");

        byte[] output = new byte[strArray.length];
        for(int i = 0; i < output.length; i++){
            output[i] = Byte.parseByte(strArray[i]);
        }

        return output;
    }

    /**
     *  decryptString takes in a string and a key and decrypts the string.
     *  the string it takes in should be a string representation of a
     *  string encrypted with the same key.
     */
    public String decryptString(String strIn, String keySeedIn) throws Exception{
        byte[] strArray = arrayStrToArray(strIn);
        return new String(decryptMessage(strArray, keySeedIn));
    }

    /**
     *  Decrypt message takes in a byte array and a key and decrypts them
     *  using the AES-CBC algorithm.  It returns the message as a byte array.
     */
    public byte[] decryptMessage(byte[] messageIn, String keySeed) throws Exception{
        secKeySpec = getKey(keySeed);
        aesCipher.init(Cipher.DECRYPT_MODE, secKeySpec, ivSpec);
        byte[] plaintext = aesCipher.doFinal(messageIn);
        return plaintext;
    }

    /**
     *  getKey takes in a string for the key and will return a key for the
     *  AES encryption using the string as a seed in the PRNG.
     */
    public SecretKeySpec getKey(String seedIn) throws Exception{
        randNum = randNum.getInstance("SHA1PRNG");
        seedBytes = seedIn.getBytes();
        randNum.setSeed(seedBytes);
        randNum.nextBytes(keyBytes);
        secKeySpec = new SecretKeySpec(keyBytes, "AES");
        return secKeySpec;
    }

    /**
     *  hashPassword takes in a plaintext password and uses a default java
     *  password hashing algorithm to generate a cryptographically secure
     *  hash that will be stored on the users profile on the server.
     */
    public String hashPassword(String password) throws Exception{
        byte[] salt = new byte[16];
        randNum.nextBytes(salt);
        String output = hashPasswordHelper(password, salt, iterations, 128);
        return output;
    }

    /**
     *  hashPasswordHelper takes in extra parameters in order to use the
     *  algorith to generate the cryptographic hash.
     */
    public String hashPasswordHelper(String password, byte[] salt, int iterations, int hashLength) throws Exception{
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, hashLength);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = keyFac.generateSecret(keySpec).getEncoded();

        byte[] outputArr = new byte[salt.length + hash.length];

        //combine the salt and the hash into one output array
        int arrCounter = 0;
        for(byte b : salt){
            outputArr[arrCounter] = b;
            arrCounter++;
        }for(byte b : hash){
            outputArr[arrCounter] = b;
            arrCounter++;
        }

        //convert the output array to a base64 string and return it
        String outputString = bytesToBase64(outputArr);
        return outputString;
    }

    /**
     *  verifyPassword takes in a plaintext password and a salted hash
     *  and returns whether the password matches the hash based on
     *  what the salt and hash are.
     *
     *  returns true if password matches, false otherwise
     */
    public Boolean verifyPassword(String password, String saltyHash) throws Exception{
        byte[] salt = new byte[16];
        byte[] saltHashArr = base64toBytes(saltyHash);
        byte[] hashIn = new byte[saltHashArr.length - salt.length];

        //split the salt and the hash from the salted hash parameter.
        int arrCounter = 0;
        for(int i = 0; i < salt.length; i++){
            salt[i] = saltHashArr[arrCounter];
            arrCounter++;
        }for(int i = 0; i < hashIn.length; i++){
            hashIn[i] = saltHashArr[arrCounter];
            arrCounter++;
        }
        System.out.println(hashIn.length);

        //initialize the algorithm tools.
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, 128);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] calcedHash = keyFac.generateSecret(keySpec).getEncoded();

        //make input hash and calced hash into base64 strings
        String hashInStr = bytesToBase64(hashIn);
        String calcedHashStr = bytesToBase64(calcedHash);

        //compare them to see if they match
        if(hashInStr.equals(calcedHashStr))
            return true;
        return false;
    }

    /**
     *  base64toBytes takes in a string and returns the binary byte
     *  array.
     */
    public byte[] base64toBytes(String lineIn) throws Exception{
        return DatatypeConverter.parseBase64Binary(lineIn);
    }


    /**
     *  bytesToBase64 takes in a binary byte array and returns a
     *  base64 string so that it can be stored cleanly.
     */
    public String bytesToBase64(byte[] bytesIn) throws Exception{
        return DatatypeConverter.printBase64Binary(bytesIn);
    }
}
