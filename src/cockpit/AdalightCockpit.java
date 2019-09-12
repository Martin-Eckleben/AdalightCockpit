package cockpit;

import processing.core.*;
import processing.serial.*;
import java.awt.*;
import java.awt.image.*;

public class AdalightCockpit extends PApplet {

	private static final long serialVersionUID = -69884399989157722L;

	Integer mode = 0;
	
	//lights
	int N_LEDS = 50;
	Serial myPort = null;
	
	//colors
	int[] blue_c		= new int[]{0,0,255};
	int[] red_c			= new int[]{255,0,0};
	int[] green_c		= new int[]{0,255,0};
	int[] pink_c		= new int[]{200,0,195};
	int[] purple_c		= new int[]{60,0,255};
	int[] lime_c		= new int[]{100,255,0};
	int[] orange_c		= new int[]{255,160,0};
	int[] brightblue_c	= new int[]{0,140,255};
	int[] off_c			= new int[]{0,0,0};
	
	//buttons
	int btn_height = 50;
	int btn_width = 160;
	int abstand = 10;
	String[] btns = new String[]{
		"Off","Equalizer","Adalight","Colorswirl","Red","Green","Blue","Pink","Purple","Lime","Orange",
		"BrightBlue"
	};
	static final int off=0;
	static final int equalizer=1;
	static final int adalight=2;
	static final int colorswirl=3;
	static final int red=4;
	static final int green=5;
	static final int blue=6;
	static final int pink=7;
	static final int purple=8;
	static final int lime=9;
	static final int orange=10;
	static final int brightblue=11;
	
	//colorswirl
	int hue1=0;
	float sine1=0;
    int hue2;
	int bright;
	int lo; 
	int r;
	int g;
	int b;
	int t;
	float sine2;
	
	////////////////////////////////////////////////////////////
	// ADALIGHT
	////////////////////////////////////////////////////////////
	static final short minBrightness = 120;
	static final short fade = 120;
	static final int pixelSize = 20;
	static final boolean useFullScreenCaps = true;
	static final int timeout = 5000;
	
	byte[]		serialData  = new byte[6 + leds.length * 3];
	short[][]	ledColor    = new short[leds.length][3];
	short[][]	prevColor   = new short[leds.length][3];
	byte[][]	gamma       = new byte[256][3];
	int			nDisplays   = displays.length;
	Robot[]		bot         = new Robot[displays.length];
	Rectangle[]	dispBounds  = new Rectangle[displays.length];
	Rectangle[] ledBounds;
	int[][]		pixelOffset = new int[leds.length][256];
	int[][]		screenData;
	PImage[]	preview     = new PImage[displays.length];
	DisposeHandler dh;
	
	static final int displays[][] = new int[][] {
		{0,17,10}
	};

	static final int leds[][] = new int[][] {
		
		//sicht von vorne auf den monitor (adalight umgedreht)
		{0,8,9},{0,9,9},{0,10,9},{0,11,9},{0,12,9},{0,13,9},{0,14,9},{0,15,9},{0,16,9}, //unten,Mitte nach rechts
		{0,16,8},{0,16,7},{0,16,6},{0,16,5},{0,16,4},{0,16,3},{0,16,2},{0,16,1},{0,16,0}, //unten rechts nach oben
		{0,15,0},{0,14,0},{0,13,0},{0,12,0},{0,11,0},{0,10,0},{0,9,0},{0,8,0},{0,7,0},{0,6,0},{0,5,0},{0,4,0},{0,3,0},{0,2,0},{0,1,0},{0,0,0}, //oben rechts nach links
		{0,0,1},{0,0,2},{0,0,3},{0,0,4},{0,0,5},{0,0,6},{0,0,7},{0,0,8},{0,0,9}, //oben links nach unten
		{0,1,9},{0,2,9},{0,3,9},{0,4,9},{0,5,9},{0,6,9},{0,7,9} //unten links nach rechts (mitte)
	};
	
	public void setup() {
		
		//get COM port
//		String s=""; 
//		try {
//			int bytesRead=0;
//			InputStream is = getClass().getResourceAsStream("COMport.txt");
//			BufferedInputStream bi = new BufferedInputStream(is);
//			byte[] contents = new byte[1024];
//			
//			while( (bytesRead = bi.read(contents)) != -1){ 
//				s = new String(contents, 0, bytesRead);               
//			}
//			bi.close();
//			is.close();
//		} catch (IOException e1) {
//			System.err.println("ERROR while opening internal COM Port conf file");
//			System.err.println(e1.getLocalizedMessage());
//			System.exit(1);
//		}
//		System.out.print(s);
		
		//open serial communication
	    myPort = new Serial(this, "COM3", 115200);
		
		size(btn_width+2*abstand,(btns.length*btn_height)+((btns.length+1)*abstand));
		
		//serial
		serialData[0] = 'A';                                			// Magic word
		serialData[1] = 'd';
		serialData[2] = 'a';
		serialData[3] = (byte)((N_LEDS - 1) >> 8);            			// LED count high byte
		serialData[4] = (byte)((N_LEDS - 1) & 0xff);          			// LED count low byte
		serialData[5] = (byte)(serialData[3] ^ serialData[4] ^ 0x55); 	// Checksum
		
		////////////////////////////////////////////////////////////
		// ADALIGHT
		////////////////////////////////////////////////////////////
		GraphicsEnvironment     ge;
		GraphicsConfiguration[] gc;
		GraphicsDevice[]        gd;
		int                     d, i, maxHeight, row, col;
		int[]                   x = new int[16], y = new int[16];
		float                   f, range, step, start;

		dh = new DisposeHandler(this);

		// Initialize screen capture code for each display's dimensions.
		dispBounds = new Rectangle[displays.length];

		if(useFullScreenCaps) screenData = new int[displays.length][];
		else ledBounds  = new Rectangle[leds.length];

		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getScreenDevices();

		if(nDisplays > gd.length) nDisplays = gd.length;

		maxHeight = 0;
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
			preview[d]      = createImage(displays[d][1], displays[d][2], RGB);
			preview[d].loadPixels();
			if(displays[d][2] > maxHeight) maxHeight = displays[d][2];
		}

