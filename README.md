# NICK
## Generate siigo-like xlsx files from EDN invoices
With this project, you can create XLSX files by using EDN files
as a source of data.

EDN files should be formatted like a DATAICO invoice type.

You can find 2 clojure namespaces
1. ```dataico.services.nick```: It has all the functions and data structures related to load EDN, processing
and sending formatted data to the workbook.
2. ```dataico.services.workbook```: Has functions and data structures related to load, create, format, write data
and save XLSX workbooks.

### FAQ
Q: Where I can find the output spreadsheets?
A: You can find them in the ```sheets/``` folder inside this project
 
Q: How do I run the tests?
A: ``` clj -M:test ```