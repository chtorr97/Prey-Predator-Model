
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

void setup()
{
  seed = int(random(0, 100000));
  randomSeed(seed);
  frameRate(20);
  fullScreen(); 

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

void draw()
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
      for (int k = int(index); k < int(index + i); k++) {
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
    erb.get(k).cohesion(erb, 0.8);
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
    carn.get(k).cohesion(carn, 0.3);
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

void mousePressed() {
  display = !display;
  background(0);
}