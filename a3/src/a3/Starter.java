package a3;

import materials.*;
import objects.*;

import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.*;

import java.awt.BorderLayout;
import java.io.File;
import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_VERSION;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.common.nio.Buffers;

public class Starter extends JFrame implements GLEventListener
{	private Camera camera = new Camera();
	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] vShader1Source, vShader2Source, fShader2Source, vShader3Source, fShader3Source, vShader4Source, fShader4Source, vShader5Source, fShader5Source;
	private int rendering_program1, rendering_program2, rendering_program3, rendering_program4, rendering_program5;
	private int vao[] = new int[1];
	private int vbo[] = new int[15];
	private int mv_location, proj_location, vertexLoc, n_location;
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	// time steps
	private long currentTime, prevCurrentTime;
	private float deltaTime;
	
	// axes
	private Vector3D X = new Vector3D(1,0,0),
					 Y = new Vector3D(0,1,0),
					 Z = new Vector3D(0,0,1);
	
	
	// movements and rotations
	private float tAmt = 2f,
				  rAmt = 45f;
	
	// display toggles
	private boolean displayAxes = true,
					togglePosLight = true;
	
	// location of d8 and camera
	private Point3D d8Loc = new Point3D(1.6, 1.0, -0.3);
	private Point3D shuttleLoc = new Point3D(-1.0, 1.1, 0.3);
	private Point3D planeLoc = new Point3D(0, 0, 0);
	private Vector3D planeScale = new Vector3D(3, 1, 3);
	
	private Point3D cameraStartLoc = new Point3D(0.0, 1.2, 6.0);
	private Vector3D[] cameraStartRot = {X, Y, Z};
	 
	private Point3D lightLoc = new Point3D(-4, 1, 1);
	private Point3D lightStartingLoc = new Point3D(-4, 1, 1);
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	// materials
	Material d8Material = new Material();
	Material shuttleMaterial = new Material();
	Material planeMaterial = new Material();
	
	// textures
	private Texture joglShuttleTexture, jogld8Texture, joglCheckTexture;
	private int thisTexture, shuttleTexture, d8Texture, checkTexture;
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();

	// model stuff
	private ImportedModel shuttle = new ImportedModel("/shuttle.obj");
	private D8 myD8 = new D8();
	private Plane myPlane = new Plane();
	private int numShuttleVertices, numD8Vertices;
	
	//////////////////
	// Buttons for UI
	private JButton resetButton, lightResetButton;
	
	public Starter()
	{	setTitle("Chapter8 - program 1");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		
		//////////////////
		// Layout
		JPanel topPanel = new JPanel();
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(myCanvas, BorderLayout.CENTER);

		////////////
		// UI Buttons
		resetButton = new JButton("Reset");
		topPanel.add(resetButton);
		CustomCommand resetCommand = new CustomCommand("Reset", "Resets camera", this);
		resetButton.setAction(resetCommand);

		lightResetButton = new JButton("Reset light");
		topPanel.add(lightResetButton);
		CustomCommand resetLightCommand = new CustomCommand("ResetLight", "Resets light", this);
		lightResetButton.setAction(resetLightCommand);
		
		///////////
		// Keys
		String [][] keys = {{"w", "Moves the Camera forward", "foward"},
							{"a", "Moves the Camera to the right", "right"},
							{"s", "Moves the camera backward", "back"},
							{"d", "Moves the camera to the left", "left"},
							{"q", "Moves the camera up", "up"},
							{"e", "Moves the camera down", "down"},
							{"UP", "Pitches the camera up", "pUP"},
							{"DOWN", "Pitches the camera down", "pDOWN"},
							{"LEFT", "Pans the camera to the left", "pLEFT"},
							{"RIGHT", "Pans the camera to the right", "pRIGHT"},
							{"NUMPAD1", "Rolls the camera to the left", "rLEFT"},
							{"NUMPAD3", "Rolls the camera to the right", "rRIGHT"},
							{"SPACE", "Displays the world axes", "axis"},
							{"o", "Moves the light forward", "lfoward"},
							{"k", "Moves the light backward", "lback"},
							{"j", "Move the light left", "lleft"},
							{"l", "Move the light right", "lright"},
							{"p", "Move the light down", "ldown"},
							{"i", "Move the light up", "lup"},
							{"t", "Toggles positional light", "tpositional"}};
		CustomCommand [] cc = new CustomCommand[keys.length]; 
		
		// Make a command and link it to the key for every key entry in keys[]
		for(int i = 0; i < keys.length; i++) {
			// c-key
			CustomCommand cCommand = new CustomCommand(keys[i][0], keys[i][1], this); // create command
			cc[i] = cCommand;
			JComponent contentPane = (JComponent) this.getContentPane();
			// get input map for content pane
			int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
			InputMap imap = contentPane.getInputMap(mapName);
			// create keystroke
			KeyStroke stroke;
			if(keys[i][0].length() == 1)
				stroke = KeyStroke.getKeyStroke(keys[i][0].charAt(0));
			else
				stroke = KeyStroke.getKeyStroke(keys[i][0]);
			// put keystroke in inputmap with label "color"
			imap.put(stroke, keys[i][2]);
			// get actionmap for content pane and put command into action map with label "color"
			ActionMap amap = contentPane.getActionMap();
			amap.put(keys[i][2], cc[i]);
			this.requestFocus();
		}
		// End Keys
		//////////////////
		
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}
	
	// Handler for all command elements, switches based on name of command
		public void CommandUpdate(String name) {
			switch(name) { // 
				case "w":	// Move forward
					camera.translateZ(tAmt*deltaTime);
					break;
				case "s":   // Move backward
					camera.translateZ(-tAmt*deltaTime);
					break;
				case "a":	// Move left
					camera.translateX(-tAmt*deltaTime);
					break;
				case "d":	// Move right
					camera.translateX(tAmt*deltaTime);
					break;
				case "q":	// Move up
					camera.translateY(tAmt*deltaTime);
					break;
				case "e":	// Move down
					camera.translateY(-tAmt*deltaTime);
					break;
					
				case "UP":	// Pitch up
					camera.pitch(rAmt*deltaTime);
					break;
				case "DOWN":// Pitch down
					camera.pitch(-rAmt*deltaTime);
					break;
				case "LEFT":// Pan left
					camera.pan(rAmt*deltaTime);
					break;
				case "RIGHT":// Pan Right
					camera.pan(-rAmt*deltaTime);
					break;
				case "NUMPAD1":// Roll left
					camera.roll(rAmt*deltaTime);
					break;
				case "NUMPAD3":// Roll right
					camera.roll(-rAmt*deltaTime);
					break;
					
				case "o": // move light z+
					lightLoc.setZ(lightLoc.getZ() - tAmt*deltaTime);
					break;
				case "k": // move light z-
					lightLoc.setZ(lightLoc.getZ() + tAmt*deltaTime);
					break;
				case "j": // move light x-
					lightLoc.setX(lightLoc.getX() - tAmt*deltaTime);
					break;
				case "l": // move light x+
					lightLoc.setX(lightLoc.getX() + tAmt*deltaTime);
					break;
				case "p": // move light y-
					lightLoc.setY(lightLoc.getY() - tAmt*deltaTime);
					break;
				case "i": // move light y+
					lightLoc.setY(lightLoc.getY() + tAmt*deltaTime);
					break;
					
				case "SPACE":
					displayAxes = !displayAxes;
					break;
				case "t":
					togglePosLight = !togglePosLight;
					break;
					
				//////////////////
				// Reset camera view to default
				case "Reset":
					camera.setCameraPos(cameraStartLoc);
					camera.setCameraRot(cameraStartRot);
					System.out.println("Returning Camera to starting orientation.");
					break;
				case "ResetLight":
					currentLight.setPosition(lightStartingLoc);
					lightLoc = lightStartingLoc;
					System.out.println("Returning light to 1,1,1");
					break;
			}
		}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		currentTime = System.currentTimeMillis();
		deltaTime = (float)(currentTime - prevCurrentTime)/1000f;
		prevCurrentTime = currentTime;

		currentLight.setPosition(lightLoc);
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts

		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program1);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		// draw the d8
		
		m_matrix.setToIdentity();
		m_matrix.translate(d8Loc.getX(),d8Loc.getY(),d8Loc.getZ());
		m_matrix.rotateX(25.0);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		int shadow_location = gl.glGetUniformLocation(rendering_program1, "shadowMVP");
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up d8 vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numD8Vertices);

		// ---- draw the shuttle
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(shuttleLoc.getX(),shuttleLoc.getY(),shuttleLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(-40.0);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, shuttle.getNumVertices());
		
		// ---- draw the plane
		
		// build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(planeLoc.getX(), planeLoc.getY(), planeLoc.getZ());
		m_matrix.scale(planeScale.getX(), planeScale.getY(), planeScale.getZ());
		m_matrix.rotateY(-135.0);
		m_matrix.rotateX(-30.0);
		m_matrix.rotateZ(-30);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, Plane.getNumVertices());
		
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		///////////////////
		// Draw the Point for the light and axes
		if(togglePosLight) {
			gl.glUseProgram(rendering_program4);
			mv_location = gl.glGetUniformLocation(rendering_program4, "mv_matrix");
			proj_location = gl.glGetUniformLocation(rendering_program4, "proj_matrix");

			m_matrix.setToIdentity();
			m_matrix.translate(currentLight.getPosition().getX()+1, currentLight.getPosition().getY(), currentLight.getPosition().getZ()+1);

			//  build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);

			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);

			gl.glPointSize(5);
			gl.glDrawArrays(GL_POINTS, 0, 1);
		}

		if(displayAxes) {
			// Axes
			m_matrix.setToIdentity();
			gl.glUseProgram(rendering_program3);
			mv_location = gl.glGetUniformLocation(rendering_program3, "mv_matrix");
			proj_location = gl.glGetUniformLocation(rendering_program3, "proj_matrix");

			m_matrix.setToIdentity();
			m_matrix.translate(0, .002, 0);

			//  build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);

			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glDrawArrays(GL_LINES, 0, 6);

			// Grid
			m_matrix.setToIdentity();
			gl.glUseProgram(rendering_program5);
			mv_location = gl.glGetUniformLocation(rendering_program5, "mv_matrix");
			proj_location = gl.glGetUniformLocation(rendering_program5, "proj_matrix");

			m_matrix.setToIdentity();
			m_matrix.translate(0, .001, 0);

			//  build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);

			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glDrawArrays(GL_LINES, 0, 44);
		}
	
		gl.glUseProgram(rendering_program2);

		// draw the d8
		thisTexture = d8Texture;
		thisMaterial = d8Material;		
		
		mv_location = gl.glGetUniformLocation(rendering_program2, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program2, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program2, "normalMat");
		int shadow_location = gl.glGetUniformLocation(rendering_program2,  "shadowMVP");
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(d8Loc.getX(),d8Loc.getY(),d8Loc.getZ());
		m_matrix.rotateX(25.0);

		//  build the VIEW matrix
		v_matrix.setToIdentity();
		v_matrix.concatenate(camera.computeView());
		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up d8 vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up d8 normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	
		
		// set up texture buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
	
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, thisTexture);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numD8Vertices);

		// draw the shuttle
		thisTexture = shuttleTexture;
		thisMaterial = shuttleMaterial;		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(shuttleLoc.getX(),shuttleLoc.getY(),shuttleLoc.getZ());
		m_matrix.rotateY(-135.0);
		m_matrix.rotateX(-30.0);
		m_matrix.rotateZ(-30);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// set up texture buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, thisTexture);

		gl.glDrawArrays(GL_TRIANGLES, 0, shuttle.getNumVertices());
		
		// ---- draw the plane\
		thisTexture = checkTexture;
		thisMaterial = planeMaterial;		
		installLights(rendering_program2, v_matrix);
		
		// build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(planeLoc.getX(), planeLoc.getY(), planeLoc.getZ());
		m_matrix.scale(planeScale.getX(), planeScale.getY(), planeScale.getZ());
		m_matrix.rotate(180,  X);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);

		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);

		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, thisTexture);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, Plane.getNumVertices());
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		System.out.println("OpenGL Ver: "+gl.glGetString(GL_VERSION)); // Print version of OpenGL
	
		camera.setCameraPos(cameraStartLoc);
		camera.setCameraRot(cameraStartRot);
		
		currentTime = System.currentTimeMillis();
		prevCurrentTime = currentTime;
		deltaTime = 0;
	
		createShaderPrograms();
		setupVertices();
		setupMaterials();
		setupTextures();
		setupShadowBuffers();
				
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
	}
	
	private void setupTextures() {
		joglShuttleTexture = loadTexture("textures/shuttleTex.jpg");
		shuttleTexture = joglShuttleTexture.getTextureObject();
		
//		joglSkyboxTexture = loadTexture("textures/cloudSkyBox.jpg");
//		skyboxTexture = joglSkyboxTexture.getTextureObject();
		
//		joglGoldTexture = loadTexture("textures/gold.jpg");
//		goldTexture = joglGoldTexture.getTextureObject();
		
		jogld8Texture = loadTexture("textures/d8.jpg");
		d8Texture = jogld8Texture.getTextureObject();
		
		joglCheckTexture = loadTexture("textures/checkered.jpg");
		checkTexture = joglCheckTexture.getTextureObject();
	}
	
	private void setupMaterials() {
		//Shuttle made of Pewter
		shuttleMaterial.setAmbient(new float[]{0, 0, 0, 1f});
		shuttleMaterial.setDiffuse(new float[]{.55f, .55f, .55f, 1f});
		shuttleMaterial.setSpecular(new float[]{.70f, .70f, .70f, 1f});
		shuttleMaterial.setShininess(32f);
		
		//d8 made of brass
		d8Material.setAmbient(new float[]{.329412f, .223529f, .027451f, 1f});
		d8Material.setDiffuse(new float[]{.780392f, .568627f, .113725f, 1f});
		d8Material.setSpecular(new float[]{.992157f, .941176f, .807843f, 1f});
		d8Material.setShininess(27.8974f);
		
		//plane
		planeMaterial.setAmbient(new float[]{.25f, .25f, .25f, 1f});
		planeMaterial.setDiffuse(new float[]{.4f, .4f, .4f, 1f});
		planeMaterial.setSpecular(new float[]{.774597f, .774597f, .774597f, 1f});
		planeMaterial.setShininess(76.8f);
	}
	
	public void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
	}

// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		setupShadowBuffers();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		// pyramid definition
		Vertex3D[] shuttle_vertices = shuttle.getVertices();
		numShuttleVertices = shuttle.getNumVertices();

		float[] shuttle_vertex_positions = new float[numShuttleVertices*3];
		float[] shuttle_normals = new float[numShuttleVertices*3];
		float[] shuttle_texture_positions = new float[numShuttleVertices*2];

		for (int i=0; i<numShuttleVertices; i++)
		{	shuttle_vertex_positions[i*3]   = (float) (shuttle_vertices[i]).getX();			
			shuttle_vertex_positions[i*3+1] = (float) (shuttle_vertices[i]).getY();
			shuttle_vertex_positions[i*3+2] = (float) (shuttle_vertices[i]).getZ();
			
			shuttle_texture_positions[i*2]   = (float) (shuttle_vertices[i]).getS();
			shuttle_texture_positions[i*2+1] = (float) (shuttle_vertices[i]).getT();
			
			shuttle_normals[i*3]   = (float) (shuttle_vertices[i]).getNormalX();
			shuttle_normals[i*3+1] = (float) (shuttle_vertices[i]).getNormalY();
			shuttle_normals[i*3+2] = (float) (shuttle_vertices[i]).getNormalZ();
		}

		numD8Vertices = myD8.getNumVertices();

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(15, vbo, 0);

		//  put the d8 vertices into the first buffer,
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(myD8.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		//  load the shuttle vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer shuttleVertBuf = Buffers.newDirectFloatBuffer(shuttle_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, shuttleVertBuf.limit()*4, shuttleVertBuf, GL_STATIC_DRAW);
		
		//  load the plane vertices into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer planeVertBuf = Buffers.newDirectFloatBuffer(myPlane.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, planeVertBuf.limit()*4, planeVertBuf, GL_STATIC_DRAW);
		
		// load the d8 normal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer torusNorBuf = Buffers.newDirectFloatBuffer(myD8.getNormals());
		gl.glBufferData(GL_ARRAY_BUFFER, torusNorBuf.limit()*4, torusNorBuf, GL_STATIC_DRAW);
		
		// load the shuttle normal coordinates into the fifth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer shuttleNorBuf = Buffers.newDirectFloatBuffer(shuttle_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, shuttleNorBuf.limit()*4, shuttleNorBuf, GL_STATIC_DRAW);
		
		// load the shuttle normal coordinates into the sixth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer planeNorBuf = Buffers.newDirectFloatBuffer(myPlane.getNormals());
		gl.glBufferData(GL_ARRAY_BUFFER, planeNorBuf.limit()*4, planeNorBuf, GL_STATIC_DRAW);
		
		// load the d8 texture coordinates into the seventh buffer 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer d8TextureBuf = Buffers.newDirectFloatBuffer(myD8.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, d8TextureBuf.limit()*4, d8TextureBuf, GL_STATIC_DRAW);
		
		// load the shuttle texture coordinates into the eighth buffer 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer shuttleTextureBuf = Buffers.newDirectFloatBuffer(shuttle_texture_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, shuttleTextureBuf.limit()*4, shuttleTextureBuf, GL_STATIC_DRAW);
		
		// load the d8 texture coordinates into the ninth buffer 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer planeTextureBuf = Buffers.newDirectFloatBuffer(myPlane.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, planeTextureBuf.limit()*4, planeTextureBuf, GL_STATIC_DRAW);
		
		Axes axes = new Axes();
		// load the axes vert coordinates into the tenth buffer 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer axesVertBuf = Buffers.newDirectFloatBuffer(axes.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, axesVertBuf.limit()*4, axesVertBuf, GL_STATIC_DRAW);
		
		Grid grid = new Grid();
		// load the axes vert coordinates into the tenth buffer 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer gridVertBuf = Buffers.newDirectFloatBuffer(grid.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, gridVertBuf.limit()*4, gridVertBuf, GL_STATIC_DRAW);
	}
	
	private void installLights(int rendering_program, Matrix3D v_matrix)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currentMaterial = thisMaterial;
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		if (togglePosLight) {
			gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
			gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
			gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		}
		else {
			gl.glProgramUniform4fv(rendering_program, ambLoc, 1, new float[] {0,0,0,0}, 0);
			gl.glProgramUniform4fv(rendering_program, diffLoc, 1, new float[] {0,0,0,0}, 0);
			gl.glProgramUniform4fv(rendering_program, specLoc, 1, new float[] {0,0,0,0}, 0);
		}
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	public static void main(String[] args) 
	{ 
		printVersions();
		Starter scene = new Starter();
		scene.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];

		vShader1Source = util.readShaderSource("shaders/vert.shader");
		vShader2Source = util.readShaderSource("shaders/vert_tex.shader");
		fShader2Source = util.readShaderSource("shaders/frag_tex.shader");
		vShader3Source = util.readShaderSource("shaders/vert_line.shader");
		fShader3Source = util.readShaderSource("shaders/frag_line.shader");
		vShader4Source = util.readShaderSource("shaders/vert_point.shader");
		fShader4Source = util.readShaderSource("shaders/frag_point.shader");
		vShader5Source = util.readShaderSource("shaders/vert_grid.shader");
		fShader5Source = util.readShaderSource("shaders/frag_grid.shader");

		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int vertexShader3 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader3 = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int vertexShader4 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader4 = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int vertexShader5 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader5 = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vertexShader1, vShader1Source.length, vShader1Source, null, 0);
		gl.glShaderSource(vertexShader2, vShader2Source.length, vShader2Source, null, 0);
		gl.glShaderSource(fragmentShader2, fShader2Source.length, fShader2Source, null, 0);
		gl.glShaderSource(vertexShader3, vShader3Source.length, vShader3Source, null, 0);
		gl.glShaderSource(fragmentShader3, fShader3Source.length, fShader3Source, null, 0);
		gl.glShaderSource(vertexShader4, vShader4Source.length, vShader4Source, null, 0);
		gl.glShaderSource(fragmentShader4, fShader4Source.length, fShader4Source, null, 0);
		gl.glShaderSource(vertexShader5, vShader5Source.length, vShader5Source, null, 0);
		gl.glShaderSource(fragmentShader5, fShader5Source.length, fShader5Source, null, 0);

		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);
		gl.glCompileShader(vertexShader3);
		gl.glCompileShader(fragmentShader3);
		gl.glCompileShader(vertexShader4);
		gl.glCompileShader(fragmentShader4);
		gl.glCompileShader(vertexShader5);
		gl.glCompileShader(fragmentShader5);

		rendering_program1 = gl.glCreateProgram();
		rendering_program2 = gl.glCreateProgram();
		rendering_program3 = gl.glCreateProgram();
		rendering_program4 = gl.glCreateProgram();
		rendering_program5 = gl.glCreateProgram();

		gl.glAttachShader(rendering_program1, vertexShader1);
		gl.glAttachShader(rendering_program2, vertexShader2);
		gl.glAttachShader(rendering_program2, fragmentShader2);
		gl.glAttachShader(rendering_program3, vertexShader3);
		gl.glAttachShader(rendering_program3, fragmentShader3);
		gl.glAttachShader(rendering_program4, vertexShader4);
		gl.glAttachShader(rendering_program4, fragmentShader4);
		gl.glAttachShader(rendering_program5, vertexShader5);
		gl.glAttachShader(rendering_program5, fragmentShader5);

		gl.glLinkProgram(rendering_program1);
		gl.glLinkProgram(rendering_program2);
		gl.glLinkProgram(rendering_program3);
		gl.glLinkProgram(rendering_program4);
		gl.glLinkProgram(rendering_program5);
		
	}

//------------------
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}

	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}
	
	public Texture loadTexture(String textureFileName) {
		Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e ) {e.printStackTrace();}
		return tex;
	}
	
	// Prints versions of Java and JOGL
	public static void printVersions() {
		System.out.println("JOGL Ver: "+Package.getPackage("com.jogamp.opengl").toString()
				+"\nJava Ver: "+(System.getProperty("java.version")));
	}
}