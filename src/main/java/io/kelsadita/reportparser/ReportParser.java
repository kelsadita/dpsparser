package io.kelsadita.reportparser;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportParser {
    public String readTextReport() throws IOException {
        File reportDir = new File(ReportParser.class.getResource("/reports").getFile());
        List<File> files =
                Arrays.asList(Objects.requireNonNull(reportDir.listFiles((d, name) -> name.endsWith(".txt"))));
        for (File dpsReport:
             files) {
            parseDpsReportToJson(dpsReport);
        }

        return "";

    }

    private void parseDpsReportToJson(File dpsReport) throws IOException {
        String dpsReportString = FileUtils.readFileToString(dpsReport, "utf-8");

        // Preprocessing
        dpsReportString = dpsReportString.replaceAll("University of Southern California\\n" +
                "Department of Public Safety\\n" +
                "Daily Incident Log\\nFrom \\d+\\/\\d+\\/\\d+ To \\d+\\/\\d+\\/\\d+", "");

        // Step1: Split the entire text report on the basis of Incident
        List<String> reportSections = Arrays.asList(dpsReportString.split("Incident:"));
        for (int index = 1; index < reportSections.size(); index ++) {
            String reportSection = reportSections.get(index);
            JSONObject reportSectionJson = convertStringSectionToJson(reportSection);
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

        // Parsing incident
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


        System.out.println(reportJsonRetVal);
        return reportJsonRetVal;
    }

    public List<String> breakDescriptiveIncident(String incident) {
        List<String> descriptiveIncidents = new ArrayList<>();

        return descriptiveIncidents;
    }

    public static void main(String[] args) throws IOException {
        ReportParser reportParser = new ReportParser();
        reportParser.readTextReport();
    }
}
