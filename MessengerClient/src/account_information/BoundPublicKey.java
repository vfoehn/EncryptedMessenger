package account_information;

import java.security.PublicKey;

import com.google.gson.Gson;

import cryptography.AsymmetricEncryption;

/*
 * This class binds a public key to a username.
 * 
 * Note: The field publicKeyString is only used to write and read the object from
 * disk more elegantly. In practice, publicKeyString contains the same information
 * as publicKey.
 */
public class BoundPublicKey {
	
	private String username;
	private PublicKey publicKey;
	private String publicKeyString;
	
	public BoundPublicKey(String username, PublicKey publicKey) {
		this.username = username;
		this.publicKey = publicKey;
		String publicKeyString = AsymmetricEncryption.keyToString(publicKey);
		this.publicKeyString = publicKeyString;
	}

	public String getUsername() {
		return username;
	}

	public PublicKey getPublicKey() throws Exception {
		return publicKey;
	}
	
	public String toString() {
		Gson gson = new Gson();  
		String boundPublicKeyJson = gson.toJson(this);
		return boundPublicKeyJson;
	}
	

}
