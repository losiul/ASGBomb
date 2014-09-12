package com.moosesoft.asgbomb;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BombCodeChecker {
	
	private String gameCode;
	private int codesCount;
	
	public BombCodeChecker(String gameCode, int codesCount) {
		this.gameCode = gameCode;
		this.codesCount = codesCount;
	}
	
	public String getBombCode(int codeNumber) {
		return calculateCode(gameCode + "code_" + (codeNumber + 1));
	}
	
	public String getDefuseCode() {
		return calculateCode(gameCode + "defuse_code");
	}
	
	public boolean checkBombCode(String codeToCheck) {
		for(int i = 0; i < codesCount; ++i)
			if(!codeToCheck.equals(getBombCode(i)))
				return false;
		return true;
	}
	
	public boolean checkDefuseCode(String codeToCheck) {
		return codeToCheck.equals(getDefuseCode());
	}

	
	private String calculateCode(String text) {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			md.update(text.getBytes("UTF-8"), 0, text.length());
			byte[] sha1hash = md.digest();
	    
			String result = new String();
		    for(int i = 0; i < sha1hash.length && i < 6; ++i) {
		    	int number;
		    	if(sha1hash[i] > 0)
		    		number = (int)sha1hash[i];
		    	else
		    		number = (int)(-sha1hash[i]);
		    	number = number % 89 + 10;
		    	result += number;
		    }		    
		    return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
}
