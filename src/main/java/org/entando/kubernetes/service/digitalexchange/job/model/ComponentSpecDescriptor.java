package org.entando.kubernetes.service.digitalexchange.job.model;

import lombok.Data;

import java.util.List;

@Data
public class ComponentSpecDescriptor {

    private List<String> plugins;
    private List<String> widgets;
    private List<String> pageModels;
    private List<String> contentTypes;
    private List<String> contentModels;
    private List<LabelDescriptor> labels;


}
