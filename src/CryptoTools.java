//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

public class CryptoTools{
    private String keySeed;
    private byte[] seedBytes;
    private byte[] keyBytes;
    private SecretKeySpec secKeySpec;
    private SecureRandom randNum;
    private Cipher aesCipher;

    private byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private IvParameterSpec ivSpec = new IvParameterSpec(iv);

    public CryptoTools() throws Exception{
        aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        keyBytes = new byte[16];
    }

    public byte[] encryptMessage(byte[] messageIn, String keySeed) throws Exception{
        secKeySpec = getKey(keySeed);
        aesCipher.init(Cipher.ENCRYPT_MODE, secKeySpec, ivSpec);
        byte[] ciphertext = aesCipher.doFinal(messageIn);
        return ciphertext;
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

    public String hashPassword(String password){
        return password;        //For now
    }

    public Boolean verifyPassword(String password, String hash){
        return false;
    }
}
