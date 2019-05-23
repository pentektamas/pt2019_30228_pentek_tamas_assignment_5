package Lambda.AnalyzingTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.temporal.ChronoUnit;

public class MonitoredData {

	private String startTime;
	private String endTime;
	private String activity;

	public MonitoredData(String st, String et, String name) {
		this.startTime = st;
		this.endTime = et;
		this.activity = name;
	}

	public static List<MonitoredData> getData() {

		List<MonitoredData> list = new ArrayList<MonitoredData>();
		String fileName = "HW5_Activities.txt";
		Stream<String> stream = null;
		try {
			stream = Files.lines(Paths.get(fileName));
		} catch (IOException e) {
			System.out.println("Error while creating the stream form the file!");
		}

		List<String> start = stream.collect(Collectors.toList());
		Function<String, String[]> split = x -> x.split("[\\t]");
		for (String s : start) {
			String[] newString = split.apply(s);
			list.add(new MonitoredData(newString[0], newString[2], newString[4]));
		}
		return list;
	}

	public static int countDays(List<MonitoredData> list) {
		long days = list.stream().map(x -> x.getStartTime().substring(0, 10)).distinct().count();
		return (int) days;
	}

	public static Map<String, Integer> countActivities(List<MonitoredData> list) {

		Map<String, Integer> map = new TreeMap<String, Integer>();
		List<String> activities = list.stream().map(m -> m.getActivity()).distinct().collect(Collectors.toList());
		for (int i = 0; i < activities.size(); i++) {
			int j = i;
			long count = list.stream().filter(x -> x.activity.equals(activities.get(j))).count();
			map.put(activities.get(i), (int) count);
		}
		return map;
	}

	public static void countActivitiesEachDay(List<MonitoredData> list) {
		List<String> activities = list.stream().map(m -> m.getActivity()).distinct().collect(Collectors.toList());
		LocalDate day = LocalDate.parse(list.stream().findFirst().get().getStartTime().substring(0, 10));
		day = day.minusDays(1);
		Map<String, Integer> activitiesPerDay = new TreeMap<String, Integer>();
		long[] count = new long[activities.size()];
		for (int k = 0; k < MonitoredData.countDays(list); k++) {
			day = day.plusDays(1);
			LocalDate currentDay = day;
			activitiesPerDay.clear();
			for (int i = 0; i < activities.size(); i++) {
				int j = i;
				count[i] = list.stream().filter(x -> x.startTime.substring(0, 10).equals(currentDay.toString())
						&& x.activity.equals(activities.get(j))).count();
				if (count[i] != 0)
					activitiesPerDay.put(activities.get(i), (int) count[i]);
			}
			System.out.println("DAY " + (k + 1) + " :");
			activitiesPerDay.entrySet().stream().forEach(System.out::println);
			System.out.println("\n");
		}
	}

	public static List<String> getDuration(List<MonitoredData> list) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		List<String> duration = new ArrayList<String>();
		Function<MonitoredData, String> asd = y -> {
			long sec = ChronoUnit.SECONDS.between(LocalDateTime.parse(y.getStartTime(), formatter),
					LocalDateTime.parse(y.getEndTime(), formatter));
			return ((y.getActivity().length() < 8) ? "\t\t\t" : "\t\t") + "Duration: " + sec / 3600 + ":"
					+ (sec - (sec / 3600) * 3600) / 60 + ":"
					+ (sec - ((sec - (sec / 3600) * 3600) / 60) * 60 - (sec / 3600) * 3600);
		};
		list.stream().forEach(x -> System.out
				.println(x.getStartTime() + "\t\t" + x.endTime + "\t\t" + x.getActivity() + "\t" + asd.apply(x)));
		duration.stream().forEach(System.out::println);
		return duration;
	}

	public static void getTotalDuration(List<MonitoredData> list) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		List<Integer> duration = new ArrayList<Integer>();
		List<String> activities = list.stream().map(m -> m.getActivity()).distinct().collect(Collectors.toList());
		List<String> totalDuration = new ArrayList<String>();
		for (int k = 0; k < activities.size(); k++) {
			Function<MonitoredData, Long> func = x -> ChronoUnit.SECONDS.between(
					LocalDateTime.parse(x.getStartTime(), formatter), LocalDateTime.parse(x.getEndTime(), formatter));
			int m = k;
			long seconds = list.stream().filter(x -> x.getActivity().equals(activities.get(m)))
					.collect(Collectors.summingLong(z -> func.apply(z)));
			duration.add((int) seconds);
		}
		int j = 0;
		for (Integer integer : duration) {
			long hr = integer / 3600;
			long min = (integer - hr * 3600) / 60;
			long s = (integer - min * 60 - hr * 3600);
			totalDuration.add(activities.get(j) + "  " + hr + ":" + min + ":" + s);
			j++;
		}
		totalDuration.stream().sorted().forEach(System.out::println);
	}

	public static void getFilteredActivities(List<MonitoredData> list) {

		Map<String, Integer> map = MonitoredData.countActivities(list);
		List<Integer> filteredValues = map.values().stream().map(x -> Math.round((x * 90f / 100)))
				.collect(Collectors.toList());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		List<String> activities = list.stream().map(m -> m.getActivity()).distinct().collect(Collectors.toList());
		List<String> filteredActivities = new ArrayList<String>();
		activities = activities.stream().sorted().collect(Collectors.toList());
		for (int k = 0; k < activities.size(); k++) {
			int nr = 0;
			for (MonitoredData m : list) {
				if (m.getActivity().equals(activities.get(k))) {
					LocalDateTime now = LocalDateTime.parse(m.getStartTime(), formatter);
					LocalDateTime then = LocalDateTime.parse(m.getEndTime(), formatter);
					long sec = ChronoUnit.SECONDS.between(now, then);
					if (sec < 300)
						nr++;
				}
			}
			if (nr >= filteredValues.get(k))
				filteredActivities.add(activities.get(k));
		}
		filteredActivities.stream().forEach(System.out::println);
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}
}
