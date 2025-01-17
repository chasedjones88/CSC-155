package a3;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import java.awt.BorderLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.nio.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.common.nio.Buffers;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import graphicslib3D.*;
import graphicslib3D.light.AmbientLight;
import graphicslib3D.light.PositionalLight;
import graphicslib3D.shape.Sphere;
import graphicslib3D.shape.Torus;

import java.lang.Math.*;

public class Starter extends JFrame implements GLEventListener, MouseWheelListener
{
	private GLCanvas myCanvas;
	private int rendering_program;
	private GLSLUtils util = new GLSLUtils();
	
	String path = "";
	
	//////////////////
	// buffers
	private int vao[] = new int[1],
				vbo[] = new int[50];
	
	//////////////////
	// Camera Data
	private Camera camera = new Camera();
	private Vector3D U = new Vector3D(1,0,0),
			 		 V = new Vector3D(0,1,0),
			 		 N = new Vector3D(0,0,1);
	
	//////////////////
	// Translate and rotate Speeds
	private float amount = 45f, // Amount to rotate by
				  tAmt = 10.0f, // Amount to translate by
				  deltaTime;
	private float amt = 0f;
	//////////////////
	// For Time-based computation
	private long currentTime,
	lastCurrentTime;
	// End camera
	//////////////////
			
	//////////////////
	// Matrices
	int mv_loc; // mv matrix
	int n_loc; // normals
	private MatrixStack mvStack = new MatrixStack(20);
	private int ms = 0; // matrix size for pops
	
	private Matrix3D l_matrix = new Matrix3D(); // light transforms
	
	//////////////////
	// Display toggles
	private boolean displayAxes = true,
					togglePosLight = true;
	
	//////////////////
	// Shapes
	Sphere mySphere = new Sphere();
	ImportedModel shuttle;
	
	//////////////////
	// Textures
	private int shuttleTexture;
	private Texture joglShuttleTexture;
	
	private int stoneTexture;
	private Texture joglStoneTexture;
	
	private int skyboxTexture;
	private Texture joglSkyboxTexture;
	
	private int goldTexture;
	private Texture joglGoldTexture;
	
	private int d8Texture;
	private Texture jogld8Texture;
	
	private int checkTexture;
	private Texture joglCheckTexture;
	
	//////////////////
	// Materials
	Material shuttleMat = new Material();
	Material d8Mat = new Material();
	
	//////////////////
	// Shadows
	private int screenSizeX, screenSizeY;
	private int[] shadow_tex = new int[1],
				  shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D(),
					 lightP_matrix = new Matrix3D(),
					 shadowMVP = new Matrix3D(),
					 shadowMVP2 = new Matrix3D(),
					 b = new Matrix3D();
	
	//////////////////
	// Lights
	private float [] globalAmbient = {0f, 0f, 0f, 0f};
	private PositionalLight currentLight = new PositionalLight();
	private Point3D lightLoc = new Point3D(5f, 10f, 5f);
	
	//////////////////
	// Buttons for UI
	private JButton resetButton;

