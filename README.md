![telomemore](logo/telomemore_logo.png?raw=true "Telomemore")


# Telomemore - Analysis of telomere state

This software is used to count telemoric reads in scATACseq data and multiomic scATACseq. 
Other motifs can also be used for the scan.

A less versatile prototype of Telomemore was first implemented in Python, with code available at https://github.com/henriksson-lab/telomemore

## Resources

In the Examples folder you will find:

* Jupyter notebooks showing integration with SCANPY
* R scripts showing integration with Signac

## Building and using

First build the software with the command `make` in the terminal. A file telomemore.jar will be produced.

To get information about usage:
`java -jar telomemore.jar`

Recommended basic usage for 10x output is:
`java -Xmx2g -jar telomemore.jar OUTPUT.csv -counttotal -countkmer -10xatacs /home/yours/thecellrangeroutputdiretory`

## Reference

"Telomemore enables single-cell analysis of cell cycle and chromatin condensation" by I.S.Mihai et al (Manuscript in preparation)

## License

Free software: MIT license

## Credits

Original implementation by William Rosenbaum. Java-reimplementation by Johan Henriksson.

