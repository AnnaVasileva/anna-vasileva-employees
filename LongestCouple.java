import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LongestCouple {
	public static void main(String[] args) throws IOException {
		final int NUMBER_OF_COLUMNS = 4;

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		List<List<String>> fileData = new ArrayList<>();

		System.out.println("Please, enter file's full path.");
		String filePath = br.readLine();
		File fileName = new File(filePath);
		checkFile(fileName);

		br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();

		while (line != null) {
			List<String> list = Arrays.stream(line.split(", ")).collect(Collectors.toList());

			if (list.size() != NUMBER_OF_COLUMNS) {
				br.close();
				throw new IllegalArgumentException("Each row must contains exactlly 4 columns.");
			}

			fileData.add(list);
			line = br.readLine();
		}
		br.close();

		Map<String, Long> teams = makeTeams(fileData);
		findLongestCouple(teams);

	}

	private static void checkFile(File fileName) throws FileNotFoundException {
		FileInputStream inputStream = null;

		try {
			inputStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.getMessage();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.getMessage();
				}
			}
		}
	}

	private static Map<String, Long> makeTeams(List<List<String>> emplsInfo) {
		final int COL_EMPLID = 0;
		final int COL_PROJECTID = 1;
		final int COL_DATE_FROM = 2;
		final int COL_DATE_TO = 3;
		Map<String, Long> teams = new HashMap<>();

		for (int i = 0; i < emplsInfo.size() - 1; i++) {
			for (int j = i + 1; j < emplsInfo.size(); j++) {
				String emplIDA = emplsInfo.get(i).get(COL_EMPLID);
				String projectIDA = emplsInfo.get(i).get(COL_PROJECTID);
				LocalDate dateFromA = LocalDate.parse(emplsInfo.get(i).get(COL_DATE_FROM));
				LocalDate dateToA = ifNullReturnToday(emplsInfo.get(i).get(COL_DATE_TO));

				String emplIDB = emplsInfo.get(j).get(COL_EMPLID);
				String projectIDB = emplsInfo.get(j).get(COL_PROJECTID);
				LocalDate dateFromB = LocalDate.parse(emplsInfo.get(j).get(COL_DATE_FROM));
				LocalDate dateToB = ifNullReturnToday(emplsInfo.get(j).get(COL_DATE_TO));

				if (!emplIDA.equals(emplIDB) && projectIDA.equals(projectIDB)
						&& isSamePeriod(dateFromA, dateToA, dateFromB, dateToB)) {

					String employeesID = "";
					if (emplIDA.compareTo(emplIDB) < 0) {
						employeesID = emplIDA + " and " + emplIDB;
					} else {
						employeesID = emplIDB + " and " + emplIDA;
					}
					long period = overlapedPeriodOfDays(dateFromA, dateToA, dateFromB, dateToB) + 1;

					if (!teams.containsKey(employeesID)) {
						teams.put(employeesID, period);
					} else {
						teams.replace(employeesID, teams.get(employeesID) + period);
					}

				}
			}
		}

		return teams;
	}

	private static LocalDate ifNullReturnToday(String endDate) {
		return ("NULL".equals(endDate) ? LocalDate.now() : LocalDate.parse(endDate));
	}

	private static boolean isSamePeriod(LocalDate startA, LocalDate endA, 
			LocalDate startB, LocalDate endB) {
		return !(endB.isBefore(startA) || startB.isAfter(endA));
	}

	private static long overlapedPeriodOfDays(LocalDate startA, LocalDate endA, 
			LocalDate startB, LocalDate endB) {
		long period;

		if (startA.isBefore(startB)) {
			if (endA.isAfter(endB)) {
				period = ChronoUnit.DAYS.between(startB, endB);
			} else {
				period = ChronoUnit.DAYS.between(startB, endA);
			}
		} else {
			if (endA.isAfter(endB)) {
				period = ChronoUnit.DAYS.between(startA, endB);
			} else {
				period = ChronoUnit.DAYS.between(startA, endA);
			}
		}

		return period;
	}

	private static void findLongestCouple(Map<String, Long> teams) {
		if (teams.isEmpty()) {
			System.out.println("Everybody are solo players.");
		} else {
			long longestPeriod = Collections.max(teams.values());

			System.out.printf("The employees, who worked together for the longest period"
					+ " (total of %d days) are:%n", longestPeriod);

			teams.entrySet().stream().filter(entry -> entry.getValue() == longestPeriod)
					.forEach(entry -> System.out.printf("%s%n", entry.getKey()));
		}

	}
}
