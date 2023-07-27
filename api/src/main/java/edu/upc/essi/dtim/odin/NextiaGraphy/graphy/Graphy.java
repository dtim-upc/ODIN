package edu.upc.essi.dtim.odin.NextiaGraphy.graphy;

import lombok.Data;

import java.util.List;

@Data
public class Graphy {

    List<Nodes> nodes;
    List<Links> links;
}
