class Vehicle {

  PVector location;
  PVector velocity;
  PVector acceleration;
  float r;
  float maxforce;
  float maxspeed;
  float theta;
  boolean mirror;
  int R, G, B;
  int life, lifeTot;
  int tag;
  PImage img;

  Vehicle(float x, float y, int life, int tag, PImage img, int R, int G, int B) {
    acceleration = new PVector(0, 0);
    velocity = new PVector(0, 0);
    location = new PVector(x, y);
    r = 4.5;
    maxspeed = tag == 0 ? 4 : 3.3;
    maxforce = 0.1;
    mirror = true;
    this.lifeTot = life;
    this.life = life;
    this.tag = tag;
    this.img = img;
    this.R = R;
    this.G = G;
    this.B = B;
  }

  void update() {
    life--;
    velocity.add(acceleration);
    velocity.limit(maxspeed);
    location.add(velocity);
    acceleration.mult(0);
  }

  void applyForce(PVector force) {
    acceleration.add(force);
  }

  void seek(PVector target) {
    PVector desired = PVector.sub(target, location);
    desired.normalize();
    desired.mult(maxspeed);
    PVector steer = PVector.sub(desired, velocity);
    steer.limit(maxforce);
    applyForce(steer);
  }

  void seek(Vehicle target) {
    PVector newPos = PVector.add(target.location, PVector.mult(target.velocity, 0.1 * target.location.dist(location)));
    seek(newPos);
  }

  void seek(ArrayList<Vehicle> targets) {
    Vehicle target = closest(targets);
    seek(target);
  }

  void evade(PVector pursuer) {
    PVector desired = PVector.mult(PVector.sub(pursuer, location), -1);
    desired.normalize();
    desired.mult(maxspeed);
    PVector steer = PVector.sub(desired, velocity);
    steer.limit(maxforce);
    applyForce(steer);
  }

  void evade(Vehicle pursuer) {
    evade(pursuer.location);
  }

  void evade(ArrayList<Vehicle> pursuers) {
    Vehicle pursuer = closest(pursuers);
    seek(pursuer);
  }

  void behave(ArrayList<Vehicle> food, ArrayList<Vehicle> carn) {
    Vehicle closestFood = closest(food);
    if (carn.size() > 0) {
      Vehicle closestCarn = closest(carn);
      Vehicle closestCarnToFood = closestFood.closest(carn);
      if ((location.dist(closestFood.location) < location.dist(closestCarn.location) ||
        life < lifeTot*(1/2)) &&
        !(closestFood.location.dist(closestCarnToFood.location) < location.dist(closestFood.location))) {
        seek(closestFood);
      } else {
        evade(closestCarn);
      }
    } else {
      seek(closestFood);
    }
  }

  void arrive(PVector target) {
    PVector desired = PVector.sub(target, location);

    float d = desired.mag();
    desired.normalize();
    if (d < 100) {
      float m = map(d, 0, 100, 0, maxspeed);
      desired.mult(m);
    } else {
      desired.mult(maxspeed);
    }

    PVector steer = PVector.sub(desired, velocity);
    steer.limit(maxforce);
    applyForce(steer);
  }

  void die() {
    life = 0;
  }

  boolean dead() {
    return life <= 0;
  }

  void separate (ArrayList<Vehicle> vehicles) {
    float desiredSeparation = r * 8;
    PVector sum = new PVector();
    int count = 0;
    for (Vehicle other : vehicles) {
      float d = PVector.dist(location, other.location);
      if ((other != this) && (d < desiredSeparation)) {
        PVector diff = PVector.sub(location, other.location);
        diff.normalize();
        diff.div(d);
        sum.add(diff);
        count++;
      }
    }
    if (count > 0) {
      sum.div(count);
      sum.normalize();
      sum.mult(maxspeed);
      PVector steer = PVector.sub(sum, velocity);
      steer.limit(maxforce * 1.5);
      applyForce(steer);
    }
  }

  void cohesion (ArrayList<Vehicle> vehicles, float modifier) {
    float desiredCohesion = r * 15;
    PVector sum = new PVector();
    int count = 0;
    for (Vehicle other : vehicles) {
      float d = PVector.dist(location, other.location);
      if ((other != this) && d < desiredCohesion) {
        PVector diff = PVector.sub(other.location, location);
        diff.normalize();
        diff.div(d);
        sum.add(diff);
        count++;
      }
    }
    if (count > 0) {
      sum.div(count);
      sum.normalize();
      sum.mult(maxspeed);
      PVector steer = PVector.sub(sum, velocity);
      steer.limit(maxforce * modifier);
      applyForce(steer);
    }
  }

  Vehicle closest(ArrayList<Vehicle> lst) {
    Vehicle closest = null;
    float dist = 1999999999; //distance
    for (Vehicle v : lst) {
      if (pow(location.x - v.location.x, 2) + pow(location.y - v.location.y, 2) < dist) {
        dist = pow(location.x - v.location.x, 2) + pow(location.y - v.location.y, 2);
        closest = v;
      }
    }
    return closest;
  }
  
  int neighbourAmount(ArrayList<Vehicle> neighbours, int dist){
    int k = 0;
    for(Vehicle v : neighbours){
      if(v.location.dist(this.location) < dist)
        k++;
    }
    return k;
  }

  void display() {
    theta = velocity.heading() + PI/2;

    pushMatrix();
    translate(location.x, location.y);
    rotate(theta);
    imageMode(CENTER);
    if (theta > 0 && theta < PI){
      scale(-1, 1);
    }
    tint(255, map(life, 0, 255, 0, lifeTot));
      image(img, 0, 0, r * 8, r * 8);
    popMatrix();
  }

  boolean intersects(Vehicle m)
  {
    boolean test;
    test = 10 < sqrt(pow(location.x - m.location.x, 2) + pow(location.y - m.location.y, 2));
    return !test;
  }

  void checkEdges() {
    float offset = -300;
    if (location.x > width - offset) {
      location.x = offset;
    } else if (location.x < offset) {
      location.x = width - offset;
    }

    if (location.y > height - offset) {
      location.y = offset;
    } else if (location.y < offset) {
      location.y = height - offset;
    }
  }
}