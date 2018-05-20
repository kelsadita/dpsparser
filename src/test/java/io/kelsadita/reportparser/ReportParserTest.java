package io.kelsadita.reportparser;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ReportParserTest {

    @Test
    public void testIncidentBreaker() {
        ReportParser reportParser = new ReportParser();

        List<String> actualIncidentBreak =
                reportParser.breakDescriptiveIncident("DISTURBANCE Disturbing The Peace");

        assertThat(actualIncidentBreak.get(0), equalTo("DISTURBANCE"));
        assertThat(actualIncidentBreak.get(1), equalTo("Disturbing The Peace"));

        actualIncidentBreak =
                reportParser.breakDescriptiveIncident("VANDALISM Vandalism-Misdemeanor");
        assertThat(actualIncidentBreak.get(0), equalTo("VANDALISM"));
        assertThat(actualIncidentBreak.get(1), equalTo("Vandalism-Misdemeanor"));

        actualIncidentBreak =
                reportParser.breakDescriptiveIncident("VAND-ALISM Vandalism/Misdemeanor");
        assertThat(actualIncidentBreak.get(0), equalTo("VAND-ALISM"));
        assertThat(actualIncidentBreak.get(1), equalTo("Vandalism/Misdemeanor"));

        actualIncidentBreak =
                reportParser.breakDescriptiveIncident("VAND-ALI-SM Vandalism/Misdem-eanor");
        assertThat(actualIncidentBreak.get(0), equalTo("VAND-ALI-SM"));
        assertThat(actualIncidentBreak.get(1), equalTo("Vandalism/Misdem-eanor"));
    }
}
