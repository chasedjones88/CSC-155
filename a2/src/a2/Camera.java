package a2;

import graphicslib3D.*;

public class Camera {
	private Point3D localLoc = new Point3D(0,0,0);
	private Vector3D rotVector = new Vector3D(0,0,0);
	
	private Vector3D U = new Vector3D(1,0,0);
	private Vector3D V = new Vector3D(0,1,0);
	private Vector3D N = new Vector3D(0,0,1);
	
	public void Camera() {
	}
	
	public void setCameraPos(float x, float y, float z) {
		localLoc = new Point3D(x,y,z);
	}
	
	public void setCameraRot(Vector3D rotVector) {
		this.rotVector = rotVector;
	}
	public void rotate(float amt, Vector3D axis) {
		if(axis.equals(new Vector3D(1,0,0))) {
			rotVector.setX(rotVector.getX()+amt);
		}
		else if(axis.equals(new Vector3D(0,1,0))) {
			rotVector.setY(rotVector.getY()+amt);
		}
		else if(axis.equals(new Vector3D(0,0,1))) {
			rotVector.setZ(rotVector.getZ()+amt);
		}
	}
	
	public void translateX(float amt) {
		Point3D U_mov = new Point3D(U.normalize());
		U_mov = U_mov.mult(amt);
		localLoc = localLoc.add(U_mov);
	}
	public void translateY(float amt) {
		Point3D V_mov = new Point3D(V.normalize());
		V_mov = V_mov.mult(amt);
		localLoc = localLoc.add(V_mov);
	}
	public void translateZ(float amt) {
		Point3D Z_mov = new Point3D(N.normalize());
		Z_mov = Z_mov.mult(amt);
		localLoc = localLoc.add(Z_mov);
	}
	
	public Matrix3D computeView() {
		Matrix3D retMat = new Matrix3D();
		retMat.translate(localLoc.getX(), localLoc.getY(), localLoc.getZ());
		retMat.rotate(rotVector.getX(), rotVector.getY(), rotVector.getZ());
		return retMat.inverse();
	}
	
	public Point3D getLoc() { return localLoc; }
	public Vector3D getRot() { return rotVector; }
}
