//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

import javax.crypto.*;
import java.security.*;

public class CryptoTools{
    private String keySeed;
    private Cipher aesCipher;

    public CryptoTools(){
        try{
            aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }catch(Exception e){
            System.out.println("Algorithm does not exist.");
        }
    }

    public byte[] encryptMessage(String messageIn, String keySeed){
        return messageIn.getBytes();
    }

    public String hash(String password){
        return password;        //For now
    }
}
