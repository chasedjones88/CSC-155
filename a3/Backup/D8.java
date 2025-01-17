package a3;

import graphicslib3D.*;

public class D8 extends Shape3D{

	float[] vertices = {
			1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, // Back Top
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, // Right Top
			-1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Front Top
			-1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Front Top
			-1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f,0.0f, -1.0f, 0.0f, // Back Bottom
			1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, // Right Bottom
			1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, // Front Bottom
			-1.0f, 0.0f, 1.0f, -1.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f // Front Bottom
	};
	float[] texture_verts = {
			0.0f, .5f, 0.25f, 0.5f, .25f/2, 1.0f,
			.25f, .5f, 0.25f*2, 0.5f, .25f/2 + 0.25f, 1.0f,
			.25f*2, .5f, 0.25f*3, 0.5f, .25f/2 + 0.25f*2, 1.0f,
			.25f*3, .5f, 1.0f, 0.5f, .25f/2 + 0.25f*3, 1.0f,
			.25f*3, .5f, 1.0f, 0.5f, .25f/2 + 0.25f*3, 0.0f,
			.25f*2, .5f, 0.25f*3, 0.5f, .25f/2 + 0.25f*2, 0.0f,
			.25f, .5f, 0.25f*2, 0.5f, .25f/2 + 0.25f, 0.0f,
			0.0f, .5f, 0.25f, 0.5f, .25f/2, 0.0f,
	};
	float[] normals = {
			0.70711f, 0f, -0.70711f, -0.70711f, 0f, -0.70711f, 0f, 1f, 0f,
			0.70711f, 0f, 0.70711f, 0.70711f, 0f, -0.70711f, 0f, 1f, 0f,
			-0.70711f, 0f, 0.70711f, 0.70711f, 0f, 0.70711f, 0f, 1f, 0f,
			-0.70711f, 0f, -0.70711f, -0.70711f, 0f, 0.70711f, 0f, 1f, 0f,
			-0.70711f, 0f, -0.70711f, 0.70711f, 0f, -0.70711f, 0f, -1f, 0f,
			0.70711f, 0f, -0.70711f, 0.70711f, 0f, 0.70711f, 0f, -1f, 0f,
			0.70711f, 0f, 0.70711f, -0.70711f, 0f, 0.70711f, 0f, -1f, 0f,
			-0.70711f, 0f, 0.70711f, -0.70711f, 0f, -0.70711f, 0f, -1f, 0f,
	};

	public D8() {}
	
	public float[] getVerts() {return vertices;}
	public float[] getTexVerts() {return texture_verts;}
	public float[] getNormals() {return normals;}
	public static int getNumVertices() {return 24;}
	
}
