class Wall{
  PVector pos, dim;
  
  Wall(PVector _pos, PVector _dim){
    pos = _pos.copy();
    dim = _dim.copy();
  }
  
  void render(PVector cam){
    stroke(0);
    fill(100);
    rect(pos.x-cam.x, pos.y-cam.y, dim.x, dim.y);
  }
  
  boolean collidingWith(PVector pt){
    if (pt.x >= pos.x && pt.x <= pos.x+dim.x) // Within x
      if (pt.y >= pos.y && pt.y <= pos.y+dim.y) // Within y
        return true;
    return false;
  }
}
