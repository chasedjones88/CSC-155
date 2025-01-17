package objects;

import graphicslib3D.*;

public class Cube extends Shape3D{
	
	private float[] vertices = 
		{	-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, 1.0f, -1.0f, -1.0f, // BackR 
			1.0f,  1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, // BackL
			1.0f, -1.0f,  1.0f, 1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, //RightR
			1.0f,  1.0f,  1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f, //RightL
			-1.0f, -1.0f,  1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, //FrontR 
			-1.0f,  1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, //FrontL
			-1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, //LeftR
			-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f, //LeftL
			1.0f, -1.0f,  1.0f,  -1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f, //BottomNear
			-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, //BottomFar
			1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f, //TopFar
			-1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f //TopNear
		};
	private float[] texture_verts = {
			1.0f*3/4, 1.0f/3, 1.0f*4/4, 1.0f/3, 1.0f*4/4, 2.0f/3, //Bottom Right, Bottom Left, Top Right
			1.0f*3/4, 2.0f/3, 1.0f*3/4, 1.0f/3,  1.0f*4/4, 2.0f/3, //Bottom Left, Top Left, Top Right
			1.0f*2/4, 1.0f/3, 1.0f*3/4, 1.0f/3, 1.0f*3/4, 2.0f/3,
			1.0f*2/4, 2.0f/3, 1.0f*2/4, 1.0f/3, 1.0f*3/4, 2.0f/3,
			1.0f*1/4, 1.0f/3, 1.0f*2/4, 1.0f/3, 1.0f*2/4, 2.0f/3,
			1.0f*1/4, 2.0f/3, 1.0f*1/4, 1.0f/3, 1.0f*2/4, 2.0f/3,
			0.0f, 1.0f/3, 1.0f*1/4, 1.0f/3, 1.0f*1/4, 2.0f/3,
			0.0f, 2.0f/3, 0.0f, 1.0f/3, 1.0f*1/4, 2.0f/3,
			1.0f*2/4, 0, 1.0f*3/4, 0, 1.0f*3/4, 1.0f/3,
			1.0f*2/4, 1.0f/3, 1.0f*2/4, 0, 1.0f*3/4, 1.0f/3,
			1.0f*2/4, 2.0f/3, 1.0f*3/4, 2.0f/3, 1.0f*3/4, 1.0f,
			1.0f*2/4, 1.0f, 1.0f*2/4, 2.0f/3, 1.0f*3/4, 1.0f,
	};
	
	public Cube(boolean FacingOut) {
		if(!FacingOut)
		{
			float[] cw_vertices = { 
					-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, // BackR 
					1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f, // BackL
					1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f, //RightR
					1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f, //RightL
					1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, //FrontR 
					-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f, //FrontL
					-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f, //LeftR
					-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f, //LeftL
					-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f, //BottomNear
					1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, //BottomFar
					-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f, //TopFar
					1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f //TopNear
				};
			float[] cw_tex_verts = {
					0.5f, 2.0f/3, 0.5f, 1.0f/3, .75f, 1.0f/3,
					.75f, 1.0f/3, .75f, 2.0f/3, 0.5f, 2.0f/3,
					.75f, 1.0f/3, 1.0f, 1.0f/3, .75f, 2.0f/3,
					1.0f, 1.0f/3, 1.0f, 2.0f/3, .75f, 2.0f/3,
					0.0f, 1.0f/3, .25f, 1.0f/3, 0.0f, 2.0f/3,
					.25f, 1.0f/3, .25f, 2.0f/3, 0.0f, 2.0f/3,
					.25f, 1.0f/3, 0.5f, 1.0f/3, .25f, 2.0f/3, 
					0.5f, 1.0f/3, 0.5f, 2.0f/3, .25f, 2.0f/3,
					0.5f, 0.0f  , .75f, 0.0f  , .75f, 1.0f/3,
					.75f, 1.0f/3, 0.5f, 1.0f/3, 0.5f, 0.0f  ,
					0.5f, 2.0f/3, .75f, 2.0f/3, .75f, 1f,
					.75f, 1f    , 0.5f, 1f    , 0.5f, 2.0f/3,
			};
			
			this.vertices = cw_vertices;
			this.texture_verts = cw_tex_verts;
		}
	}
	public Cube() {}
	
	public float[] getVerts() {return vertices;}
	public float[] getTexVerts() {return texture_verts;}
	public static int getNumVertices() {return 36;}
	
}
