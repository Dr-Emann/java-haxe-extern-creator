Haxe to Java Extern Creator
===========================

A java program which parses folders of Java .class files, and exports a folder containing
a list of [Haxe] (http://haxe.org/) external definitions. 

Useage
-------------

The program can be run using `java -jar haxe-extern.jar (input-directory) (output-directory)` or
`java -jar haxe-extern.jar (input-jar) (output-directory)`.
This will recursively find class files within `(input-directory)`, and create externs
in `(output-directory)`. 

> (**Note:** the output directory will be structured based on the class
> hierarchy of class files in the input directory. Also, all existing files that match classes in the input
> directory *will be overwritten*)

Example
-------------

The following is example output for [android.widget.Toast] (http://developer.android.com/reference/android/widget/Toast.html). 

````Haxe
package android.widget;

import java.StdTypes;
extern class Toast
{
	static public var LENGTH_SHORT:Int;
	static public var LENGTH_LONG:Int;

	public function setView(arg0:android.view.View):Void;

	public function setGravity(arg0:Int,arg1:Int,arg2:Int):Void;

	public function setMargin(arg0:Float,arg1:Float):Void;

	public function getGravity():Int;

	public function getHorizontalMargin():Float;

	@:overload(function(arg0:android.content.Context,arg1:java.lang.CharSequence,arg2:Int):android.widget.Toast{})
	static public function makeText(arg0:android.content.Context,arg1:Int,arg2:Int):android.widget.Toast;

	public function show():Void;

	public function cancel():Void;

	@:overload(function(arg0:Int):Void{})
	public function setText(arg0:java.lang.CharSequence):Void;

	public function getYOffset():Int;

	public function getView():android.view.View;

	public function getDuration():Int;

	public function setDuration(arg0:Int):Void;

	public function new(arg0:android.content.Context):Void;

	public function getXOffset():Int;

	public function getVerticalMargin():Float;
}
````