package io.kelsadita.reportparser;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportParser {

    private void readTextReport() throws IOException {
        File reportDir = new File(ReportParser.class.getResource("/reports").getFile());
        List<File> files =
                Arrays.asList(Objects.requireNonNull(reportDir.listFiles((d, name) -> name.endsWith(".txt"))));
        for (File dpsReport:
             files) {
            parseDpsReportToJson(dpsReport);
        }
    }

    private void parseDpsReportToJson(File dpsReport) throws IOException {
        String dpsReportString = FileUtils.readFileToString(dpsReport, "utf-8");

        // Preprocessing
        dpsReportString = dpsReportString.replaceAll("University of Southern California\\n" +
                "Department of Public Safety\\n" +
                "Daily Incident Log\\nFrom \\d+/\\d+/\\d+ To \\d+/\\d+/\\d+", "");

        // Step1: Split the entire text report on the basis of Incident
        List<String> reportSections = Arrays.asList(dpsReportString.split("Incident:"));
        JSONArray reportsJsons = new JSONArray();
        for (int index = 1; index < reportSections.size(); index ++) {
            String reportSection = reportSections.get(index);
            JSONObject reportSectionJson = convertStringSectionToJson(reportSection);
            reportsJsons.put(reportSectionJson);
        }

        // Step2: Saving the reports json array to file in same location
        String outputPath = ReportParser.class.getResource("/reports").getPath();
        String outputFilePath = outputPath + "/" + dpsReport.getName() + ".json";
        System.out.println(outputFilePath);
        try (FileWriter file = new FileWriter(outputFilePath)) {
            file.write(reportsJsons.toString());
        }
    }

    private JSONObject convertStringSectionToJson(String reportSection) {
        final JSONObject reportJsonRetVal = new JSONObject();

        // Parsing summary
        Pattern summaryPattern = Pattern.compile("Summary: (.*)?", Pattern.DOTALL);
        Matcher summaryPatternMatcher = summaryPattern.matcher(reportSection);
        if (summaryPatternMatcher.find()) {
            String summary = summaryPatternMatcher.group(1);
            reportJsonRetVal.put("summary", summary.replaceAll("\n", " "));
        }
        else {
            reportJsonRetVal.put("summary", "");
        }

        // Parsing disposition
        Pattern dispositionPattern = Pattern.compile("Disposition: (.*)?\nSummary:", Pattern.DOTALL);
        Matcher dispositionPatternMatcher = dispositionPattern.matcher(reportSection);
        if (dispositionPatternMatcher.find()) {
            String disposition = dispositionPatternMatcher.group(1);
            reportJsonRetVal.put("disposition", disposition.replaceAll("\n", ""));
        } else {
            reportJsonRetVal.put("disposition", "");
        }

        // Parsing incident and report Id
        Pattern incidentPattern =
                Pattern.compile("(\\d+)\\n(.*?)\\nReported: -", Pattern.MULTILINE);
        Matcher incidentPatternMatcher = incidentPattern.matcher(reportSection);
        if (incidentPatternMatcher.find()) {
            String reportId = incidentPatternMatcher.group(1).trim();
            String incident = incidentPatternMatcher.group(2).trim();
            List<String> descriptiveIncidents = breakDescriptiveIncident(incident);
            reportJsonRetVal.put("incident", descriptiveIncidents.get(0));
            reportJsonRetVal.put("incidentDesc", descriptiveIncidents.get(1));
            reportJsonRetVal.put("reportId", reportId);
        } else {
            reportJsonRetVal.put("reportId", "");
            reportJsonRetVal.put("incident", "");
            reportJsonRetVal.put("incidentDesc", "");
        }

        // Parsing occurred
        String occurredAtLine = reportSection.split("\n")[1];
        Pattern occurredToAndFromPattern =
                Pattern.compile("(\\d+/\\d+/\\d+\\s+\\d+:\\d+\\s+\\w{2})\\s*(\\d+/\\d+/\\d+)");
        Matcher occurredToAndFromMatcher = occurredToAndFromPattern.matcher(occurredAtLine);
        String occcurredFrom = "";
        String occcurredTo = "";
        if (occurredToAndFromMatcher.find()) {
            occcurredFrom = occurredToAndFromMatcher.group(1);
            occcurredTo = occurredToAndFromMatcher.group(2);
        }


        Pattern occuredToTimePattern = Pattern.compile("Location:\\n(.*?)-\\nReport #:");
        Matcher occuredToTimeMatcher = occuredToTimePattern.matcher(reportSection);
        String occuredToTime = "";
        if (occuredToTimeMatcher.find()) {
            occuredToTime = occuredToTimeMatcher.group(1).trim();
        }
        occcurredTo += " " + occuredToTime;

        reportJsonRetVal.put("occurredFrom", convertToStdDate(occcurredFrom));
        reportJsonRetVal.put("occurredTo", convertToStdDate(occcurredTo));


        // Parsing reported and location after removing the first line
        reportSection = reportSection.substring(reportSection.indexOf(System.getProperty("line.separator"))+1);
        reportSection = reportSection.substring(reportSection.indexOf(System.getProperty("line.separator"))+1);
        Pattern reportedAndLocationPattern =
                Pattern.compile("(\\d+/\\d+/\\d+\\s+\\d+:\\d+\\s+\\w{2})([\\dA-Z\\s\\p{Punct}]*?)(\\d{7})", Pattern.MULTILINE);
        Matcher reportedAndLocationMatcher = reportedAndLocationPattern.matcher(reportSection);
        if (reportedAndLocationMatcher.find()) {
            String reportedAt = reportedAndLocationMatcher.group(1).trim();
            String location = reportedAndLocationMatcher.group(2).trim();
            location = location.replaceAll("\n", " ");
            reportJsonRetVal.put("reportedAt", convertToStdDate(reportedAt));
            reportJsonRetVal.put("location", location);
        } else {
            reportJsonRetVal.put("reportedAt", "");
            reportJsonRetVal.put("location", "");
        }

        System.out.println(reportJsonRetVal);
        return reportJsonRetVal;
    }

    private String convertToStdDate(String dateRawString) {
        Pattern dateTimePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{1,2})\\s+(\\d{1,2}):(\\d{1,2})\\s+(am|pm)");
        Matcher dateTimePatternMatcher = dateTimePattern.matcher(dateRawString);
        if (dateTimePatternMatcher.find()) {
            int month = Integer.parseInt(dateTimePatternMatcher.group(1));
            int day = Integer.parseInt(dateTimePatternMatcher.group(2));
            int year = Integer.parseInt(dateTimePatternMatcher.group(3));

            String hours = dateTimePatternMatcher.group(4);
            String minutes = dateTimePatternMatcher.group(5);
            String section = dateTimePatternMatcher.group(6);
            String wellFormattedTime = hours + ":" + minutes + " " + section;

            LocalTime localTime = DateTimeFormat.forPattern("hh:mm aa").parseLocalTime(wellFormattedTime);
            int hour = localTime.getHourOfDay();
            int minute = localTime.getMinuteOfHour();

            DateTime dateTime = new DateTime(year, month, day, hour, minute);
            return dateTime.toString();
        } else {
            return dateRawString;
        }
    }

    public List<String> breakDescriptiveIncident(String incident) {
        List<String> descriptiveIncidents = new ArrayList<>(2);
        Pattern incidentBreakPattern = Pattern.compile("(\\b[A-Z\\s\\p{Punct}]+\\b)(\\b[A-Za-z\\p{Punct}\\s]+\\b)");
        Matcher incidentBreakMatcher = incidentBreakPattern.matcher(incident);
        if (incidentBreakMatcher.find()) {
            descriptiveIncidents.add(incidentBreakMatcher.group(1).trim());
            descriptiveIncidents.add(incidentBreakMatcher.group(2).trim());
        } else {
            descriptiveIncidents.add("");
            descriptiveIncidents.add("");
        }
        return descriptiveIncidents;
    }

    public static void main(String[] args) throws IOException {
        ReportParser reportParser = new ReportParser();
        reportParser.readTextReport();
    }
}
