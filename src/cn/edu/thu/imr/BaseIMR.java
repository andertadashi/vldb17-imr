package cn.edu.thu.imr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import Jama.Matrix;
import cn.edu.thu.imr.entity.TimePoint;
import cn.edu.thu.imr.entity.TimeSeries;

public class BaseIMR {
  public static double MINVAL = Double.MAX_VALUE;
  public static double MAXVAL = -Double.MAX_VALUE;

  protected ArrayList<Boolean> labelList; // whether the point is labeled
  protected int p; // AR(p) model
  protected double delta; // converge
  protected int maxNumIterations; // max iteration number

  public void setLabelList(ArrayList<Boolean> labelList) {
    this.labelList = labelList;
  }

  public void setP(int p) {
    this.p = p;
  }

  public void setDelta(double delta) {
    this.delta = delta;
  }

  public void setMaxNumIterations(int maxNumIterations) {
    this.maxNumIterations = maxNumIterations;
  }

  /**
   * learn the parameter can use incremental computation for x and y
   */
  protected Matrix learnParamsOLS(Matrix xMatrix, Matrix yMatrix) {
    Matrix phi = new Matrix(p, 1);

    Matrix xMatrixT = xMatrix.transpose();

    Matrix middleMatrix = xMatrixT.times(xMatrix);
    phi = middleMatrix.inverse().times(xMatrixT).times(yMatrix);

    return phi;
  }
  
  protected Matrix learnParamsIC(Matrix A, Matrix B) {
    Matrix phi = new Matrix(p, 1);
    
    phi = A.inverse().times(B);
    
    return phi;
  }

  /**
   * use phi to combine
   */
  protected Matrix combine(Matrix phi, Matrix xMatrix) {
    Matrix yhatMatrix = xMatrix.times(phi);

    return yhatMatrix;
  }

  /**
   * Absolute minimum
   */
  protected int repairAMin(Matrix yhatMatrix, Matrix yMatrix) {
    int rowNum = yhatMatrix.getRowDimension();
    Matrix residualMatrix = yhatMatrix.minus(yMatrix);

    double aMin = MINVAL;
    int targetIndex = -1;
    double yhat, yhatabs;

    for (int i = 0; i < rowNum; ++i) {
      if (labelList.get(i + p)) {
        continue;
      }
      if (Math.abs(residualMatrix.get(i, 0)) < delta) {
        continue;
      }

      yhat = yhatMatrix.get(i, 0);
      yhatabs = Math.abs(yhat);

      if (yhatabs < aMin) { // no need to > 0
        aMin = yhatabs;
        targetIndex = i;
      }
    }

    return targetIndex;
  }
}
