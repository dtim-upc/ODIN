package edu.upc.essi.dtim.NextiaCD.parser;

import edu.upc.essi.dtim.NextiaCore.constraints.DenialConstraint;
import edu.upc.essi.dtim.NextiaCore.constraints.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DCParser {

    private static final Pattern pattern = Pattern.compile("(t\\d+)\\.(\\w+)\\(\\w+\\)?\\s*(==|<>|>=|<=|<|>)\\s*(t\\d+)\\.(\\w+)\\(\\w+\\)?");

    public static List<DenialConstraint> parse(String response) {
        List<DenialConstraint> dcs = new ArrayList<>();

        for (String line : response.split("\\R")) {
            line = line.trim();
            if (line.startsWith("¬(") && line.endsWith(")")) {
                String inner = line.substring(2, line.length() - 1); // remove ¬( and )
                String[] preds = inner.split("\\^");
                List<Predicate> predicates = new ArrayList<>();

                for (String p : preds) {
                    Matcher matcher = pattern.matcher(p.trim());
                    if (matcher.find()) {
                        String left = matcher.group(1) + "." + matcher.group(2);
                        String op = matcher.group(3);
                        String right = matcher.group(4) + "." + matcher.group(5);
                        predicates.add(new Predicate(left, op, right));
                    }
                }

                if (!predicates.isEmpty()) {
                    dcs.add(new DenialConstraint(predicates));
                }
            }
        }

        return dcs;
    }

    public static void main(String[] args) throws Exception {
        // Simulate JSON output from DQRuleDiscovery API
        String jsonResponse = "{ \"denial_constraints\": ["
                + "\"¬(t0.MarriedExemp(Integer) < t1.MarriedExemp(Integer) ^ t0.Zip(String) == t1.Zip(String) ^ t0.SingleExemp(Integer) == t1.SingleExemp(Integer))\","
                + "\"¬(t0.MarriedExemp(Integer) > t1.MarriedExemp(Integer) ^ t0.Zip(String) == t1.Zip(String))\""
                + "] }";

        // Parse JSON using Jackson
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode dcArray = root.get("denial_constraints");

        StringBuilder rawDCs = new StringBuilder();
        for (JsonNode node : dcArray) {
            rawDCs.append(node.asText()).append("\n");
        }

        // Use your DCParser to parse the raw denial constraint lines
        List<DenialConstraint> parsed = DCParser.parse(rawDCs.toString());

        // Print the parsed results
        System.out.println("Parsed Denial Constraints:");
        for (DenialConstraint dc : parsed) {
            System.out.println(dc);
        }
    }
}
