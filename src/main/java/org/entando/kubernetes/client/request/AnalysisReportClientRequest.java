package org.entando.kubernetes.client.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class AnalysisReportClientRequest {

    List<String> widgets;
    List<String> fragments;
    List<String> pages;
    List<String> pageTemplates;
    List<String> contents;
    List<String> contentTemplates;
    List<String> contentTypes;
    List<String> assets;
    List<String> resources;
    List<String> plugins;
    List<String> categories;
    List<String> groups;
    List<String> labels;
    List<String> languages;
    List<String> directories;
}
