class Population{
  Dot[] pop;
  PVector goal;
  boolean manualStop;
  
  Population(PVector startPos, int size, int dotBrainSize, PVector _goal, color col){
    pop = new Dot[size];
    for (int i = 0; i < size; i++){
      pop[i] = new Dot(startPos, dotBrainSize, col);
    }
    goal = _goal;
    manualStop = false;
  }
  
  void render(PVector cam){ // Draw all the dots
    fill(0, 0, 255);
    noStroke();
    ellipse(goal.x-cam.x, goal.y-cam.y, 5, 5);
    for (Dot d : pop) d.render(cam);
    for (Dot d : pop) if (d.prevBest) d.render(cam);
  }
  
  void step(ArrayList<Wall> walls){ // Move all the dots
    for (Dot d : pop){
      d.step(goal, walls);
      if (d.prevBest && d.atGoal){
        manualStop = true;
      }
    }
  }
  
  void reset(int mode, PVector to){ // Reset code
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
          c.brain.brain[j] = (random(1) < 0.5) ? a.brain.brain[j] : b.brain.brain[j]; // ~50% from a, rest from b
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
  
  Dot getBestDot(){ // Get the best dot
    Dot best = pop[0];
    for (Dot d : pop){
      if (d.getFitness(goal) >= best.getFitness(goal)){
        best = d;
      }
    }
    return best;
  }
  
  boolean allFinished(){ // All dots complete
    if (manualStop) return true;
    for (Dot d : pop){
      if (d.alive && !d.atGoal && !d.stopped) return false;
    }
    return true;
  }
  
  Dot getDotByFitness(float totalFitness){ // Get a dot from the population based on a weighted random number
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
