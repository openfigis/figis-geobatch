
Example of use:

1. From command line
1.1 Executing the converter.bat file
-Simply double click on the bat file. An RDF file is generated.
-If you want to see a verbouse output open a cmd window, set the prompt to the location of the this readme file, and execute the converter.bat
NOTE. the converter.bat is set to work with the statistical.csv file. This is the input parameter that the coversion procudere needs.

1.2 Executing the java command
- You can copy/past the following line in the cmd window, replacing %1 with the path to csv file you want to convert
java -cp .\lib\icu4j_3_4.jar;.\lib\iri.jar;.\lib\xercesImpl.jar;.\lib\commons-logging-1.1.1.jar;.\lib\geowl.jar;.\lib\jena.jar;.\WFS2RDF.jar org.fao.figis.wfs.Converter %1

2. Java library
1.1 Importing the WSF2RDF.jar
- Example of java class

public class MyClass {
    
    public void myConverter(String csvFilePath){
		//Create  an instance of the Convert
		Converter c = new Converter();
		//invoke the convert method with the path to the csv file
		String filePath = c.convert(csvFilePath);
		System.out.print(filePath);
    }
}