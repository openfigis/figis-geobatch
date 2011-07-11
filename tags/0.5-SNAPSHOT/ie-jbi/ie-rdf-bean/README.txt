
Manually install WFS2RDF libs.

mvn install:install-file -Dfile=WFS2RDF.jar -DgroupId=wfs2rdf -DartifactId=wfs2rdf -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=commons-logging-1.1.1.jar -DgroupId=wfs2rdf -DartifactId=commons-logging -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=geowl.jar -DgroupId=wfs2rdf -DartifactId=geowl -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=icu4j_3_4.jar -DgroupId=wfs2rdf -DartifactId=icu4j -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=iri.jar -DgroupId=wfs2rdf -DartifactId=iri -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=jena.jar -DgroupId=wfs2rdf -DartifactId=jena -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=xercesImpl.jar -DgroupId=wfs2rdf -DartifactId=xercesImpl -Dversion=1.0 -Dpackaging=jar
