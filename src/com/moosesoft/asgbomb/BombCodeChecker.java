package com.moosesoft.asgbomb;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BombCodeChecker {
	
	private String gameCode;
	
	public BombCodeChecker(String gameCode) {
		this.gameCode = gameCode;
	}
	
	public String getBombCode1() {
		return calculateCode(gameCode + "code_1");
	}
	
	public String getBombCode2() {
		return calculateCode(gameCode + "code_2");
	}
	
	public String getDefuseCode() {
		return calculateCode(gameCode + "defuse_code");
	}
	
	public boolean checkBombCode(String codeToCheck) {
		return codeToCheck.equals(getBombCode1()) || codeToCheck.equals(getBombCode2()); 
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
