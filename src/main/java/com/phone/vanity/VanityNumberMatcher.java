package com.phone.vanity;

import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 *  by  HYang
 * 
 * 
 */
public class VanityNumberMatcher {

	private static final Pattern WORD_CLEAN_PATTERN = Pattern.compile("[\\p{InCombiningDiacriticalMarks}|\'|\\s]");
	private Node root;
	private Map<Character, char[]> keyPad;

	public VanityNumberMatcher(InputStream dictionary) {
		loadDefaultKeypad();
		loadDictionary(dictionary);
	}

	private void loadDefaultKeypad() {
		this.keyPad = new HashMap<>();
		this.keyPad.put('2', new char[] { 'A', 'B', 'C' });
		this.keyPad.put('3', new char[] { 'D', 'E', 'F' });
		this.keyPad.put('4', new char[] { 'G', 'H', 'I' });
		this.keyPad.put('5', new char[] { 'J', 'K', 'L' });
		this.keyPad.put('6', new char[] { 'M', 'N', 'O' });
		this.keyPad.put('7', new char[] { 'P', 'Q', 'R', 'S' });
		this.keyPad.put('8', new char[] { 'T', 'U', 'V' });
		this.keyPad.put('9', new char[] { 'W', 'X', 'Y', 'Z' });
	}

	private void loadDictionary(InputStream isDictionary) {
		try {
			InputStreamReader isr = new InputStreamReader(isDictionary);
			BufferedReader readerKeyPad = new BufferedReader(isr);

			for (String line = readerKeyPad.readLine(); line != null; line = readerKeyPad.readLine()) {
				String normalLine = normalize(line);
				if (!normalLine.isEmpty())
					put(normalLine);
			}

		} catch (Exception e) {
			throw new IllegalArgumentException("Error trying to read the dictionary file!", e);
		}
	}

	/**
	 * start match
	 *
	 */
	public void matches(String phone) {
		String holder = "";
		if (phone.isEmpty())
			return;
		this.initVanity(phone, 3, new char[phone.length() * 2], 0, holder);

	}

	/**
	 * Skip all first digits that are no mapped to letters ( 0 and 1)
	 * 
	 * At every word starting, control the allowed allowSkip ( like 404)
	 * 
	 * @param phone
	 * @param areaCodeIndex
	 * @param word
	 * @param holder
	 * @return
	 */
	private Boolean initVanity(String phone, int areaCodeIndex, char[] word, int letterIndex, String holder) {

		char[] letters = this.keyPad.get(phone.charAt(areaCodeIndex));
		while (letters == null && areaCodeIndex < phone.length()) {
			word[letterIndex] = phone.charAt(areaCodeIndex);
			areaCodeIndex++;
			letterIndex++;
			if (areaCodeIndex < phone.length()) {
				letters = this.keyPad.get(phone.charAt(areaCodeIndex));
			} else {
				printWord(word, holder, phone, areaCodeIndex);
				return true;
			}
		}

		return startVanityProcess(phone, areaCodeIndex, word, letterIndex, holder);
	}


	private Boolean startVanityProcess(String phone, int areaCodeIndex, char[] word, int letterIndex, String holder) {

		boolean regularAttempt = matchesLetters(this.root, phone, areaCodeIndex, word, letterIndex, holder);

		{

			word[letterIndex] = phone.charAt(areaCodeIndex);

			areaCodeIndex++;
			letterIndex++;

			if (areaCodeIndex < phone.length()) {
				initVanity(phone, areaCodeIndex, word, letterIndex, holder);
			} else {

				printWord(word, holder, phone, areaCodeIndex);
			}
		}

		return regularAttempt;
	}

	/**
	 * Control different possible path for every digit.
	 * 
	 *
	 * @param node
	 * @param phone
	 * @param areaCodeIndex
	 * @param word
	 * @param holder
	 * @return
	 */
	private Boolean matchesLetters(Node node, String phone, int areaCodeIndex, char[] word, int letterIndex,
			String holder) {
		char[] letters = this.keyPad.get(phone.charAt(areaCodeIndex)); 
		boolean worked = false;

		if (letters != null)

			for (int i = 0; i < letters.length; i++)
				if (matchTreeNode(node, phone, areaCodeIndex, word, letterIndex, holder, letters[i]))
					worked = true;

		return worked;
	}

	/**
	 * Traverse the tree structure.
	 *
	 */
	private boolean matchTreeNode(Node node, String phone, int areaCodeIndex, char[] word, int letterIndex,
			String holder, Character letter) {
		if (node == null)
			return false;

		boolean result;

		if (letter < node.c)
			result = matchTreeNode(node.left, phone, areaCodeIndex, word, letterIndex, holder, letter);
		else if (letter > node.c)
			result = matchTreeNode(node.right, phone, areaCodeIndex, word, letterIndex, holder, letter);
		else {
			word[letterIndex] = node.c;

			if (node.finished) {
				if (areaCodeIndex < phone.length() - 1) {
					Boolean sameWord = matchesLetters(node.mid, phone, areaCodeIndex + 1, word, letterIndex + 1,
							holder);
					Boolean nextWord = initVanity(phone, areaCodeIndex + 1, word, letterIndex + 1, holder);

					result = nextWord || sameWord;

				} else {
					printWord(word, holder, phone, areaCodeIndex);
					result = true;
				}
			} else {
				if (areaCodeIndex < phone.length() - 1)
					result = matchesLetters(node.mid, phone, areaCodeIndex + 1, word, letterIndex + 1, holder);
				else
					result = false;
			}
		}

		emptyWord(word, letterIndex);
		return result;
	}


	private void printWord(char[] word, String holder, String phone, int areaCodeIndex) {
		String tmp = new String(word).trim();
		if (tmp.matches("\\d*") && tmp.length() > 2) {
		} else {
			String pre = phone.substring(0, 3);
			String print = pre + tmp;
			System.out.println(print.toLowerCase());
		}
	}


	/**
	 * Clean a word using a space char in the given element range
	 *
	 * @param word
	 * @param f
	 */
	private void emptyWord(char[] word, int f) {
		for (int i = f; i < word.length; i++) {
			word[i] = ' ';
		}
	}

	/**
	 * construct tree
	 *
	 * @param key
	 */
	private void put(String key) {
		root = put(root, key, 0);
	}

	private Node put(Node x, String key, int d) {
		char c = key.charAt(d);
		if (x == null) {
			x = new Node();
			x.c = c;
		}
		if (c < x.c)
			x.left = put(x.left, key, d);
		else if (c > x.c)
			x.right = put(x.right, key, d);
		else if (d < key.length() - 1)
			x.mid = put(x.mid, key, d + 1);
		else
			x.finished = true;
		return x;
	}

	private String normalize(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		return WORD_CLEAN_PATTERN.matcher(str.toUpperCase()).replaceAll("").trim();
	}

}
