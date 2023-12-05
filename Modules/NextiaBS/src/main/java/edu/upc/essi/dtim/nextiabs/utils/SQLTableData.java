package edu.upc.essi.dtim.nextiabs.utils;

import org.apache.jena.atlas.lib.Pair;

import java.util.ArrayList;
import java.util.List;

public class SQLTableData {
    private String name;

    //el datatype de les columnes hauria d'estar ja en RDFS? no tindria sentit, hauria d'estar en l'original
    // perque esta és la implementació, però despres s'haurà de consultar quina estrategia és suposo
    // per veure les conversións? Son diferents o son totes en SQL iguals?
    private List<Pair<String, String>> columns;

    private List<Pair<List<String>, String>> references;


    public SQLTableData(String name){
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public List<Pair<String, String>> getColumns(){
        return columns;
    }
    void putColumns(List<Pair<String, String>> columns){
        this.columns = columns;
    }

    void addColumn(Pair<String, String> column){
        columns.add(column);
    }

    public void addColumn(String name, String dataType) {
        columns.add(new Pair(name, dataType));
    }


    //setters i getters
    // Esta clase se setea al sacar el metamodelo de una base de datos
}
