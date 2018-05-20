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
    }
}
