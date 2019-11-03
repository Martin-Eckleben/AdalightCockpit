package cockpit;

import processing.core.*;
import processing.serial.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Timer;
import java.util.TimerTask;

public class Adalight extends PApplet {

	private static final long serialVersionUID = -69884399989157722L;
	
	private Mode mode = Mode.OFF;

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		
		System.out.println("setting mode to "+mode);
		drawthread = null;
		timer.cancel();
		
		this.mode = mode;
		if(mode == Mode.OFF)
			this.draw_off();
		else if(mode == Mode.Adalight || mode == Mode.Colorswirl) {
			startDrawthread();
		}
			
	}
	
	// this is for single color draw only
	// we dont want that to be done the whole time but just sometimes so the lights dont die :)
	Timer timer = null;
	public void setMode(Color c) {
		
		drawthread = null;
		
		this.mode = Mode.SingleColor;
		this.singleColor = c;
		this.draw_singleColor();
		
		if(this.timer!=null)
			this.timer.cancel();
		
		this.timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				draw_singleColor();
			}
		}, 0, 5000);
	}
	
	// the draw thread for the more dynamic drawing modes
	private Thread drawthread = null;
	private void startDrawthread(){
		drawthread = new Thread() {
	        @Override public void run() {
	        	Thread thisThread = Thread.currentThread();
	        	while (drawthread == thisThread) {
	        		if(mode == Mode.Adalight)
	        			draw_adalight();
	        		else if(mode == Mode.Colorswirl)
	    				draw_colorswirl();
	        	}
	        }
	    };
	    drawthread.start();
	}
	
	// CONFIGURABLE PROGRAM CONSTANTS --------------------------------------------

	// Minimum LED brightness; some users prefer a small amount of backlighting
	// at all times, regardless of screen content.  Higher values are brighter,
	// or set to 0 to disable this feature.

	static final short minBrightness = 120;

	// LED transition speed; it's sometimes distracting if LEDs instantaneously
	// track screen contents (such as during bright flashing sequences), so this
	// feature enables a gradual fade to each new LED state.  Higher numbers yield
	// slower transitions (max of 255), or set to 0 to disable this feature
	// (immediate transition of all LEDs).

	static final short fade = 75;

	// Pixel size for the live preview image.

	static final int pixelSize = 50;

	// Depending on many factors, it may be faster either to capture full
	// screens and process only the pixels needed, or to capture multiple
	// smaller sub-blocks bounding each region to be processed.  Try both,
	// look at the reported frame rates in the Processing output console,
	// and run with whichever works best for you.

	static final boolean useFullScreenCaps = true;

	// Serial device timeout (in milliseconds), for locating Arduino device
	// running the corresponding LEDstream code.  See notes later in the code...
	// in some situations you may want to entirely comment out that block.

	static final int timeout = 5000; // 5 seconds

	// PER-DISPLAY INFORMATION ---------------------------------------------------

	// This array contains details for each display that the software will
	// process.  If you have screen(s) attached that are not among those being
	// "Adalighted," they should not be in this list.  Each triplet in this
	// array represents one display.  The first number is the system screen
	// number...typically the "primary" display on most systems is identified
	// as screen #1, but since arrays are indexed from zero, use 0 to indicate
	// the first screen, 1 to indicate the second screen, and so forth.  This
	// is the ONLY place system screen numbers are used...ANY subsequent
	// references to displays are an index into this list, NOT necessarily the
	// same as the system screen number.  For example, if you have a three-
	// screen setup and are illuminating only the third display, use '2' for
	// the screen number here...and then, in subsequent section, '0' will be
	// used to refer to the first/only display in this list.
	// The second and third numbers of each triplet represent the width and
	// height of a grid of LED pixels attached to the perimeter of this display.
	// For example, '9,6' = 9 LEDs across, 6 LEDs down.

	static final int displays[][] = new int[][] {
		//		{0,9,6} // Screen 0, 9 LEDs across, 6 LEDs down
		{0,17,10} 
	};

	// PER-LED INFORMATION -------------------------------------------------------

	// This array contains the 2D coordinates corresponding to each pixel in the
	// LED strand, in the order that they're connected (i.e. the first element
	// here belongs to the first LED in the strand, second element is the second
	// LED, and so forth).  Each triplet in this array consists of a display
	// number (an index into the display array above, NOT necessarily the same as
	// the system screen number) and an X and Y coordinate specified in the grid
	// units given for that display.  {0,0,0} is the top-left corner of the first
	// display in the array.
	// For our example purposes, the coordinate list below forms a ring around
	// the perimeter of a single screen, with a one pixel gap at the bottom to
	// accommodate a monitor stand.  Modify this to match your own setup:

	static final int leds[][] = new int[][] {    
        // view to front of the screen (adalight mirrored)
        {0,8,9},{0,9,9},{0,10,9},{0,11,9},{0,12,9},{0,13,9},{0,14,9},{0,15,9},{0,16,9}, // down middle to down right
        {0,16,8},{0,16,7},{0,16,6},{0,16,5},{0,16,4},{0,16,3},{0,16,2},{0,16,1},{0,16,0}, // down right to top right
        {0,15,0},{0,14,0},{0,13,0},{0,12,0},{0,11,0},{0,10,0},{0,9,0},{0,8,0},{0,7,0},{0,6,0},{0,5,0},{0,4,0},{0,3,0},{0,2,0},{0,1,0},{0,0,0}, // top right to top left
        {0,0,1},{0,0,2},{0,0,3},{0,0,4},{0,0,5},{0,0,6},{0,0,7},{0,0,8},{0,0,9}, // top left to top down
        {0,1,9},{0,2,9},{0,3,9},{0,4,9},{0,5,9},{0,6,9},{0,7,9} // down left to down middle
    };
	
	//	static final int leds[][] = new int[][] {
	//		  {0,3,5}, {0,2,5}, {0,1,5}, {0,0,5}, // Bottom edge, left half
	//		  {0,0,4}, {0,0,3}, {0,0,2}, {0,0,1}, // Left edge
	//		  {0,0,0}, {0,1,0}, {0,2,0}, {0,3,0}, {0,4,0}, // Top edge
	//		           {0,5,0}, {0,6,0}, {0,7,0}, {0,8,0}, // More top edge
	//		  {0,8,1}, {0,8,2}, {0,8,3}, {0,8,4}, // Right edge
	//		  {0,8,5}, {0,7,5}, {0,6,5}, {0,5,5}  // Bottom edge, right half
	//
	//		/* Hypothetical second display has the same arrangement as the first.
	//		   But you might not want both displays completely ringed with LEDs;
	//		   the screens might be positioned where they share an edge in common.
	//		 ,{1,3,5}, {1,2,5}, {1,1,5}, {1,0,5}, // Bottom edge, left half
	//		  {1,0,4}, {1,0,3}, {1,0,2}, {1,0,1}, // Left edge
	//		  {1,0,0}, {1,1,0}, {1,2,0}, {1,3,0}, {1,4,0}, // Top edge
	//		           {1,5,0}, {1,6,0}, {1,7,0}, {1,8,0}, // More top edge
	//		  {1,8,1}, {1,8,2}, {1,8,3}, {1,8,4}, // Right edge
	//		  {1,8,5}, {1,7,5}, {1,6,5}, {1,5,5}  // Bottom edge, right half
	//		*/
	//	};
    
	// GLOBAL VARIABLES ---- You probably won't need to modify any of this -------

    private byte[]           serialData  = new byte[6 + leds.length * 3];
	private short[][]        ledColor    = new short[leds.length][3],
	                 prevColor   = new short[leds.length][3];
	private byte[][]         gamma       = new byte[256][3];
	private int              nDisplays   = displays.length;
	private Robot[]          bot         = new Robot[displays.length];
	private Rectangle[]      dispBounds  = new Rectangle[displays.length],
	                 ledBounds;  // Alloc'd only if per-LED captures
	private int[][]          pixelOffset = new int[leds.length][256],
	                 screenData; // Alloc'd only if full-screen captures
	private Serial           port;

	private Color			 singleColor = new Color(0, 0, 0);
	
	// INITIALIZATION ------------------------------------------------------------

	public Adalight() {
		
	  GraphicsEnvironment     ge;
	  GraphicsConfiguration[] gc;
	  GraphicsDevice[]        gd;
	  int                     d, i, row, col;
	  int[]                   x = new int[16], y = new int[16];
	  double                   f, range, step, start;

	  // Open serial port.  As written here, this assumes the Arduino is the
	  // first/only serial device on the system.  If that's not the case,
	  // change "Serial.list()[0]" to the name of the port to be used:
	  port = new Serial(this, Serial.list()[0], 115200);
	  // Alternately, in certain situations the following line can be used
	  // to detect the Arduino automatically.  But this works ONLY with SOME
	  // Arduino boards and versions of Processing!  This is so convoluted
	  // to explain, it's easier just to test it yourself and see whether
	  // it works...if not, leave it commented out and use the prior port-
	  // opening technique.
	  // port = openPort();
	  // And finally, to test the software alone without an Arduino connected,
	  // don't open a port...just comment out the serial lines above.

	  // Initialize screen capture code for each display's dimensions.
	  dispBounds = new Rectangle[displays.length];
	  if(useFullScreenCaps) {
	    screenData = new int[displays.length][];
	    // ledBounds[] not used
	  } else {
	    ledBounds  = new Rectangle[leds.length];
	    // screenData[][] not used
	  }
	  ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	  gd = ge.getScreenDevices();
	  if(nDisplays > gd.length) nDisplays = gd.length;
	  for(d=0; d<nDisplays; d++) { // For each display...
	    try {
	      bot[d] = new Robot(gd[displays[d][0]]);
	    }
	    catch(AWTException e) {
	      System.out.println("new Robot() failed");
	      continue;
	    }
	    gc              = gd[displays[d][0]].getConfigurations();
	    dispBounds[d]   = gc[0].getBounds();
	    dispBounds[d].x = dispBounds[d].y = 0;
	  }

	  // Precompute locations of every pixel to read when downsampling.
	  // Saves a bunch of math on each frame, at the expense of a chunk
	  // of RAM.  Number of samples is now fixed at 256; this allows for
	  // some crazy optimizations in the downsampling code.
	  for(i=0; i<leds.length; i++) { // For each LED...
	    d = leds[i][0]; // Corresponding display index

	    // Precompute columns, rows of each sampled point for this LED
	    range = (float)dispBounds[d].width / (float)displays[d][1];
	    step  = range / 16.0;
	    start = range * (float)leds[i][1] + step * 0.5;
	    for(col=0; col<16; col++) x[col] = (int)(start + step * (float)col);
	    range = (float)dispBounds[d].height / (float)displays[d][2];
	    step  = range / 16.0;
	    start = range * (float)leds[i][2] + step * 0.5;
	    for(row=0; row<16; row++) y[row] = (int)(start + step * (float)row);

	    if(useFullScreenCaps) {
	      // Get offset to each pixel within full screen capture
	      for(row=0; row<16; row++) {
	        for(col=0; col<16; col++) {
	          pixelOffset[i][row * 16 + col] =
	            y[row] * dispBounds[d].width + x[col];
	        }
	      }
	    } else {
	      // Calc min bounding rect for LED, get offset to each pixel within
	      ledBounds[i] = new Rectangle(x[0], y[0], x[15]-x[0]+1, y[15]-y[0]+1);
	      for(row=0; row<16; row++) {
	        for(col=0; col<16; col++) {
	          pixelOffset[i][row * 16 + col] =
	            (y[row] - y[0]) * ledBounds[i].width + x[col] - x[0];
	        }
	      }
	    }
	  }

	  for(i=0; i<prevColor.length; i++) {
	    prevColor[i][0] = prevColor[i][1] = prevColor[i][2] =
	      minBrightness / 3;
	  }

	  // A special header / magic word is expected by the corresponding LED
	  // streaming code running on the Arduino.  This only needs to be initialized
	  // once (not in draw() loop) because the number of LEDs remains constant:
	  serialData[0] = 'A';                              // Magic word
	  serialData[1] = 'd';
	  serialData[2] = 'a';
	  serialData[3] = (byte)((leds.length - 1) >> 8);   // LED count high byte
	  serialData[4] = (byte)((leds.length - 1) & 0xff); // LED count low byte
	  serialData[5] = (byte)(serialData[3] ^ serialData[4] ^ 0x55); // Checksum

	  // Pre-compute gamma correction table for LED brightness levels:
	  for(i=0; i<256; i++) {
	    f           = pow((float)(i/255.0),(float)2.8);
	    gamma[i][0] = (byte)(f * 255.0);
	    gamma[i][1] = (byte)(f * 240.0);
	    gamma[i][2] = (byte)(f * 220.0);
	  }
	}

	public void draw_singleColor() {
		int j = 6;          // Serial led data follows header / magic word
		for(int i=0; i<leds.length; i++) {  // For each LED...
			serialData[j++]  = (byte)this.singleColor.getRed();
			serialData[j++]  = (byte)this.singleColor.getGreen();
			serialData[j++]  = (byte)this.singleColor.getBlue();
		}
		if(port != null) port.write(serialData); // Issue data to Arduino
	}
	
	// colorswirl vars
	private float sine1 = 0.0f;
	private int hue1  = 0;
	private float sine2 = sine1;
	private int hue2  = hue1;
	int    bright, lo, r, g, b, t, prev, frame = 0;
	
	private void draw_colorswirl() {
		
	    // Start at position 6, after the LED header/magic word
	    for (int i = 6; i < serialData.length; ) {
	      // Fixed-point hue-to-RGB conversion.  'hue2' is an integer in the
	      // range of 0 to 1535, where 0 = red, 256 = yellow, 512 = green, etc.
	      // The high byte (0-5) corresponds to the sextant within the color
	      // wheel, while the low byte (0-255) is the fractional part between
	      // the primary/secondary colors.
	      lo = hue2 & 255;
	      switch((hue2 >> 8) % 6) {
	      case 0:
	        r = 255;
	        g = lo;
	        b = 0;
	        break;
	      case 1:
	        r = 255 - lo;
	        g = 255;
	        b = 0;
	        break;
	      case 2:
	        r = 0;
	        g = 255;
	        b = lo;
	        break;
	      case 3:
	        r = 0;
	        g = 255 - lo;
	        b = 255;
	        break;
	      case 4:
	        r = lo;
	        g = 0;
	        b = 255;
	        break;
	      default:
	        r = 255;
	        g = 0;
	        b = 255 - lo;
	        break;
	      }

	      // Resulting hue is multiplied by brightness in the range of 0 to 255
	      // (0 = off, 255 = brightest).  Gamma corrrection (the 'pow' function
	      // here) adjusts the brightness to be more perceptually linear.
	      bright      = (int) Math.round( pow((float)(0.5 + sin(sine2) * 0.5), 2.8f) * 255.0 );
	      serialData[i++] = (byte)((r * bright) / 255);
	      serialData[i++] = (byte)((g * bright) / 255);
	      serialData[i++] = (byte)((b * bright) / 255);

	      // Each pixel is slightly offset in both hue and brightness
	      hue2  += 40;
	      sine2 += 0.3;
	    }

	    // Slowly rotate hue and brightness in opposite directions
	    hue1   = (hue1 + 4) % 1536;
	    sine1 -= .03;

	    this.port.write(serialData);
	    try {
			drawthread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void draw_adalight() {
		BufferedImage img;
		int           d, i, j, o, c, weight, rb, g, sum, deficit, s2;
		int[]         pxls, offs;

		  if(useFullScreenCaps) {
		    // Capture each screen in the displays array.
		    for(d=0; d<nDisplays; d++) {
		      img = bot[d].createScreenCapture(dispBounds[d]);
		      // Get location of source pixel data
		      screenData[d] =
		        ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		    }
		  }

		  weight = 257 - fade; // 'Weighting factor' for new frame vs. old
		  j      = 6;          // Serial led data follows header / magic word

		  // This computes a single pixel value filtered down from a rectangular
		  // section of the screen.  While it would seem tempting to use the native
		  // image scaling in Processing/Java, in practice this didn't look very
		  // good -- either too pixelated or too blurry, no happy medium.  So
		  // instead, a "manual" downsampling is done here.  In the interest of
		  // speed, it doesn't actually sample every pixel within a block, just
		  // a selection of 256 pixels spaced within the block...the results still
		  // look reasonably smooth and are handled quickly enough for video.

		  for(i=0; i<leds.length; i++) {  // For each LED...
		    d = leds[i][0]; // Corresponding display index
		    if(useFullScreenCaps) {
		      // Get location of source data from prior full-screen capture:
		      pxls = screenData[d];
		    } else {
		      // Capture section of screen (LED bounds rect) and locate data::
		      img  = bot[d].createScreenCapture(ledBounds[i]);
		      pxls = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		    }
		    offs = pixelOffset[i];
		    rb = g = 0;
		    for(o=0; o<256; o++) {
		      c   = pxls[offs[o]];
		      rb += c & 0x00ff00ff; // Bit trickery: R+B can accumulate in one var
		      g  += c & 0x0000ff00;
		    }

		    // Blend new pixel value with the value from the prior frame
		    ledColor[i][0]  = (short)((((rb >> 24) & 0xff) * weight +
		                               prevColor[i][0]     * fade) >> 8);
		    ledColor[i][1]  = (short)(((( g >> 16) & 0xff) * weight +
		                               prevColor[i][1]     * fade) >> 8);
		    ledColor[i][2]  = (short)((((rb >>  8) & 0xff) * weight +
		                               prevColor[i][2]     * fade) >> 8);

		    // Boost pixels that fall below the minimum brightness
		    sum = ledColor[i][0] + ledColor[i][1] + ledColor[i][2];
		    if(sum < minBrightness) {
		      if(sum == 0) { // To avoid divide-by-zero
		        deficit = minBrightness / 3; // Spread equally to R,G,B
		        ledColor[i][0] += deficit;
		        ledColor[i][1] += deficit;
		        ledColor[i][2] += deficit;
		      } else {
		        deficit = minBrightness - sum;
		        s2      = sum * 2;
		        // Spread the "brightness deficit" back into R,G,B in proportion to
		        // their individual contribition to that deficit.  Rather than simply
		        // boosting all pixels at the low end, this allows deep (but saturated)
		        // colors to stay saturated...they don't "pink out."
		        ledColor[i][0] += deficit * (sum - ledColor[i][0]) / s2;
		        ledColor[i][1] += deficit * (sum - ledColor[i][1]) / s2;
		        ledColor[i][2] += deficit * (sum - ledColor[i][2]) / s2;
		      }
		    }

		    // Apply gamma curve and place in serial output buffer
		    serialData[j++] = gamma[ledColor[i][0]][0];
		    serialData[j++] = gamma[ledColor[i][1]][1];
		    serialData[j++] = gamma[ledColor[i][2]][2];
		  }

		  if(port != null) port.write(serialData); // Issue data to Arduino

		  // Copy LED color data to prior frame array for next pass
		  arraycopy(ledColor, 0, prevColor, 0, ledColor.length);
	}
	
	private void draw_off() {
		int j = 6;          // Serial led data follows header / magic word
		for(int i=0; i<leds.length; i++) {  // For each LED...
			serialData[j++]  = 0;
			serialData[j++]  = 0;
			serialData[j++]  = 0;
		}
		if(port != null) port.write(serialData); // Issue data to Arduino
	}
}
