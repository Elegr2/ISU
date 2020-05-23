class Dot{
  PVector pos, mom, mov; // Position, momentum, acceleration
  Brain brain;
  boolean alive, atGoal;
  boolean stopped;
  boolean prevBest;
  color c;
  
  Dot(PVector startPos, int brainSize, color col){ // Pretty standard contructor
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
  
  void step(PVector goal, ArrayList<Wall> walls){
    if (!stopped && alive && !atGoal){ // Make sure it can move
      mov = brain.get(); // get the next acceleration vector
      mom.add(mov); // Add acceleration to momentum
      mom.limit(5); // Limit momentum
      mom.mult(0.95); // Drag
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
  
  Dot getCopy(PVector copyTo){ // Copy the dot
    Dot copy = new Dot(copyTo, brain.brain.length, c);
    
    copy.brain = brain.getCopy();
    
    return copy;
  }
  
  Dot getOffspring(PVector to){ // Get an evolved offspring of the dot
    Dot offspring = getCopy(to);
    offspring.evolve();
    return offspring;
  }
  
  void evolve(){ // Just call evolve() on the brain.
    brain.evolve();
  }
  
  float getFitness(PVector goal){
    if (atGoal){
      return 2 + (1/brain.index); // At goal
    }
    return 1/distance(pos, goal); // Not at goal
  }
  
  void render(PVector cam){
    noStroke();
    fill(c);
    if (prevBest){
      ellipse(pos.x-cam.x, pos.y-cam.y, 5, 5);
    }else{
      ellipse(pos.x-cam.x, pos.y-cam.y, 3, 3);
    }
  }
}
