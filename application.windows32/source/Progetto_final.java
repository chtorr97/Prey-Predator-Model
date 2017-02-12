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

public class Progetto_final extends PApplet {

ArrayList<Vehicle> erb;
ArrayList<Vehicle> carn;
ArrayList<Vehicle> food;
ArrayList<PVector> data;

float ye, yc, pye, pyc, maxY;
float i;

int erbNum = 20, 
  carnNum = 4, 
  foodNum = 15, 
  erbLife = 2000, 
  carnLife = 1300,
  newErb = 3, 
  newCarn = 1, 
  offsetErb = 20, 
  offsetCarn = 3, 
  seed, 
  pFrameCount = 0, 
  currentFrameCount, 
  bestFrameCount = 0, 
  sizeCross, 
  hide;

boolean display = true, 
  restart = false;

PImage bear, deer, plant;

public void setup()
{
  seed = PApplet.parseInt(random(0, 100000));
  randomSeed(seed);
  frameRate(20);
   

  erb = new ArrayList<Vehicle>();
  carn = new ArrayList<Vehicle>();
  food = new ArrayList<Vehicle>();
  data = new ArrayList<PVector>();

  deer = loadImage("deer.png");
  bear = loadImage("bear.png");
  plant = loadImage("plant.png");

  for (int k = 0; k < erbNum; k++) {
    erb.add(new Vehicle(random(0, width), random(0, height), erbLife, 0, deer, 116, 245, 255));
  }

  for (int k = 0; k < carnNum; k++) {
    carn.add(new Vehicle(random(0, width), random(0, height), carnLife, 1, bear, 255, 0, 0));
  }

  for (int k = 0; k < foodNum; k++) {
    food.add(new Vehicle(random(0, width), random(0, height), erbLife, 2, plant, 0, 0, 0));
  }

  restart = false;
  sizeCross = 0;

  data.add(new PVector(erbNum, carnNum));
  background(0);
}

public void draw()
{
  if (restart) {
    pFrameCount = frameCount;
    if (currentFrameCount > bestFrameCount) {
      bestFrameCount = frameCount;
      println(seed + " last " + bestFrameCount + " with " + sizeCross + " crosses");
    }
    delay(3500);
    setup();
  }

  if (display) {
    background(0);

    strokeWeight(3);
    maxY = 0;
    hide = -1;
    for (int k = 0; k < data.size(); k++) {
      float x = data.get(k).x;
      float y = data.get(k).y;
      maxY = x > y ? (x > maxY ? x : maxY) : (y > maxY ? y : maxY);
    }

    i = data.size() / width;
    if (i == Float.NaN)
      i = 1;
    float index = 0;
    i++;
    for (int x = 0; x <= width; x++) {
      ye = 0;
      yc = 0;
      for (int k = PApplet.parseInt(index); k < PApplet.parseInt(index + i); k++) {
        if (k < data.size()) {
          ye += data.get(k).x;
          yc += data.get(k).y;
        }
      }
      ye /= i;
      yc /= i;
      index += i;
      if (x != 0) {
        stroke(116, 245, 255);
        line(x - 1, height - map(pye, 0, maxY, 0, height), x, height - map(ye, 0, maxY, 0, height));
        stroke(255, 0, 0);
        //if (abs(yc - pyc) < 25 * max(yc, pyc) / 100)
        line(x - 1, height - map(pyc, 0, maxY, 0, height), x, height - map(yc, 0, maxY, 0, height));
      }
      if ((ye == 0 && pye > 5) || (yc == 0 && pyc > 5))
        hide = x;
      pye = ye;
      pyc = yc;
    }
    if (hide != -1) {
      fill(0); 
      noStroke();
      rect(hide - 3, -5, width + 5, height + 25);
    }
  }

  for (Vehicle f : food) {
    fill(0, 255, 0);
    stroke(0);
    if (display)
      f.display();
    for (int k = 0; k < erb.size(); k++) {
      if (erb.get(k).intersects(f) && f.life > 0) {
        erb.get(k).life = erbLife;
        f.die();
        if (f.neighbourAmount(erb, 150) < 35) {
          for (int i = 0; i < newErb; i++) {
            erb.add(new Vehicle(random(erb.get(k).location.x - offsetErb, erb.get(k).location.x + offsetErb), 
              random(erb.get(k).location.y - offsetErb, erb.get(k).location.y + offsetErb), 
              erbLife, 0, deer, 116, 245, 255));
          }
        }
        f.location = new PVector(random(0, width), random(0, height));
        f.life = erbLife;
      }
    }
    strokeWeight(1);
  }

  for (int k = 0; k < erb.size(); k++) {
    for (int j = 0; j < carn.size(); j++) {
      if (erb.get(k).intersects(carn.get(j)) && erb.get(k).life > 0) {
        erb.get(k).die();
        carn.get(j).life = carnLife;
        if (erb.get(k).neighbourAmount(carn, 150) < 35) {
        for (int i = 0; i < newCarn; i++) {
          carn.add(new Vehicle(random(carn.get(j).location.x - offsetCarn, carn.get(j).location.x + offsetCarn), 
            random(carn.get(j).location.y - offsetCarn, carn.get(j).location.y + offsetCarn), 
            carnLife, 1, bear, 255, 0, 0));
        }
        }
      }
    }
  }

  for (int k = 0; k < erb.size(); k++)
  {
    if (erb.get(k).life <= 0)
      erb.remove(k);
  }
  for (int k = 0; k < carn.size(); k++)
  {
    if (carn.get(k).life <= 0)
      carn.remove(k);
  }

  for (int k = 0; k < erb.size(); k++) {
    erb.get(k).behave(food, carn);
    erb.get(k).separate(erb);
    erb.get(k).cohesion(erb, 0.8f);
    erb.get(k).checkEdges();
    erb.get(k).update();
    if (display)
      erb.get(k).display();
  }

  for (int k = 0; k < carn.size(); k++) {
    if (erb.size() != 0 &&
      carn.get(k).location.dist(new PVector(width / 2, height / 2)) < 1200)
      carn.get(k).seek(erb);
    else
      carn.get(k).seek(new PVector(width/2, height/2));
    carn.get(k).separate(carn);
    carn.get(k).cohesion(carn, 0.3f);
    carn.get(k).checkEdges();
    carn.get(k).update();
    if (display)
      carn.get(k).display();
  }

  data.add(new PVector(erb.size(), carn.size()));
  currentFrameCount = frameCount - pFrameCount;
  if (erb.size() == carn.size() && data.get(data.size()-2) != data.get(data.size() - 1))
    sizeCross++;

  if (display) {
    fill(255, 255, 255);
    textSize(15);
    textAlign(CENTER);
    noStroke();
    fill(255, 100);
    rect(width - 250, 15, 230, 70);
    textSize(25);
    fill(116, 245, 255);
    text("HERBIVORES: " + erb.size(), width - 135, 45);
    fill(255, 0, 0);
    text("CARNIVORES: " + carn.size(), width - 135, 75);
  }

  if (carn.size() == 0) {
   String s = erb.size() == 0 ? "MASS EXTINCTION" : "CARNIVORES WENT EXTINCT";
   fill(255, 50, 50);
   textSize(110);
   textAlign(CENTER);
   text(s, width/2, height/2);
   restart = true;
  }
}

public void mousePressed() {
  display = !display;
  background(0);
}
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
    r = 4.5f;
    maxspeed = tag == 0 ? 4 : 3.3f;
    maxforce = 0.1f;
    mirror = true;
    this.lifeTot = life;
    this.life = life;
    this.tag = tag;
    this.img = img;
    this.R = R;
    this.G = G;
    this.B = B;
  }

  public void update() {
    life--;
    velocity.add(acceleration);
    velocity.limit(maxspeed);
    location.add(velocity);
    acceleration.mult(0);
  }

  public void applyForce(PVector force) {
    acceleration.add(force);
  }

  public void seek(PVector target) {
    PVector desired = PVector.sub(target, location);
    desired.normalize();
    desired.mult(maxspeed);
    PVector steer = PVector.sub(desired, velocity);
    steer.limit(maxforce);
    applyForce(steer);
  }

  public void seek(Vehicle target) {
    PVector newPos = PVector.add(target.location, PVector.mult(target.velocity, 0.1f * target.location.dist(location)));
    seek(newPos);
  }

  public void seek(ArrayList<Vehicle> targets) {
    Vehicle target = closest(targets);
    seek(target);
  }

  public void evade(PVector pursuer) {
    PVector desired = PVector.mult(PVector.sub(pursuer, location), -1);
    desired.normalize();
    desired.mult(maxspeed);
    PVector steer = PVector.sub(desired, velocity);
    steer.limit(maxforce);
    applyForce(steer);
  }

  public void evade(Vehicle pursuer) {
    evade(pursuer.location);
  }

  public void evade(ArrayList<Vehicle> pursuers) {
    Vehicle pursuer = closest(pursuers);
    seek(pursuer);
  }

  public void behave(ArrayList<Vehicle> food, ArrayList<Vehicle> carn) {
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

  public void arrive(PVector target) {
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

  public void die() {
    life = 0;
  }

  public boolean dead() {
    return life <= 0;
  }

  public void separate (ArrayList<Vehicle> vehicles) {
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
      steer.limit(maxforce * 1.5f);
      applyForce(steer);
    }
  }

  public void cohesion (ArrayList<Vehicle> vehicles, float modifier) {
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

  public Vehicle closest(ArrayList<Vehicle> lst) {
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
  
  public int neighbourAmount(ArrayList<Vehicle> neighbours, int dist){
    int k = 0;
    for(Vehicle v : neighbours){
      if(v.location.dist(this.location) < dist)
        k++;
    }
    return k;
  }

  public void display() {
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

  public boolean intersects(Vehicle m)
  {
    boolean test;
    test = 10 < sqrt(pow(location.x - m.location.x, 2) + pow(location.y - m.location.y, 2));
    return !test;
  }

  public void checkEdges() {
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
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Progetto_final" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
