package oracle.votingsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class VoteCounterMain {
	static Map<String, String> candidateList;
	static ArrayList<String> userInputList = new ArrayList<String>();
	static Map<String, Integer> upperGroupSize = new HashMap<String, Integer>();
	static String[] greaterThanQcand = new String[1];
	static String[] otherCands = null;
	static int quota = 0;
	static int totalSize = userInputList.size();

	static Map<String, String> readInputFile(String path) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			Map<String, String> input = new HashMap<>();
			String line = br.readLine();
			int count = 0;
			char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
			while (line != null) {
				if (count > 25) {
					return null;
				}
				input.put(Character.toString(alphabet[count++]), line.trim());
				line = br.readLine();
			}
			return input;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	static void displayInput(Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey() + ". " + entry.getValue());
		}
		System.out.println(">");
	}

	static Map getUserInput() {

		String userInput;

		Scanner scanner = new Scanner(System.in);
		while (true) {
			userInput = scanner.nextLine();
			if (userInput.equalsIgnoreCase("tally")) {
				break;
			} else {
				if(Validator.discardforinvalidinput(userInput.replaceAll(" ", ""))) {
					userInputList.add(userInput.replaceAll(" ", ""));
				}
			}
		}
		scanner.close();
		
		return Validator.validateInput(userInputList);
		// return Validator.validateInput(orginalInput,list);
	}
	
	static Map getUserInput(List autoInputList) {
		userInputList = (ArrayList<String>) autoInputList;
		return Validator.validateInput(autoInputList);
		// return Validator.validateInput(orginalInput,list);
	}

	public static Map<String, List> round1Grouping() {
		int quota = candidateList.size() / 2;
		System.out.println(quota);
		Collections.sort(userInputList);
		System.out.println("---------------------------------------------------");
		System.out.println("ROUND - 1 (Grouping the user input per candidate)");
		System.out.println("---------------------------------------------------");

		Map<String, List> uppergroup = new HashMap<String, List>();
		for (int i = 0; i < userInputList.size(); i++) {
			List lowerGroup = new ArrayList();
			for (int j = 0; j < userInputList.size(); j++) {
				if (userInputList.get(i).toString().charAt(0) == userInputList.get(j).toString().charAt(0)) {
					lowerGroup.add(userInputList.get(j));
				}
			}
			// Has grouped the input
			uppergroup.put(Character.toString(userInputList.get(i).toString().charAt(0)), lowerGroup);
			// has grouped based on the count
			upperGroupSize.put(Character.toString(userInputList.get(i).toString().charAt(0)), lowerGroup.size());
		}
		System.out.println(uppergroup);

		return uppergroup;

	}

	public static String getWinner() {
		int i = 1;
		Map<String, List> copyMap = round1Grouping();
		totalSize = userInputList.size();
		System.out.println("totalSize:" + totalSize);
		String runtillwinner = null;
		if(totalSize > 1) {
			do {
				System.out.println("---------------------------------------------------");
				System.out.println("ROUND" + (i));
				System.out.println("---------------------------------------------------");
				runtillwinner = getWinner(copyMap, i);
				i++;
	
			} while (runtillwinner.equals("continue"));
		} else {
			runtillwinner = "finish";
			//Get the 1st item and return it
			greaterThanQcand[0] = userInputList.get(0).toString();
		}
		
		if (runtillwinner.equals("finish")) {
			System.out.println("---------------------------------------------------");
			System.out.println("Winner Candidate : " + greaterThanQcand[0]);
		} else if (runtillwinner.equals("rhandomize")) {
			List<String> winnerList = new java.util.ArrayList<>(java.util.Arrays.asList(otherCands));
			System.out.println("---------------------------------------------------");
			for (String elements : otherCands) {
				Random rand = new Random();
				int index = rand.nextInt(winnerList.size());
				if (winnerList.size() != 1) {
					System.out.println("Vote eliminating Randomly :" + winnerList.get(index));
					winnerList.removeIf(p -> p.equals(winnerList.get(index)));
				} else {
					System.out.println("---------------------------------------------------");
					System.out.println("Winner Candidate after Random elimination : " + winnerList.get(index));
				}
			}
		}
		System.out.println("---------------------------------------------------");
		return runtillwinner;
	}

	public static String getWinner(Map<String, List> groupMap, int i) {

		String rhOrNext = "";

		for (Map.Entry<String, List> value : groupMap.entrySet()) {
			int removedItems = 0;
			int totalItems = 0;
			if (value.getValue().size() == i) {
				for (int k = 0; k < value.getValue().size(); k++) {
					for (int pref = 0; pref < value.getValue().get(k).toString().length(); pref++) {
						if (value.getValue().get(k).toString().length() == 1) {
							System.out.println("Eliminated Candidate : " + value.getValue().get(k));
							upperGroupSize.put(value.getKey(), 0);
							groupMap.remove(value.getKey(), value.getValue().get(k));
							removedItems++;
						}

						int nowSize = 0;
						String current = Character.toString(value.getValue().get(k).toString().charAt(pref));
						if (value.getKey().equals(current)) {
							continue;
						}
						if (groupMap.containsKey(current) && upperGroupSize.get(current) != 0) {

							List headFinder = groupMap.get(current);
							headFinder.add(value.getValue().get(k));
							upperGroupSize.put(current, headFinder.size());
							System.out.println(
									value.getValue().get(k) + " is moved to (Coz of minimum vote): " + current);
							nowSize = upperGroupSize.get(value.getKey());
							upperGroupSize.put(value.getKey(), nowSize - 1);
							break;
						} else {
							// search till the last item in the string and then remove it
							if (pref == value.getValue().get(k).toString().length() - 1) {
								nowSize = upperGroupSize.get(value.getKey());
								upperGroupSize.put(value.getKey(), nowSize - 1);
								System.out.println("Eliminated Candidate : " + value.getValue().get(k));
								groupMap.remove(value.getKey(), value.getValue().get(k));
								removedItems++;
							}
						}
					}
				}
			}

			// to get the possible winning candidates
			for (Map.Entry<String, Integer> sizeList : upperGroupSize.entrySet()) {
				if (sizeList.getValue() != 0)
					totalItems++;
			}

			totalSize = totalSize - removedItems;
			quota = (totalSize) / 2;
			otherCands = new String[totalItems];
			System.out.println("totalSize:"+totalSize);
			System.out.println("quota:"+quota);
			int si = 0;
			Set checkSet = new HashSet();
			for (Map.Entry<String, Integer> sizeList : upperGroupSize.entrySet()) {
				if (sizeList.getValue() != 0) {
					if (sizeList.getValue().doubleValue() > quota) {
						greaterThanQcand[0] = sizeList.getKey();
						rhOrNext = "finish";
					} else {
						checkSet.add(sizeList.getValue());
						otherCands[si] = sizeList.getKey();
						si++;
					}
				}
			}
			if (!rhOrNext.equals("finish")) {
				if (otherCands[0] != null) {
					/*
					 * for (int q = 0; q < otherCands.length; q++)
					 * System.out.println("otherCands[0]:" + otherCands[q]);
					 */
				}
				if (greaterThanQcand[0] == null && checkSet.size() != 1) {
					rhOrNext = "continue";
				} else if (checkSet.size() == 1 && otherCands[1] != null && greaterThanQcand[0] == null) {
					rhOrNext = "rhandomize";
					greaterThanQcand = otherCands;
				}
			}
		}
		System.out.println("Quota for winning : " + (long) Math.ceil(quota + 0.5d));
		System.out.println("Vote for each canditate : " + upperGroupSize);
		return rhOrNext;
	}

	public static void main(String args[]) {
		candidateList = readInputFile(
				System.getProperty("user.dir") + "\\src\\oracle\\votingsystem\\resources\\input.txt");
		displayInput(candidateList);
		candidateList = getUserInput();
		System.out.println(getWinner());

	}
	
	public static void performVoting(List autoInputList) {
		candidateList = readInputFile(
				System.getProperty("user.dir") + "\\src\\oracle\\votingsystem\\resources\\input.txt");
		displayInput(candidateList);
		candidateList = getUserInput(autoInputList);
		System.out.println(getWinner());

	}
}
