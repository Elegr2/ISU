import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ISU extends PApplet {

ArrayList<Wall> walls;
Population basic, neat;
//QDot q;
int generation = 1;
PVector camera = new PVector(-50, -50);
//PVector qGoal = new PVector(1270, 125);
//float QVal;

public void setup(){
  
  walls = new ArrayList<Wall>();
  setupBoundaries(); // Set the boundaries that differentiate the test zones
  addMazeAt(new PVector(0, 0)); // Add the maze
  addMazeAt(new PVector(510, 0));
  //addMazeAt(new PVector(1020, 0));
  basic = new Population(new PVector(250, 375), 200, 500, new PVector(250, 125), color(0));
  neat = new Population(new PVector(760, 375), 200, 500, new PVector(760, 125), color(255, 0, 0));
  //q = new QDot(new PVector(1270, 375), color(255, 0, 255));
}

public void draw(){
  background(200); // Fill the canvas with a color
  for (Wall w : walls){ // Draw all the walls
    w.render(camera);
  }
  
  basic.step(walls);
  neat.step(walls);
  //q.step(qGoal, walls);
  
  basic.render(camera);
  neat.render(camera);
  //q.render(camera);
  
  if (basic.allFinished() && neat.allFinished()/* && (!q.alive || q.atGoal)*/){
    basic.reset(0, new PVector(250, 375));
    neat.reset(1, new PVector(760, 375));
    //q.reset(new PVector(1270, 375));
    generation++;
    //q.exploiting = false;
    //if (generation%10 == 0) q.exploiting = true;
  }
  fill(0);
  text("Gen: " + generation, 0-camera.x, 530-camera.y);
  //text("QVal: " + QVal, 1000-camera.x, 530-camera.y);
}

public void setupBoundaries(){ // Boundaries for the arena
  walls.add(new Wall(new PVector(500, 0), new PVector(10, 500)));
  walls.add(new Wall(new PVector(1010, 0), new PVector(10, 500)));
  walls.add(new Wall(new PVector(-10, -10), new PVector(1540, 10)));
  walls.add(new Wall(new PVector(-10, 500), new PVector(1540, 10)));
  walls.add(new Wall(new PVector(-10, 0), new PVector(10, 500)));
  walls.add(new Wall(new PVector(1520, 0), new PVector(10, 500)));
}

public void addMazeAt(PVector tl){
  walls.add(new Wall(new PVector(tl.x, tl.y+245), new PVector(300, 10)));
  walls.add(new Wall(new PVector(tl.x+270, tl.y), new PVector(30, 100)));
  walls.add(new Wall(new PVector(tl.x+270, tl.y+115), new PVector(30, 115)));
  walls.add(new Wall(new PVector(tl.x+200, tl.y+115), new PVector(70, 5)));
  walls.add(new Wall(new PVector(tl.x+200, tl.y+120), new PVector(5, 70)));
}
class Brain{
  PVector[] brain; // List of vectors, determining the next acceleration vector of the dot
  int index;
  
  Brain(int size){
    brain = new PVector[size];
    for (int i = 0; i < size; i++){
      brain[i] = PVector.random2D(); // Fill the brain with random vectors
    }
    index = 0;
  }
  
  public PVector get(){ // Get the next vector, and increment index
    index++;
    if (index >= brain.length){ // If you've reached the end of the brain
      index--; // Step back
      return new PVector(0, 0); // Do not accelerate
    }
    return brain[index-1];
  }
  
  public boolean atEnd(){ // Run through all the vectors
    return index == brain.length-1;
  }
  
  public void evolve(){
    for (int i = 0; i < brain.length; i++){
      if (random(1) < 0.01f){ // Randomly replace ~1% of the vectors with random new vectors
        brain[i] = PVector.random2D();
      }
    }
  }
  
  public Brain getCopy(){
    Brain copy = new Brain(brain.length); // Create a copy
    
    for (int i = 0; i < brain.length; i++){ // Copy values
      copy.brain[i] = brain[i].copy();
    }
    
    return copy;
  }
}
class Dot{
  PVector pos, mom, mov; // Position, momentum, acceleration
  Brain brain;
  boolean alive, atGoal;
  boolean stopped;
  boolean prevBest;
  int c;
  
  Dot(PVector startPos, int brainSize, int col){ // Pretty standard contructor
    pos = startPos.copy();
    mom = new PVector(0, 0);
    mov = new PVector(0, 0);
    brain = new Brain(brainSize);
    alive = true;
    atGoal = false;
    stopped = false;
    prevBest = false;
    c = col;
  }
  
  public void step(PVector goal, ArrayList<Wall> walls){
    if (!stopped && alive && !atGoal){ // Make sure it can move
      mov = brain.get(); // get the next acceleration vector
      mom.add(mov); // Add acceleration to momentum
      mom.limit(5); // Limit momentum
      mom.mult(0.95f); // Drag
      pos.add(mom); // Move
      if (distance(pos, goal) < 5){
        atGoal = true;
      }
      for (Wall w : walls){ // Check collisions with walls
        if (w.collidingWith(pos)){
          alive = false; // Die
          break; // Already dead, no need to check the other walls
        }
      }
      stopped = brain.atEnd(); // If you've run out of accelerations, stop.
    }
  }
  
  public Dot getCopy(PVector copyTo){ // Copy the dot
    Dot copy = new Dot(copyTo, brain.brain.length, c);
    
    copy.brain = brain.getCopy();
    
    return copy;
  }
  
  public Dot getOffspring(PVector to){ // Get an evolved offspring of the dot
    Dot offspring = getCopy(to);
    offspring.evolve();
    return offspring;
  }
  
  public void evolve(){ // Just call evolve() on the brain.
    brain.evolve();
  }
  
  public float getFitness(PVector goal){
    if (atGoal){
      return 2 + (1/brain.index); // At goal
    }
    return 1/distance(pos, goal); // Not at goal
  }
  
  public void render(PVector cam){
    noStroke();
    fill(c);
    if (prevBest){
      ellipse(pos.x-cam.x, pos.y-cam.y, 5, 5);
    }else{
      ellipse(pos.x-cam.x, pos.y-cam.y, 3, 3);
    }
  }
}
class Population{
  Dot[] pop;
  PVector goal;
  boolean manualStop;
  
  Population(PVector startPos, int size, int dotBrainSize, PVector _goal, int col){
    pop = new Dot[size];
    for (int i = 0; i < size; i++){
      pop[i] = new Dot(startPos, dotBrainSize, col);
    }
    goal = _goal;
    manualStop = false;
  }
  
  public void render(PVector cam){ // Draw all the dots
    fill(0, 0, 255);
    noStroke();
    ellipse(goal.x-cam.x, goal.y-cam.y, 5, 5);
    for (Dot d : pop) d.render(cam);
    for (Dot d : pop) if (d.prevBest) d.render(cam);
  }
  
  public void step(ArrayList<Wall> walls){ // Move all the dots
    for (Dot d : pop){
      d.step(goal, walls);
      if (d.prevBest && d.atGoal){
        manualStop = true;
      }
    }
  }
  
  public void reset(int mode, PVector to){ // Reset code
    float totalFitness = 0;
    for (Dot d : pop){ // Get total fitness
      totalFitness += d.getFitness(goal);
    }
    if (mode == 0){
      // Basic algorithm
      Dot[] newPop = new Dot[pop.length]; // New Population
      newPop[0] = getBestDot().getCopy(to); // Keep the best dot
      newPop[0].prevBest = true;
      for (int i = 1; i < newPop.length; i++){
        newPop[i] = getDotByFitness(totalFitness).getOffspring(to); // Get a weighted random dot's offspring
      }
      pop = newPop;
      manualStop = false;
    }else if (mode == 1){
      // NEAT based algorithm
      Dot[] newPop = new Dot[pop.length];
      newPop[0] = getBestDot().getCopy(to); // Keep the best dot
      newPop[0].prevBest = true;
      for (int i = 1; i < newPop.length; i++){
        Dot a, b; // Parents
        a = getDotByFitness(totalFitness).getCopy(to); // Get parents by weighted random number
        b = getDotByFitness(totalFitness).getCopy(to);
        Dot c = new Dot(to, a.brain.brain.length, a.c); // New Dot
        for (int j = 0; j < a.brain.brain.length; j++){
          c.brain.brain[j] = (random(1) < 0.5f) ? a.brain.brain[j] : b.brain.brain[j]; // ~50% from a, rest from b
        }
        c.evolve(); // Mutate the dot
        newPop[i] = c;
      }
      pop = newPop;
      manualStop = false;
    }else if (mode == 2){
      // Q-Learning
      // Not implemented
    }else{ // Default
      for (int i = 0; i < pop.length; i++){
        pop[i] = pop[i].getOffspring(to);
      }
      manualStop = false;
    }
  }
  
  public Dot getBestDot(){ // Get the best dot
    Dot best = pop[0];
    for (Dot d : pop){
      if (d.getFitness(goal) >= best.getFitness(goal)){
        best = d;
      }
    }
    return best;
  }
  
  public boolean allFinished(){ // All dots complete
    if (manualStop) return true;
    for (Dot d : pop){
      if (d.alive && !d.atGoal && !d.stopped) return false;
    }
    return true;
  }
  
  public Dot getDotByFitness(float totalFitness){ // Get a dot from the population based on a weighted random number
    float r = random(totalFitness);
    float runningFitness = 0;
    for (Dot d : pop){
      runningFitness += d.getFitness(goal);
      if (runningFitness > r){
        return d;
      }
    }
    return null;
  }
}
// NOT IMPLEMENTED

class QDot extends Dot{
  float learnRate = 0.9f;
  float discount = 0.9f;
  
  boolean exploiting = false;
  
  HashMap<QState, HashMap<PVector, Float>> qTable; // State (Action, Percieved Reward)
  QDot(PVector startPos, int c){
    super(startPos, 0, c);
    qTable = new HashMap<QState, HashMap<PVector, Float>>();
  }
  
  public void step(PVector goal, ArrayList<Wall> walls){
    if (!stopped && alive && !atGoal){
      QState state = getState(walls, pos, goal);
      if (!exploiting && random(1) < 0.1f) {
        // Exploration
        mov = PVector.random2D();
      }else{
        // Exploitation
        mov = getAction(state);
      }
      updateQ(state, mov, goal, walls);
      mom.add(mov);
      mom.limit(5);
      pos.add(mom);
      if (distance(pos, goal) < 5){
        atGoal = true;
      }
      for (Wall w : walls){
        if (w.collidingWith(pos)){
          alive = false;
          break;
        }
      }
      stopped = brain.atEnd();
      println(maxQ(state));
      //QVal = maxQ(state);
    }
    //QVal = 0;
  }
  
  public float getQ(QState state, PVector action){
    if (qTable.keySet().contains(state)){
      if (qTable.get(state).keySet().contains(action)){
        return qTable.get(state).get(action);
      }
    }
    return 0;
  }
  
  public float maxQ(QState state){
    ArrayList<PVector> actions = new ArrayList<PVector>();
    for (float theta = 0; theta < TWO_PI; theta += TWO_PI*0.01f){
      for (float r = 0.1f; r < 5; r += 0.1f){
        actions.add(fromPolar(theta, r));
      }
    }
    float maxQ = getQ(state, actions.get(0));
    for (PVector p : actions){
      maxQ = max(maxQ, getQ(state, p));
    }
    return maxQ;
  }
  
  public PVector getAction(QState state){
   ArrayList<PVector> actions = new ArrayList<PVector>();
    for (float theta = 0; theta < TWO_PI; theta += TWO_PI*0.01f){
      actions.add(fromPolar(theta, 5));
    }
    PVector action = actions.get(0);
    for (PVector p : actions){
      float currentQ = getQ(state, action);
      float testQ = getQ(state, p);
      if (testQ > currentQ){
        action = p;
      }else if (testQ == currentQ){
        action = (random(1) < 0.5f) ? p : action;
      }
    }
    return action;
  }
  
  public QState getState(ArrayList<Wall> walls, PVector pos, PVector goal){
    float[] sense = dists(pos, walls);
    float[] state = new float[]{distance(pos, goal), sense[0], sense[1], sense[2], sense[3]};
    return new QState(state);
  }
  
  public QState getNewState(PVector action, ArrayList<Wall> walls, PVector goal){
    PVector newPos = pos.copy().add(mom.copy()).add(action);
    return getState(walls, newPos, goal);
  }
  
  public float getR(QState state, PVector action, PVector goal){
    float[] s = state.getState();
    float dist = s[0];
    float uSense = s[1];
    float dSense = s[2];
    float lSense = s[3];
    float rSense = s[4];
    PVector act = mom.copy().add(action);
    uSense += act.y;
    dSense -= act.y;
    lSense += act.x;
    rSense -= act.x;
    
    print("Reward: ");
    if (uSense <= 0 || dSense <= 0 || lSense <= 0 || rSense <= 0){
      println(-5);
      return -5;
    }
    PVector newPos = pos.copy().add(act);
    if (distance(newPos, goal) < dist){
      println(1/distance(newPos, goal));
      return 1/distance(newPos, goal);
    }else if (distance(newPos, goal) > dist){
      println((-2)/dist);
      return (-2)/dist;
    }
    println(0);
    return 0;
  }
  
  public void updateQ(QState state, PVector action, PVector goal, ArrayList<Wall> walls){
    float newQ = getQ(state, action) + learnRate * (getR(state, action, goal) + discount * (maxQ(getNewState(action, walls, goal))) - getQ(state, action));
    println("new Q: " + newQ);
    if (qTable.keySet().contains(state)){
      HashMap<PVector, Float> subtable = qTable.get(state);
      PVector recAct = new PVector(floor(action.x/10)*10, floor(action.y/10)*10);
      subtable.put(recAct, newQ);
    }else{
      HashMap<PVector, Float> newSub = new HashMap<PVector, Float>();
      PVector recAct = new PVector(floor(action.x/10)*10, floor(action.y/10)*10);
      newSub.put(recAct, newQ);
      qTable.put(state, newSub);
    }
  }
  
  public void reset(PVector to){
    alive = true;
    pos = to.copy();
    mom = new PVector(0, 0);
    atGoal = false;
  }
}
// NOT IMPLEMENTED

class QState{
  float[] data;
  
  QState(float[] dta){
    data = new float[dta.length];
    for (int i = 0; i < dta.length; i++){
      data[i] = floor(10*dta[i])/10.0f; // Cut off anything after the first decimal point
    }
  }
  
  public boolean equals(QState q){
    for (int i = 0; i < data.length; i++){
      if (data[i] != q.data[i]) return false;
    }
    return true;
  }
  
  public float[] getState(){
    return data;
  }
}
public float distance(PVector a, PVector b){ // Cartesian distance (their dist() is wonky)
  return sqrt(pow(a.x-b.x, 2)+pow(a.y-b.y, 2));
}

public float[] dists(PVector pos, ArrayList<Wall> arena){ // NOT IMPLEMENTED (Senses for QLearning)
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

public boolean lineRect(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) { // NOT IMPLEMENTED Senses for QLearning
  boolean left =   lineLine(x1,y1,x2,y2, rx,ry,rx, ry+rh);
  boolean right =  lineLine(x1,y1,x2,y2, rx+rw,ry, rx+rw,ry+rh);
  boolean top =    lineLine(x1,y1,x2,y2, rx,ry, rx+rw,ry);
  boolean bottom = lineLine(x1,y1,x2,y2, rx,ry+rh, rx+rw,ry+rh);
  if (left || right || top || bottom) {
    return true;
  }
  return false;
}

public PVector[] lineRectPos(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) { // NOT IMPLEMENTED Senses for QLearning
  PVector left =   lineLinePos(x1,y1,x2,y2, rx,ry,rx, ry+rh);
  PVector right =  lineLinePos(x1,y1,x2,y2, rx+rw,ry, rx+rw,ry+rh);
  PVector top =    lineLinePos(x1,y1,x2,y2, rx,ry, rx+rw,ry);
  PVector bottom = lineLinePos(x1,y1,x2,y2, rx,ry+rh, rx+rw,ry+rh);
  return new PVector[]{top, bottom, left, right};
}

public boolean lineLine(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) { // NOT IMPLEMENTED Senses for QLearning
  float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
    return true;
  }
  return false;
}

public PVector lineLinePos(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) { // NOT IMPLEMENTED Senses for QLearning
  float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
  if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
    float intersectionX = x1 + (uA * (x2-x1));
    float intersectionY = y1 + (uA * (y2-y1));
    return new PVector(intersectionX, intersectionY);
  }
  return null;
}

public PVector fromPolar(float theta, float r){ // NOT IMPLEMENTED Movement for QLearning
  return new PVector(r*cos(theta), r*sin(theta));
}
class Wall{
  PVector pos, dim;
  
  Wall(PVector _pos, PVector _dim){
    pos = _pos.copy();
    dim = _dim.copy();
  }
  
  public void render(PVector cam){
    stroke(0);
    fill(100);
    rect(pos.x-cam.x, pos.y-cam.y, dim.x, dim.y);
  }
  
  public boolean collidingWith(PVector pt){
    if (pt.x >= pos.x && pt.x <= pos.x+dim.x) // Within x
      if (pt.y >= pos.y && pt.y <= pos.y+dim.y) // Within y
        return true;
    return false;
  }
}
  public void settings() {  size(1620, 700); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ISU" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
