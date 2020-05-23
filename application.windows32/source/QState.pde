// NOT IMPLEMENTED

class QState{
  float[] data;
  
  QState(float[] dta){
    data = new float[dta.length];
    for (int i = 0; i < dta.length; i++){
      data[i] = floor(10*dta[i])/10.0; // Cut off anything after the first decimal point
    }
  }
  
  boolean equals(QState q){
    for (int i = 0; i < data.length; i++){
      if (data[i] != q.data[i]) return false;
    }
    return true;
  }
  
  float[] getState(){
    return data;
  }
}