		for(i=0; i<leds.length; i++) {
			d = leds[i][0];

			// Precompute columns, rows of each sampled point for this LED
			range = (float)dispBounds[d].width / (float)displays[d][1];
			step  = range / 16.0f;
			start = range * (float)leds[i][1] + step * 0.5f;
			for(col=0; col<16; col++) x[col] = (int)(start + step * (float)col);
			range = (float)dispBounds[d].height / (float)displays[d][2];
			step  = range / 16.0f;
			start = range * (float)leds[i][2] + step * 0.5f;
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

		// Pre-compute gamma correction table for LED brightness levels:
		for(i=0; i<256; i++) {
			f           = pow((float)i / 255.0f, 2.8f);
			gamma[i][0] = (byte)(f * 255.0);
			gamma[i][1] = (byte)(f * 240.0);
			gamma[i][2] = (byte)(f * 220.0);
		}
		
		///////////////////////////////////////
		//MENU
		///////////////////////////////////////
		background(255);
		stroke(0);
		
		//draw btns
		PFont font = createFont("Jokerman",16,true);
		textFont(font,20);
		int temp_y=abstand;
		for(i=0;i<btns.length;i++){
			fill(255);
			rect(abstand,temp_y,btn_width,btn_height);
			
			//textcolors
			
			if(btns[i]=="Blue") fill(blue_c[0],blue_c[1],blue_c[2]);
			else if(btns[i]=="Red") fill(red_c[0],red_c[1],red_c[2]);
			else if(btns[i]=="Green") fill(green_c[0],green_c[1],green_c[2]);
			else if(btns[i]=="Pink") fill(pink_c[0],pink_c[1],pink_c[2]);
			else if(btns[i]=="Purple") fill(purple_c[0],purple_c[1],purple_c[2]);
			else if(btns[i]=="Orange") fill(orange_c[0],orange_c[1],orange_c[2]);
			else if(btns[i]=="Lime") fill(lime_c[0],lime_c[1],lime_c[2]);
			else if(btns[i]=="BrightBlue") fill(brightblue_c[0],brightblue_c[1],brightblue_c[2]);
			
			else fill(0);
			
			text(btns[i],abstand+10,temp_y+btn_height-10);
			
			
			temp_y+=btn_height;
			temp_y+=abstand;
		}
	}
	
	public void draw () {
		
		//if colorswirl
		if(mode==colorswirl){	
			
			sine2 = sine1;
			hue2  = hue1;

			for (int i = 6; i < serialData.length; ) {
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

				bright      = (int)(pow(0.5f + sin(sine2) * 0.5f, 2.8f) * 255.0);

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

			if(myPort != null) myPort.write(serialData);
		}
		
		//if adalight
		else if(mode==adalight){

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
						deficit = minBrightness / 3;
						ledColor[i][0] += deficit;
						ledColor[i][1] += deficit;
						ledColor[i][2] += deficit;
					} else {
						deficit = minBrightness - sum;
						s2      = sum * 2;
						
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

			if(myPort != null) myPort.write(serialData); // Issue data to Arduino

			//DEBUG
			//println(frameRate); // How are we doing?

			arraycopy(ledColor, 0, prevColor, 0, ledColor.length);
		}
		
		//moodlights
		else if(mode==blue){
			setColor(blue_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==red){
			setColor(red_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==off){
			setColor(off_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==green){
			setColor(green_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==pink){
			setColor(pink_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==purple){
			setColor(purple_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==orange){
			setColor(orange_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==lime){
			setColor(lime_c);
			if(myPort != null) myPort.write(serialData);
		}
		else if(mode==brightblue){
			setColor(brightblue_c);
			if(myPort != null) myPort.write(serialData);
		}
		
		
		//if equalizer
		else if(mode==equalizer){
			
		}
		
		//wenn maus gedr�ckt
		if(mousePressed){
			
			//und maus x einen btn trifft
			if(mouseX>abstand && mouseX<(btn_width+abstand)){
				
				//gehe �ber alle btns
				for(int index=0;index<btns.length;index++){
					//schaue ob maus darin liegt
					if(mouseY>(index*btn_height) + ((index+1)*abstand) 				//linke grenze
					&& mouseY<(((index+1)*btn_height) + ((index+1)*btn_height))    //untere grenze
						){
						//setze modus auf knopf der geklickt wurde
						mode=index;
					}
				}
			}
		}
	}

	public void setColor(int[] c){
		for (int i = 6; i < serialData.length; ) {      
			serialData[i++] = (byte)c[0];
			serialData[i++] = (byte)c[1];
			serialData[i++] = (byte)c[2];
		}
	}
	
	public class DisposeHandler {
		DisposeHandler(PApplet pa) {
			pa.registerDispose(this);
		}
		public void dispose() {
			// Fill serialData (after header) with 0's, and issue to Arduino...
			//	    Arrays.fill(serialData, 6, serialData.length, (byte)0);
			java.util.Arrays.fill(serialData, 6, serialData.length, (byte)0);
			if(myPort != null) myPort.write(serialData);
		}
	}
}
