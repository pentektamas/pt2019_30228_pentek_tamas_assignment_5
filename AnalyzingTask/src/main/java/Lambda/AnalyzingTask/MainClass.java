package Lambda.AnalyzingTask;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class MainClass {

	public static void main(String[] args) {

		List<MonitoredData> splittedList = MonitoredData.getData();
		int days = MonitoredData.countDays(splittedList);
		System.out.println("The number of days is : " + days + "\n");
		Map<String, Integer> activities = MonitoredData.countActivities(splittedList);
		System.out.println("The list of activities is:");
		activities.entrySet().stream().forEach(System.out::println);
		System.out.println("\n");
		MonitoredData.countActivitiesEachDay(splittedList);
		System.out.println("Duration for each activity:\n");
		try {
			MonitoredData.getDuration(splittedList);
		} catch (ParseException e) {
			System.out.println("Error getDuration!!");
		}
		System.out.println("\n\nEntire duration over the monitoring period:\n");
		try {
			MonitoredData.getTotalDuration(splittedList);
		} catch (ParseException e) {
			System.out.println("Error getTotalDuration!!");
		}
		System.out.println(
				"\n\nActivities that have 90% of the monitoring records with duration less than 5 minutes are: \n");
		MonitoredData.getfilteredActivities(splittedList);
	}

}
