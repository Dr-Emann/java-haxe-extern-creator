Haxe to Java Extern Creator
===========================

A java program which parses folders of Java .class files, and exports a folder containing
a list of [Haxe] (http://haxe.org/) external definitions. 

Useage
-------------

This program must be built to include tools.jar, which can be found at *jdk-loc/lib/tools.jar*

The program can be run using 
`java -jar (jar-file) -source (source-dir) -output (output-dir) -packages (package-list)`

This will create matching haxe externs in (output-dir) for each java class in
the specified packages, looking in (source-dir)

> **Note:** the output directory will be structured based on the class
> hierarchy of the java files input. Also, all existing files that match classes in the input
> directory *will be overwritten*

Example
-------------

The following is example output for [android.widget.Toast] (http://developer.android.com/reference/android/widget/Toast.html).

````Haxe
package android.widget;

@:native("android.widget.Toast")
/**
 * A toast is a view containing a quick little message for the user.  The toast class
 * helps you create and show those.
 * {@more}
 *
 * <p>
 * When the view is shown to the user, appears as a floating view over the
 * application.  It will never receive focus.  The user will probably be in the
 * middle of typing something else.  The idea is to be as unobtrusive as
 * possible, while still showing the user the information you want them to see.
 * Two examples are the volume control, and the brief message saying that your
 * settings have been saved.
 * <p>
 * The easiest way to use this class is to call one of the static methods that constructs
 * everything you need and returns a new Toast object.
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For information about creating Toast notifications, read the
 * <a href="{@docRoot}guide/topics/ui/notifiers/toasts.html">Toast Notifications</a> developer
 * guide.</p>
 * </div>
 */
extern class Toast 
{
/**
 * Show the view or text notification for a short period of time.  This time
 * could be user-definable.  This is the default.
 * @see #setDuration
 */
	inline static public var LENGTH_SHORT:Int = 0;
/**
 * Show the view or text notification for a long period of time.  This time
 * could be user-definable.
 * @see #setDuration
 */
	inline static public var LENGTH_LONG:Int = 1;
/**
 * Construct an empty Toast object.  You must call {@link #setView} before you
 * can call {@link #show}.
 *
 * @param context  The context to use.  Usually your {@link android.app.Application}
 *                 or {@link android.app.Activity} object.
 */
	public function new(context:android.content.Context);
/**
 * Set the view to show.
 * @see #getView
 */
	public function setView(view:android.view.View):Void;
/**
 * Set the location at which the notification should appear on the screen.
 * @see android.view.Gravity
 * @see #getGravity
 */
	public function setGravity(gravity:Int,xOffset:Int,yOffset:Int):Void;
/**
 * Set the margins of the view.
 *
 * @param horizontalMargin The horizontal margin, in percentage of the
 *        container width, between the container's edges and the
 *        notification
 * @param verticalMargin The vertical margin, in percentage of the
 *        container height, between the container's edges and the
 *        notification
 */
	public function setMargin(horizontalMargin:Float,verticalMargin:Float):Void;
/**
 * Get the location at which the notification should appear on the screen.
 * @see android.view.Gravity
 * @see #getGravity
 */
	public function getGravity():Int;
/**
 * Return the horizontal margin.
 */
	public function getHorizontalMargin():Float;
/**
 * Make a standard toast that just contains a text view with the text from a resource.
 *
 * @param context  The context to use.  Usually your {@link android.app.Application}
 *                 or {@link android.app.Activity} object.
 * @param resId    The resource id of the string resource to use.  Can be formatted text.
 * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
 *                 {@link #LENGTH_LONG}
 *
 * @throws Resources.NotFoundException if the resource can't be found.
 */
	@:overload(function (context:android.content.Context,resId:Int,duration:Int):android.widget.Toast {})
/**
 * Make a standard toast that just contains a text view.
 *
 * @param context  The context to use.  Usually your {@link android.app.Application}
 *                 or {@link android.app.Activity} object.
 * @param text     The text to show.  Can be formatted text.
 * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
 *                 {@link #LENGTH_LONG}
 */
	static public function makeText(context:android.content.Context,text:java.lang.CharSequence,duration:Int):android.widget.Toast;
/**
 * Show the view for the specified duration.
 */
	public function show():Void;
/**
 * Close the view if it's showing, or don't show it if it isn't showing yet.
 * You do not normally have to call this.  Normally view will disappear on its own
 * after the appropriate duration.
 */
	public function cancel():Void;
/**
 * Update the text in a Toast that was previously created using one of the makeText() methods.
 * @param resId The new text for the Toast.
 */
	@:overload(function (resId:Int):Void {})
/**
 * Update the text in a Toast that was previously created using one of the makeText() methods.
 * @param s The new text for the Toast.
 */
	public function setText(s:java.lang.CharSequence):Void;
/**
 * Return the view.
 * @see #setView
 */
	public function getView():android.view.View;
/**
 * Return the Y offset in pixels to apply to the gravity's location.
 */
	public function getYOffset():Int;
/**
 * Return the duration.
 * @see #setDuration
 */
	public function getDuration():Int;
/**
 * Set how long to show the view for.
 * @see #LENGTH_SHORT
 * @see #LENGTH_LONG
 */
	public function setDuration(duration:Int):Void;
/**
 * Return the X offset in pixels to apply to the gravity's location.
 */
	public function getXOffset():Int;
/**
 * Return the vertical margin.
 */
	public function getVerticalMargin():Float;

}
````