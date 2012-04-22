Haxe to Java Extern Creator
===========================

A java program which parses folders of Java .class files, and exports a folder containing
a list of [Haxe] (http://haxe.org/) external definitions. 

Useage
-------------

The program can be run using `java -jar haxe-extern.jar (input-directory) (output-directory)`.
This will recursively find class files within `(input-directory)`, and create externs
in `(output-directory)`. 

> (**Note:** the output directory will be structured based on the class
> hierarchy of class files in the input directory. Also, all existing files that match classes in the input
> directory *will be overwritten*)