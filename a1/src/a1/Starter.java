package a1;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import java.awt.BorderLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.GLContext;
import com.jogamp.common.nio.Buffers;

import com.jogamp.opengl.util.*;
import graphicslib3D.*;

import java.lang.Math.*;

public class Starter extends JFrame implements GLEventListener, MouseWheelListener
{
	// Buttons for UI
	private JButton circleButton, upDownButton, resetButton;
	
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private GLSLUtils util = new GLSLUtils();
	
	// States altered by commands
	private int inColor = 0;
	private boolean upDown = false;
	private boolean circle = false;
	
	private float s_all = 1.0f; // Scale
	private Vec3 pos = new Vec3(0.0f, 0.0f, 0.0f); // Pos of triangle passed to shader
	private float inc = 0.01f; // Up/Down inc
	private float angularInc = 2; // Speed of circluar movement
	private float radius = .75f; // Size of circle movement
	private float angle = 0; // Actual angle used in circular movement calculation

	public Starter()
	{	setTitle("Chase Jones - Assignment #1");
		setSize(800, 600);
		myCanvas = new GLCanvas(); // Create frame
		myCanvas.addGLEventListener(this);
		
		// Layout
		JPanel topPanel = new JPanel();
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(myCanvas, BorderLayout.CENTER);
		
		////////////
		// Buttons
		////////////
		upDownButton = new JButton("Up/Down");
		topPanel.add(upDownButton);
		CustomCommand upDownCommand = new CustomCommand("Up/Down", "Moves triangle up and down", this);
		upDownButton.setAction(upDownCommand);
		
		circleButton = new JButton("Circle");
		topPanel.add(circleButton);
		CustomCommand circleCommand = new CustomCommand("Circle", "Moves triangle in a circle", this);
		circleButton.setAction(circleCommand);
		
		resetButton = new JButton("Reset");
		topPanel.add(resetButton);
		CustomCommand resetCommand = new CustomCommand("Reset", "Resets triangle", this);
		resetButton.setAction(resetCommand);
		
		///////////
		// Listeners
		///////////
		
		// Mouse
		this.addMouseWheelListener(this);
		
		// Keys
		// c-key
		CustomCommand cCommand = new CustomCommand("c", "Toggles color of triangle", this); // create command
		JComponent contentPane = (JComponent) this.getContentPane();
		// get input map for content pane
		int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
		InputMap imap = contentPane.getInputMap(mapName);
		// create keystroke
		KeyStroke cKey = KeyStroke.getKeyStroke('c');
		// put keystroke in inputmap with label "color"
		imap.put(cKey, "color");
		// get actionmap for content pane and put command into action map with label "color"
		ActionMap amap = contentPane.getActionMap();
		amap.put("color", cCommand);
		this.requestFocus();
		///////////
		
		
		///////////
		// Set Active Pane
		///////////
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}
	
	public void CommandUpdate(String name) {
		switch(name) { // up/Down and Circle are mutually exclusive
			case "Up/Down":	// Start moving up/down
				circle = false;
				upDown = true;
				System.out.format("Up/Down State: %b%n", upDown);
				System.out.format("Circle State: %b%n%n", circle);
				pos = new Vec3(0,0,0); // reset position
				break;
			case "Circle": // Start moving in circle
				upDown = false;
				circle = true;
				System.out.format("Up/Down State: %b%n", upDown);
				System.out.format("Circle State: %b%n%n", circle);
				pos = new Vec3(0,0,0); // reset position
				break;
			case "Reset": // Reset position to center and stop moving
				circle = false;
				upDown = false;
				System.out.format("Up/Down State: %b%n", upDown);
				System.out.format("Circle State: %b%n%n", circle);
				pos = new Vec3(0,0,0);
				break;
			case "c":	// Toggle color
				inColor = ~inColor; // Change to non-zero or zero
				System.out.println("In Color? :" + inColor);
		}
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);
		
