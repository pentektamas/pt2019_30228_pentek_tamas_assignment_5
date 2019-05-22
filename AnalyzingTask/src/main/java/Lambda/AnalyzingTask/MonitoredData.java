package Lambda.AnalyzingTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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

		LocalDate start = LocalDate.parse(list.get(0).getStartTime().substring(0, 10));
		LocalDate end = LocalDate.parse(list.get(list.size() - 1).getEndTime().substring(0, 10));
		Period days = Period.between(start, end);
		return days.getDays() + 1;
	}

	public static Map<String, Integer> countActivities(List<MonitoredData> list) {

		Map<String, Integer> map = new TreeMap<String, Integer>();
		List<String> activities = list.stream().map(m -> m.getActivity()).distinct().collect(Collectors.toList());
		long[] count = new long[activities.size()];
		int i = 0;
		for (i = 0; i < activities.size(); i++) {
			int j = i;
			count[i] = list.stream().filter(x -> x.activity.equals(activities.get(j))).count();
			map.put(activities.get(i), (int) count[i]);
		}
		return map;
	}

	public static void countActivitiesEachDay(List<MonitoredData> list) {
		List<String> activities = list.stream().map(m -> m.getActivity()).distinct().collect(Collectors.toList());
		LocalDate day = LocalDate.parse(list.get(0).getStartTime().substring(0, 10));
		day = day.minusDays(1);
		Map<String, Integer> activitiesPerDay = new TreeMap<String, Integer>();
		long[] count = new long[activities.size()];
		for (int k = 0; k < MonitoredData.countDays(list); k++) {
			int i = 0;
			day = day.plusDays(1);
			LocalDate currentDay = day;
			activitiesPerDay.clear();
			for (i = 0; i < activities.size(); i++) {
				int j = i;
				count[i] = list.stream().filter(x -> x.startTime.substring(0, 10).equals(currentDay.toString())
						&& x.activity.equals(activities.get(j))).count();
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

		for (MonitoredData m : list) {
			LocalDateTime now = LocalDateTime.parse(m.getStartTime(), formatter);
			LocalDateTime then = LocalDateTime.parse(m.getEndTime(), formatter);
			long sec = ChronoUnit.SECONDS.between(now, then);
			long hr = sec / 3600;
			long min = (sec - hr * 3600) / 60;
			long s = (sec - min * 60 - hr * 3600);
			String temp;
			if (m.getActivity().length() < 8)
				temp = "\t\t\t";
			else
				temp = "\t\t";
			String date = hr + ":" + min + ":" + s;
			String full = m.getStartTime() + "\t\t" + m.getEndTime() + "\t\t" + m.getActivity() + temp + "Duration: "
					+ date;
			duration.add(full);
		}
		duration.stream().forEach(System.out::println);
		return duration;
	}

	public static void getTotalDuration(List<MonitoredData> list) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		List<Integer> duration = new ArrayList<Integer>();
		List<String> activities = list.stream().map(m -> m.getActivity()).distinct().collect(Collectors.toList());
		List<String> totalDuration = new ArrayList<String>();
		long sec = 0;
		for (int k = 0; k < activities.size(); k++) {
			for (MonitoredData m : list) {
				if (m.getActivity().equals(activities.get(k))) {
					LocalDateTime now = LocalDateTime.parse(m.getStartTime(), formatter);
					LocalDateTime then = LocalDateTime.parse(m.getEndTime(), formatter);
					sec = sec + ChronoUnit.SECONDS.between(now, then);
				}
			}
			duration.add((int) sec);
			sec = 0;
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

	public static void getfilteredActivities(List<MonitoredData> list) {

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
