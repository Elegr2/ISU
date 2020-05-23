float distance(PVector a, PVector b){ // Cartesian distance (their dist() is wonky)
  return sqrt(pow(a.x-b.x, 2)+pow(a.y-b.y, 2));
}

float[] dists(PVector pos, ArrayList<Wall> arena){ // NOT IMPLEMENTED (Senses for QLearning)
  float[] dsts = new float[]{500, 500, 500, 500}; // UP, DOWN, LEFT, RIGHT
  for (Wall w : arena){
    PVector[] collisions = new PVector[4]; //UDLR
    collisions[0] = lineRectPos(pos.x, pos.y, pos.x, pos.y-500, w.pos.x, w.pos.y, w.dim.x, w.dim.y)[1]; // UP, checking collision with the bottom of the wall
    collisions[1] = lineRectPos(pos.x, pos.y, pos.x, pos.y+500, w.pos.x, w.pos.y, w.dim.x, w.dim.y)[0]; // DOWN, checking collision with the top of the wall
    collisions[2] = lineRectPos(pos.x, pos.y, pos.x-500, pos.y, w.pos.x, w.pos.y, w.dim.x, w.dim.y)[3]; // LEFT, checking collision with the right side of the wall
    collisions[3] = lineRectPos(pos.x, pos.y, pos.x+500, pos.y, w.pos.x, w.pos.y, w.dim.x, w.dim.y)[2]; // RIGHT, checking collision with the left side of the wall
    for (int i = 0; i < 4; i++){
      if (collisions[i] != null && distance(pos, collisions[i]) < dsts[i]){
        dsts[i] = distance(pos, collisions[i]);
      }
    }
  }
  return dsts;
}

boolean lineRect(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) { // NOT IMPLEMENTED Senses for QLearning
  boolean left =   lineLine(x1,y1,x2,y2, rx,ry,rx, ry+rh);
  boolean right =  lineLine(x1,y1,x2,y2, rx+rw,ry, rx+rw,ry+rh);
  boolean top =    lineLine(x1,y1,x2,y2, rx,ry, rx+rw,ry);
  boolean bottom = lineLine(x1,y1,x2,y2, rx,ry+rh, rx+rw,ry+rh);
  if (left || right || top || bottom) {
    return true;
  }
  return false;
}

PVector[] lineRectPos(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) { // NOT IMPLEMENTED Senses for QLearning
  PVector left =   lineLinePos(x1,y1,x2,y2, rx,ry,rx, ry+rh);
  PVector right =  lineLinePos(x1,y1,x2,y2, rx+rw,ry, rx+rw,ry+rh);
  PVector top =    lineLinePos(x1,y1,x2,y2, rx,ry, rx+rw,ry);
  PVector bottom = lineLinePos(x1,y1,x2,y2, rx,ry+rh, rx+rw,ry+rh);
  return new PVector[]{top, bottom, left, right};
}

boolean lineLine(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) { // NOT IMPLEMENTED Senses for QLearning
  float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
    return true;
  }
  return false;
}

PVector lineLinePos(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) { // NOT IMPLEMENTED Senses for QLearning
  float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
    float intersectionX = x1 + (uA * (x2-x1));
    float intersectionY = y1 + (uA * (y2-y1));
    return new PVector(intersectionX, intersectionY);
  }
  return null;
}

PVector fromPolar(float theta, float r){ // NOT IMPLEMENTED Movement for QLearning
  return new PVector(r*cos(theta), r*sin(theta));
}
