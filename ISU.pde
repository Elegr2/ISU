ArrayList<Wall> walls;
Population basic, neat;
//QDot q;
int generation = 1;
PVector camera = new PVector(-50, -50);
//PVector qGoal = new PVector(1270, 125);
//float QVal;

void setup(){
  size(1620, 700);
  walls = new ArrayList<Wall>();
  setupBoundaries(); // Set the boundaries that differentiate the test zones
  addMazeAt(new PVector(0, 0)); // Add the maze
  addMazeAt(new PVector(510, 0));
  //addMazeAt(new PVector(1020, 0));
  basic = new Population(new PVector(250, 375), 200, 500, new PVector(250, 125), color(0));
  neat = new Population(new PVector(760, 375), 200, 500, new PVector(760, 125), color(255, 0, 0));
  //q = new QDot(new PVector(1270, 375), color(255, 0, 255));
}

void draw(){
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

void setupBoundaries(){ // Boundaries for the arena
  walls.add(new Wall(new PVector(500, 0), new PVector(10, 500)));
  walls.add(new Wall(new PVector(1010, 0), new PVector(10, 500)));
  walls.add(new Wall(new PVector(-10, -10), new PVector(1540, 10)));
  walls.add(new Wall(new PVector(-10, 500), new PVector(1540, 10)));
  walls.add(new Wall(new PVector(-10, 0), new PVector(10, 500)));
  walls.add(new Wall(new PVector(1520, 0), new PVector(10, 500)));
}

void addMazeAt(PVector tl){
  walls.add(new Wall(new PVector(tl.x, tl.y+245), new PVector(300, 10)));
  walls.add(new Wall(new PVector(tl.x+270, tl.y), new PVector(30, 100)));
  walls.add(new Wall(new PVector(tl.x+270, tl.y+115), new PVector(30, 115)));
  walls.add(new Wall(new PVector(tl.x+200, tl.y+115), new PVector(70, 5)));
  walls.add(new Wall(new PVector(tl.x+200, tl.y+120), new PVector(5, 70)));
}
