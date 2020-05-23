package cryptography;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/*
 * AccountInformationIO has two functionalities:
 *   1) Take a String (usually the state of an account in Json format) as input,
 *      encrypt it and write it into a file.
 *   3) Take a file (usually the encrypted state of an account) as input, decrypt
 *      it and return it as a String.
 *
 * The key for the symmetric encryption is created using a password-based key.
 */
public class AccountInformationIO {

    //Arbitrarily selected 8-byte salt sequence:
    private static final byte[] salt = {
            (byte) 0x43, (byte) 0x76, (byte) 0x95, (byte) 0xc7,
            (byte) 0x5b, (byte) 0xd7, (byte) 0x45, (byte) 0x17
    };

    private static Cipher makeCipher(String pass, Boolean decryptMode) throws GeneralSecurityException {
        //Use a KeyFactory to derive the corresponding key from the passphrase:
        PBEKeySpec keySpec = new PBEKeySpec(pass.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

        if (decryptMode) {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
        }

        return cipher;
    }

    // Encrypts plaintext and writes it to a file.
    public static void encryptAndWrite(String plainText, String fileName, String pass)
            throws IOException, GeneralSecurityException {
        File file = new File(fileName);
        if (!file.exists())
            file.createNewFile();

        byte[] decData;
        byte[] encData;

        byte[] plainTextBytes = plainText.getBytes();
        int plainTextLength = plainTextBytes.length;

        int blockSize = 8;
        int paddedCount = blockSize - (plainTextLength % blockSize);
        int padded = plainTextLength + paddedCount;

        decData = new byte[padded];
        for (int i = 0; i < plainTextLength; i++)
            decData[i] = plainTextBytes[i];

        //Write out padding bytes as per PKCS5 algorithm
        for (int i = plainTextLength; i < padded; ++i) {
            decData[i] = (byte) paddedCount;
        }

        Cipher cipher = makeCipher(pass, true);
        encData = cipher.doFinal(decData);
        //System.out.println(new String(encData));

        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(encData);
        outStream.close();
    }

    // Reads a file and returns its decrypted content. 
    public static String readAndDecrypt(String fileName, String pass)
            throws GeneralSecurityException, IOException {
        byte[] encData;
        byte[] decData;
        File inFile = new File(fileName);

        FileInputStream inStream = new FileInputStream(inFile);
        encData = new byte[(int) inFile.length()];
        inStream.read(encData);
        inStream.close();

        Cipher cipher = makeCipher(pass, false);
        decData = cipher.doFinal(encData);

        //Figure out how much padding to remove
        int padCount = (int) decData[decData.length - 1];
        if (padCount >= 1 && padCount <= 8) {
            decData = Arrays.copyOfRange(decData, 0, decData.length - padCount);
        }

        return new String(decData);
    }

}