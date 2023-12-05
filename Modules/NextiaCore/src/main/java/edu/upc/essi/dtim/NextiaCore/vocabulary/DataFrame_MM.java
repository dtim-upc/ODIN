package edu.upc.essi.dtim.NextiaCore.vocabulary;

public class DataFrame_MM {
    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri="https://www.essi.upc.edu/dtim/dataframe-metamodel#";

    protected static String resource(String local )
    { return uri + local; }

    protected static String property (String local )
    { return uri + local; }

    public static final String DataSource = Init.DataSource();
    public static final String DataFrame = Init.DataFrame();
    public static final String Data = Init.Data();
    public static final String DataType = Init.DataType();
    public static final String Array = Init.Array();
    public static final String Primitive = Init.Primitive();
    public static final String String = Init.String();
    public static final String Number = Init.Number();


    public static final String hasData     = Init.hasData();
    public static final String hasDataType     = Init.hasDataType();




    public static class Init {
        public static String DataSource() { return resource("DataSource"); }
        public static String DataFrame() { return resource("DataFrame"); } //or object
        public static String Data() { return resource("Data"); }
        public static String DataType() { return resource("DataType"); }
        public static String Array() { return resource("Array"); }
        public static String Primitive() { return resource("Primitive"); }
        //NOT object <-- Strings, Arrays and Objects. DataFrames are also Python::Objects
        //int64, float64, datetime64, bool
        public static String String() { return resource("String"); }
        public static String Number() { return resource("Number"); }

        public static String hasData() { return property( "hasData"); }

        public static String hasDataType() { return property( "hasDataType"); }
    }

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
