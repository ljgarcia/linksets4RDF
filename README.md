linksets4RDF
============

Extracts types and relations from an RDF endpoint and models them as VoiD linksets and class partitions. Idea, definitions, scope, design, and initial code all developed as part of the BioHackathon 2014. The ultimate goal is to generate and visualize linksets across datasets. Just sarting to get there...

Version
----------
0.1.0, distributed under Apache 2.0 licence. Not even bet but more alpha version at this time. 

Description
----------
This project aims to build the linksets triples for all types in a given RDF dataset. Types taken into acoutn should be an owl:Class and be either the subject or object of an owl:ObjectProperty relation.
Some types are filtered out such as http://www.ifomis.org/,http://www.w3.org/2002/, some others are merged together like http://www.ebi.ac.uk/efo/* into http://www.ebi.ac.uk/efo/. For more information about filtering and merging take a look at config.properties.

Initially the total number of classes is calculated but only those beyond the specified OFFSET are retrieved. Every 100 processed types (not configurable at this time), a temporal linkset RDF is written to linksets.rdf.temp in the location specifed as program option. Any time a temporal RDF is written, the total number of types processed is informed. In such a way, if the process drops for any reason, it is possible to run it again from a particular OFFSET. 

Final linksets are written in the working directory specified as running option. If not all types are processed, the extension will be rdf.temp, otherwise it will be just rdf.

How to run
----------
The main class is uk.ac.ebi.rdf.linksets.LinksetsGeneration that can be run from command line with the folloing paramenter, all of them mandatory.
* -m extract (only extract mode is currently supported)
* -w <working directory> directory where RDF output will be saved, the RDF name will be linksets.rdf
* -ep <endpoint URI> endpoint for which you are generating linksets
* -ds <dataset URI> dataset URI
* -o <number> offset to process types (in case process drops in the middle)

