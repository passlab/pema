
## HWLOC XML Parsing
hwloc XML parsing is implemented using JAXB Java-XML binding and the source files in folder
`xjcgenerated` are generated using xjc utility installed on Ubuntu Linux. Its verion is
`xjc 2.2.8-b130911.1802`

The command to generate those Java-XML binding source files based on DTD is 
`xjc -dtd -p xxx.peviewer.hwloc3d.xjcgenerated hwloc2.dtd`

The xjc.sh utility comes with JAXB standalone download has some issue, thus we use the xjc utility
installed with Ubuntu
