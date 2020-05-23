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
  
  PVector get(){ // Get the next vector, and increment index
    index++;
    if (index >= brain.length){ // If you've reached the end of the brain
      index--; // Step back
      return new PVector(0, 0); // Do not accelerate
    }
    return brain[index-1];
  }
  
  boolean atEnd(){ // Run through all the vectors
    return index == brain.length-1;
  }
  
  void evolve(){
    for (int i = 0; i < brain.length; i++){
      if (random(1) < 0.01){ // Randomly replace ~1% of the vectors with random new vectors
        brain[i] = PVector.random2D();
      }
    }
  }
  
  Brain getCopy(){
    Brain copy = new Brain(brain.length); // Create a copy
    
    for (int i = 0; i < brain.length; i++){ // Copy values
      copy.brain[i] = brain[i].copy();
    }
    
    return copy;
  }
}
