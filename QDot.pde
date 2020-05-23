// NOT IMPLEMENTED

class QDot extends Dot{
  float learnRate = 0.9;
  float discount = 0.9;
  
  boolean exploiting = false;
  
  HashMap<QState, HashMap<PVector, Float>> qTable; // State (Action, Percieved Reward)
  QDot(PVector startPos, color c){
    super(startPos, 0, c);
    qTable = new HashMap<QState, HashMap<PVector, Float>>();
  }
  
  void step(PVector goal, ArrayList<Wall> walls){
    if (!stopped && alive && !atGoal){
      QState state = getState(walls, pos, goal);
      if (!exploiting && random(1) < 0.1) {
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
  
  float getQ(QState state, PVector action){
    if (qTable.keySet().contains(state)){
      if (qTable.get(state).keySet().contains(action)){
        return qTable.get(state).get(action);
      }
    }
    return 0;
  }
  
  float maxQ(QState state){
    ArrayList<PVector> actions = new ArrayList<PVector>();
    for (float theta = 0; theta < TWO_PI; theta += TWO_PI*0.01){
      for (float r = 0.1; r < 5; r += 0.1){
        actions.add(fromPolar(theta, r));
      }
    }
    float maxQ = getQ(state, actions.get(0));
    for (PVector p : actions){
      maxQ = max(maxQ, getQ(state, p));
    }
    return maxQ;
  }
  
  PVector getAction(QState state){
   ArrayList<PVector> actions = new ArrayList<PVector>();
    for (float theta = 0; theta < TWO_PI; theta += TWO_PI*0.01){
      actions.add(fromPolar(theta, 5));
    }
    PVector action = actions.get(0);
    for (PVector p : actions){
      float currentQ = getQ(state, action);
      float testQ = getQ(state, p);
      if (testQ > currentQ){
        action = p;
      }else if (testQ == currentQ){
        action = (random(1) < 0.5) ? p : action;
      }
    }
    return action;
  }
  
  QState getState(ArrayList<Wall> walls, PVector pos, PVector goal){
    float[] sense = dists(pos, walls);
    float[] state = new float[]{distance(pos, goal), sense[0], sense[1], sense[2], sense[3]};
    return new QState(state);
  }
  
  QState getNewState(PVector action, ArrayList<Wall> walls, PVector goal){
    PVector newPos = pos.copy().add(mom.copy()).add(action);
    return getState(walls, newPos, goal);
  }
  
  float getR(QState state, PVector action, PVector goal){
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
  
  void updateQ(QState state, PVector action, PVector goal, ArrayList<Wall> walls){
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
  
  void reset(PVector to){
    alive = true;
    pos = to.copy();
    mom = new PVector(0, 0);
    atGoal = false;
  }
}