	//////////////////
	// Starter
	public Starter()
	{	
		//////////////////
		// Window
		setTitle("Chase Jones - Assignment #3");
		setSize(1400, 1000);
		myCanvas = new GLCanvas(); // Create frame
		myCanvas.addGLEventListener(this);
		
		//////////////////
		// Layout
		JPanel topPanel = new JPanel();
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(myCanvas, BorderLayout.CENTER);
		
		////////////
		// UI Buttons
		resetButton = new JButton("Reset");
		topPanel.add(resetButton);
		CustomCommand resetCommand = new CustomCommand("Reset", "Resets triangle", this);
		resetButton.setAction(resetCommand);
		
		///////////
		// Listeners
		///////////
		
		//////////////////
		// Mouse
		this.addMouseWheelListener(this);
		
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
		
		
		//////////////////
		// Set Active Pane
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 26);
		animator.start();
	}
	
	// Handler for all command elements, switches based on name of command
	public void CommandUpdate(String name) {
		switch(name) { // 
			case "w":	// Move forward
				camera.translateZ(tAmt*deltaTime); printUpdate();
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
				camera.pitch(amount*deltaTime);
				break;
			case "DOWN":// Pitch down
				camera.pitch(-amount*deltaTime);
				break;
			case "LEFT":// Pan left
				camera.pan(amount*deltaTime);
				break;
			case "RIGHT":// Pan Right
				camera.pan(-amount*deltaTime);
				break;
			case "NUMPAD1":// Roll left
				camera.roll(amount*deltaTime);
				break;
			case "NUMPAD3":// Roll right
				camera.roll(-amount*deltaTime);
				break;
				
			case "o": // move light z+
				lightLoc.setZ(lightLoc.getZ() + tAmt*deltaTime);
				break;
			case "k": // move light z-
				lightLoc.setZ(lightLoc.getZ() - tAmt*deltaTime);
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
				camera.setCameraPos(20,10,20);
				camera.setCameraRot(new Vector3D[] {new Vector3D(1,0,0), new Vector3D(0,1,0), new Vector3D(0,0,1)});
				camera.pan(45);
				System.out.println("Returning Camera to starting orientation.");
				break;
		}
	}
	
	public void printUpdate() {
		System.out.println("\nCamera has location: "+ camera.getLoc().toString());
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {
		passTwo();
	}
	
	
	public void passTwo() 
	{	
		currentTime = System.currentTimeMillis();
		deltaTime = (float)(currentTime - lastCurrentTime)/1000f;
		lastCurrentTime = currentTime;
		
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// Set Background buffer color
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		gl.glUseProgram(rendering_program);
		
		mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		n_loc = gl.glGetUniformLocation(rendering_program, "norm_matrix");
		
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_TEXTURE_2D);
		gl.glDepthFunc(GL_LEQUAL);
		
		////////////
		// Set World center
		pushMatrix();
		mvStack.loadIdentity();
		mvStack.multMatrix(camera.computeView());
		
		
		////////////
		// Lights
		currentLight.setPosition(lightLoc);
		Matrix3D v_matrix = new Matrix3D();
		v_matrix.setToIdentity();
		v_matrix.translate(-camera.getX(), -camera.getY(), -camera.getZ());
		
		
		installLights(v_matrix);
		
		/////////////
		// Get matrices to manipulate vert shader
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		
		////////////
		// Draw World axes
		
		int offset_color = gl.glGetUniformLocation(rendering_program, "isLine");
		gl.glProgramUniform1i(rendering_program, offset_color, 1);
		
		if(displayAxes) {
			gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glDrawArrays(GL_LINES, 0, 6);
		}
		
		gl.glProgramUniform1i(rendering_program, offset_color, 0);

		/// Plane
		pushMatrix();
		mvStack.scale(25, 25, 25);
		mvStack.rotate(180, 1, 0, 0);
		
		drawTextureObject(new int[] {8,9,10}, checkTexture, new Shadeless(), mySphere.getIndices().length);
		popMatrix();
		
		/////////////
		// Sphere
		installMaterial(Material.GOLD);
		pushMatrix();
		mvStack.translate(-5, 10, 15);
		mvStack.scale(10, 10, 10);
		
		drawTextureObject(new int[] {0,1,2}, goldTexture, Material.GOLD, mySphere.getIndices().length);
		popMatrix();
		
		//////////////////
		// Skybox
		pushMatrix();
		mvStack.scale(100,100,100);
		drawTextureObject(new int[] {3,4}, skyboxTexture, new Shadeless(), Cube.getNumVertices());
		popMatrix();
		
 		//////////////////
		//	ball around light
		/*pushMatrix();
		mvStack.translate(lightLoc.getX(), lightLoc.getY(), lightLoc.getZ());
		mvStack.scale(.1,.1,.1);
		
		drawTextureObject(new int[] {0,1,2}, 0, new Shadeless(), mySphere.getIndices().length);
		popMatrix();*/
		
		////////////////////
		// d8
		pushMatrix();
		mvStack.translate(5, 5, -25);
		mvStack.scale(4, 5, 4);
		pushMatrix();
		mvStack.rotate(22,0, 0, 1);
		
		drawTextureObject(new int[] {5,6,7}, d8Texture, d8Mat, D8.getNumVertices());

		popMatrix(); popMatrix();
		////////////////////
		
		////////////////////
		// Shuttle
		mvStack.translate(-15, 3, -15);
		pushMatrix();
		mvStack.rotate(-105, 0, 1, 0);
		mvStack.scale(25, 25, 25);
		
		drawTextureObject(new int[]{12,13,14}, shuttleTexture, shuttleMat, shuttle.getNumVertices());
		////////////////////
		
		// Pop all matrices
		while(ms > 0) {
			popMatrix();
		}
		
	}
	
	private void drawTextureObject(int [] vbos, int texture, Material mat, int verts) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_loc, 1, false, (mvStack.peek().inverse()).transpose().getFloatValues(),0);
		
		for(int i = 0; i < vbos.length; i++) {
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vbos[i]]);
			if(i == 1) { gl.glVertexAttribPointer(i, 2, GL_FLOAT, false, 0, 0); }
			else { gl.glVertexAttribPointer(i, 3, GL_FLOAT, false, 0, 0); }
			gl.glEnableVertexAttribArray(i);
		}
		
		installMaterial(mat);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texture);
		gl.glDrawArrays(GL_TRIANGLES, 0, verts);
	}
	
	private void pushMatrix() {
		mvStack.pushMatrix();
		ms++;
	}
	private void popMatrix() {
		mvStack.popMatrix();
		ms--;
	}
	
	private void installLights(Matrix3D v_matrix)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		Point3D lightP = currentLight.getPosition();
		System.out.println("\n"+currentLight.getPosition().toString()+ "\n");
		Point3D lightPv = lightP.mult(v_matrix);
		float[] viewspaceLightPos = new float[] {(float) lightPv.getX(), (float) lightPv.getY(), (float) lightPv.getZ()};
		System.out.println(viewspaceLightPos[0] + " " + viewspaceLightPos[1] + " " + viewspaceLightPos[2]);
		
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);
		
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");
		
		if(togglePosLight) {
			gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
			gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
			gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		}
		else {
			gl.glProgramUniform4fv(rendering_program, ambLoc, 1, new float[] {0,0,0,0}, 0);
			gl.glProgramUniform4fv(rendering_program, diffLoc, 1, new float[] {0,0,0,0}, 0);
			gl.glProgramUniform4fv(rendering_program, specLoc, 1, new float[] {0,0,0,0}, 0);
		}
		gl.glProgramUniform4fv(rendering_program, posLoc, 1, viewspaceLightPos, 0);
	}
	
	private void installMaterial(Material mat) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		int ambLocMat = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int difLocMat = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int specLocMat = gl.glGetUniformLocation(rendering_program, "material.specular");
		int shinLocMat = gl.glGetUniformLocation(rendering_program, "material.shininess");
		
		gl.glProgramUniform4fv(rendering_program, ambLocMat, 1, mat.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, difLocMat, 1, mat.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLocMat, 1, mat.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, shinLocMat, mat.getShininess());
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		currentTime = System.currentTimeMillis();
		lastCurrentTime = currentTime;
		
		l_matrix.setToIdentity();
		
		camera.setCameraPos(20,10,20);
		camera.setCameraRot(new Vector3D[] {new Vector3D(1,0,0), new Vector3D(0,1,0), new Vector3D(0,0,1)});
		camera.pan(45);
		
		shuttle = new ImportedModel("../shuttle.obj");
		
		setupVertices();
		setupMaterials();
		setupTextures();
		
		setupShadowBuffers();
		b.setElementAt(0, 0, 0.5f);
		b.setElementAt(0, 1, 0.0f);
		b.setElementAt(0, 2, 0.0f);
		b.setElementAt(0, 3, 0.5f);
		b.setElementAt(1, 0, 0.0f);
		b.setElementAt(1, 1, 0.5f);
		b.setElementAt(1, 2, 0.0f);
		b.setElementAt(1, 3, 0.5f);
		b.setElementAt(2, 0, 0.0f);
		b.setElementAt(2, 1, 0.0f);
		b.setElementAt(2, 2, 0.5f);
		b.setElementAt(2, 3, 0.5f);
		b.setElementAt(3, 0, 0.0f);
		b.setElementAt(3, 1, 0.0f);
		b.setElementAt(3, 2, 0.0f);
		b.setElementAt(3, 3, 1.0f);
		
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		System.out.println("OpenGL Ver: "+gl.glGetString(GL_VERSION)); // Print version of OpenGL
	}
	
	public void setupShadowBuffers(GLAutoDrawable d) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        screenSizeX = myCanvas.getWidth();
        screenSizeY = myCanvas.getHeight();

        gl.glGenFramebuffers(1, shadow_buffer, 0);
        gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);

        gl.glGenTextures(1, shadow_tex, 0);
        gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, screenSizeX, screenSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        // may reduce shadow border artifacts
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }
	
	private void setupTextures() {
		joglShuttleTexture = loadTexture("a3/shuttleTex.jpg");
		shuttleTexture = joglShuttleTexture.getTextureObject();
		
		joglStoneTexture = loadTexture("a3/pexels-photo.jpg");
		stoneTexture = joglStoneTexture.getTextureObject();
		
		joglSkyboxTexture = loadTexture("a3/cloudSkyBox.jpg");
		skyboxTexture = joglSkyboxTexture.getTextureObject();
		
		joglGoldTexture = loadTexture("a3/gold.jpg");
		goldTexture = joglGoldTexture.getTextureObject();
		
		jogld8Texture = loadTexture("a3/d8.jpg");
		d8Texture = jogld8Texture.getTextureObject();
		
		joglCheckTexture = loadTexture("a3/checkered.jpg");
		checkTexture = joglCheckTexture.getTextureObject();
	}
	
	private void setupMaterials() {
		//Shuttle made of Pewter
		shuttleMat.setAmbient(new float[]{.105882f, .058824f, .113725f, 1f});
		shuttleMat.setDiffuse(new float[]{.427451f, .470588f, .541176f, 1f});
		shuttleMat.setSpecular(new float[]{.333333f, .333333f, .521569f, 1f});
		shuttleMat.setShininess(9.84615f);
		
		//d8 made of rubber
		d8Mat.setAmbient(new float[]{.02f, .02f, .02f, 1f});
		d8Mat.setDiffuse(new float[]{.01f, .01f, .01f, 1f});
		d8Mat.setSpecular(new float[]{.4f, .4f, .4f, 1f});
		d8Mat.setShininess(10f);
	}
	
	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		////////////////
		// Sun Sphere
		Vertex3D[] vertices = mySphere.getVertices();
		int[] indices = mySphere.getIndices();
		
		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++) {
			pvalues[i*3] = (float) (vertices[indices[i]]).getX();
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(15, vbo, 0);	// Change the first arg for each element added to scene
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
		
		//////////////
		// SKYBOX
		Cube cwCube = new Cube(false); // Creates cube with faces pointing inwards
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer skyboxBuf = Buffers.newDirectFloatBuffer(cwCube.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, skyboxBuf.limit()*4, skyboxBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer skyboxTexBuf = Buffers.newDirectFloatBuffer(cwCube.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, skyboxTexBuf.limit()*4, skyboxTexBuf, GL_STATIC_DRAW);

		/////////////
		// d8 
		D8 d8 = new D8();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer d8Buf = Buffers.newDirectFloatBuffer(d8.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, d8Buf.limit()*4, d8Buf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer d8TextureBuf = Buffers.newDirectFloatBuffer(d8.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, d8TextureBuf.limit()*4, d8TextureBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer d8NorBuf = Buffers.newDirectFloatBuffer(d8.getNormals());
		gl.glBufferData(GL_ARRAY_BUFFER, d8NorBuf.limit()*4, d8NorBuf, GL_STATIC_DRAW);
		
		/////////////
		// Plane
		Plane plane = new Plane();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer planeBuf = Buffers.newDirectFloatBuffer(plane.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, planeBuf.limit()*4, planeBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer planeTextureBuf = Buffers.newDirectFloatBuffer(plane.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, planeTextureBuf.limit()*4, planeTextureBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer planeNorBuf = Buffers.newDirectFloatBuffer(plane.getNormals());
		gl.glBufferData(GL_ARRAY_BUFFER, planeNorBuf.limit()*4, planeNorBuf, GL_STATIC_DRAW);
		
		
		/////////////
		// Axes
		
		Axes axes = new Axes();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer axesBuf = Buffers.newDirectFloatBuffer(axes.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, axesBuf.limit()*4, axesBuf, GL_STATIC_DRAW);
		
		// Shuttle
		
		vertices = shuttle.getVertices();
		int numObjVertices = shuttle.getNumVertices();
		
		pvalues = new float[numObjVertices*3];
		tvalues = new float[numObjVertices*2];
		nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices[i]).getX();
			pvalues[i*3+1] = (float) (vertices[i]).getY();
			pvalues[i*3+2] = (float) (vertices[i]).getZ();
			tvalues[i*2]   = (float) (vertices[i]).getS();
			tvalues[i*2+1] = (float) (vertices[i]).getT();
			nvalues[i*3]   = (float) (vertices[i]).getNormalX();
			nvalues[i*3+1] = (float) (vertices[i]).getNormalY();
			nvalues[i*3+2] = (float) (vertices[i]).getNormalZ();
		}
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer monkeyBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, monkeyBuf.limit()*4, monkeyBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer monkeyTextureBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, monkeyTextureBuf.limit()*4, monkeyTextureBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer monkeyNorBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, monkeyNorBuf.limit()*4, monkeyNorBuf, GL_STATIC_DRAW);
	}
	
	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		// Read shaders from files
		String currentDir = System.getProperty("user.dir");
		if(!currentDir.contains("src"))
			path = "src/";
			
		String vshaderSource[] = util.readShaderSource(path + "a3/vert.shader");
		String fshaderSource[] = util.readShaderSource(path + "a3/frag.shader");

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
	
	public Texture loadTexture(String textureFileName) {
		Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e ) {e.printStackTrace();}
		return tex;
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
	
	// Apply changes in window size to dimensions and placement of objects
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
		return r;
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
	}

}
