# Overview
This project, SPAM - Suite of Pyroprint Analysis Methods, was the
implementation backing my M.S. work. Lots of work to be done here to clean it
up, eventually, just to see how much better I can make it (and because it is
pretty terribad).

# TODO
* De-couple xml/raw pyrosequencing methods
* fix visualization of two histograms
* clean up code (lots of this)

# Directory Layout
* gui - main GUI classes:
   * MainWindow - initial main window
   * Histogram - Draws the pane containing pyrograms
* guiListeners - miscellaneous listeners:
   * ExitListener - Closes the program
   * ImportDataListener - Creates a dialog for opening a pyrogram file
   * SaveFileListener - Creates a dialog for writing a pyrogram to a file
* outputHandlers - code for formatting various outputs:
   * MetricsDisplay - writes comparison results to a CSV file
* parsers:
   * PolyParse - parses dna files in the format:
     ```
        >description
        dna string
        ...
     ```
   * SequenceParser - parses dispensation sequence strings
* pyrogramStructures - code for describing pyrograms and how to compare them:
   * PyrogramComparer - how to compare pyrograms
   * Pyrogram - pyrogram object definition
* rawHandlers - code for displaying/comparing pyrograms from raw text files
* xmlHandlers - code for displaying/comparing pyrograms from xml files