		// Set Background buffer color
		float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		if(upDown == true) {// If moving up/down, change offsets
			pos.y(pos.y()+inc);
			if(pos.y() > 1.0f) inc = -inc;
			if(pos.y() < -1.0f) inc = -inc;
		}
		
		if(circle == true) {	// If moving in circle, change offsets
			angle += angularInc;
			pos.x(radius*((float)Math.cos(Math.toRadians(angle))));
			pos.y(radius*((float)Math.sin(Math.toRadians(angle))));
			System.out.format("x: %f	y: %f%n%n", pos.x(),pos.y());
		}
		
		// Change color at runtime
		int offset_color = gl.glGetUniformLocation(rendering_program, "inColor");
		gl.glProgramUniform1i(rendering_program, offset_color, inColor);
		// Change scale at runtime
		int offset_scale_all = gl.glGetUniformLocation(rendering_program, "s_all");
		gl.glProgramUniform1f(rendering_program, offset_scale_all, s_all);
		// Change x/y offsets at runtime
		int offset_loc_x = gl.glGetUniformLocation(rendering_program, "xinc");
		gl.glProgramUniform1f(rendering_program, offset_loc_x, pos.x());
		int offset_loc_y = gl.glGetUniformLocation(rendering_program, "yinc");
		
		// Draw it
		gl.glProgramUniform1f(rendering_program, offset_loc_y, pos.y());
		gl.glDrawArrays(GL_TRIANGLES,0,3);
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		System.out.println("OpenGL Ver: "+gl.glGetString(GL_VERSION)); // Print version of OpenGL
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}
	
	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		// Read shaders from files
		String vshaderSource[] = util.readShaderSource("a1/vert.shader");
		String fshaderSource[] = util.readShaderSource("a1/frag.shader");

		// Create vertex shader
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glCompileShader(vShader);
		
		// Check for compilation errors
		checkOpenGLError();  // can use returned boolean if desired
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] == 1)
		{	System.out.println("vertex compilation success");
		} else
		{	System.out.println("vertex compilation failed");
			printShaderLog(vShader);
		}
		
		// Create fragment shader
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
		gl.glCompileShader(fShader);
		
		// Check for compilation errors
		checkOpenGLError();  // can use returned boolean if desired
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] == 1)
		{	System.out.println("fragment compilation success");
		} else
		{	System.out.println("fragment compilation failed");
			printShaderLog(fShader);
		}
		
		// create OpenGL program with attached shaders
		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		
		// Check for link errors
		checkOpenGLError();
		gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] == 1)
		{	System.out.println("linking succeeded");
		} else
		{	System.out.println("linking failed");
			printProgramLog(vfprogram);
		}
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		return vfprogram;
	}
	
	////////////
	// OpenGL methods
	////////////
	private void printShaderLog(int shader)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}
	
	void printProgramLog(int prog)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine length of the program compilation log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}

	// Check if OpenGL has thrown an error;
	// Use after you make a major OpenGL call, like linking or compiling
	private boolean checkOpenGLError() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR)
		{	System.err.println("glError: " + glu.gluErrorString(glErr));
		foundError = true;
		glErr = gl.glGetError();
		}
		return foundError;
	}
	
	// Main
	public static void main(String[] args) 
	{ 
		printVersions(); // Print JOGL and Java versions
		Starter scene = new Starter();
		scene.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	// Prints versions of Java and JOGL
	public static void printVersions() {
		System.out.println("JOGL Ver: "+Package.getPackage("com.jogamp.opengl").toString()
				+"\nJava Ver: "+System.getProperty("java.version"));
	}
	
	// Not implemented yet
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getWheelRotation() < 0) // If scrolling up, shrink scale
			s_all /= 1.1;
		else s_all *=1.1;			// If scrolling down, enlarge scale
		System.out.println("Scale factor: " + s_all);
	}

}
