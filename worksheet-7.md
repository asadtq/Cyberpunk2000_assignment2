# COMP2000 Worksheet 1 — Mid-Semester Submission

**Student name:**
Asad Tariq

**Student ID:**
43171380

**GitHub repo URL:** (your own fork of your team's repository, not your team's URL)
https://github.com/newdok23/Cyberpunk2000/tree/newbranch
---

## 1. Version Control

**1.1.** Paste the first 10 lines of the output of `git log --graph --oneline --all` from your repository:

```

* 3459078 (HEAD -> newbranch) Queen code refactor and clean up. Simulation stop implementation
* addad3f queen code fix
* 7c8427f refactor and fix worker speed
* 175ac7b working worker code.
* db51021 refactor main and add rudementary worker code
* c864b6d Update Main.java
| * 1f6f1fd (origin/main, origin/HEAD, main) Added and exception try /catch block
| * a91524e (origin/pheromone-visibility) Adjusted the graphics
| * 24ea717 Workers were getting stuck after loosing trail They will now wander randomly in search of new one left by the scouts
| * 1aec7de Added pheromone decas () so that worker ants wouldnt keep going to same location Still need fixing the bug where worker ant wont disappear


```

**1.2.** Describe your workflow. Did you use branches? Pull requests?

Main quick/ bug fixing edits were made by pulling origin and pushing when done. Later on we used pull requests in seperate branches and merged once approved by everyone else.



**1.3.** Estimate the percentage of commits you contributed relative to the total in your repository.
15% to main
100% to my branch




---

## 2. Program Design

Don't forget to submit a pdf file of your program design along with this file.

**2.1.** List every class in your project and write 1–2 sentences describing its responsibility.

Ant: The superclass defining base characteristics like health, stamina, and coordinate positions for all ant entities

AntBehaviour: An interface for the standard movement, health, and stamina methods required for any ant object.

Main: The core loop and entry point of the simulation, responsible for grid updates, UI rendering, timer management, and food spawning.

Pheromone: A generic class that stores a coordinate point and a strength value, along with  a decay method to reduce its strength over time

Queen: subclass of Ant that acts as the colony manager, expending stamina to spawn Scouts or Workers, monitoring return timers, and executing emergency panic spawns if the colony stalls.

QueenDeadException: A custom exception that halts the simulation when the Queen's stamina reaches zero.

Scout: A subclass of Ant that wanders the map randomly to locate food and drops a pheromone trail on its return journey to the colony

Worker: A subclass of Ant that follows decaying pheromone trails outward, picks up food blocks, and returns them to the colony to restore the Queen's stamina




**2.2.** Identify any inheritance relationships. For each parent–child pair, list what the child inherits and what it overrides.
Parent Ant / Children Queen, Scout, Worker: 
    The subclasses inherit the variables for health, stamina, alive status, and grid position. The subclasses dont override parent methods, but rather extend functionality with unique behaviors (e.g., Scout.wander, Worker.followTrail, Queen.prodWorker)

Interface AntBehaviour / Parent Ant: 
    Ant implements AntBehaviour, providing the concrete code for inherited methods like getHealth(), die()



**2.3.** Pick the class that you think has the best design. Explain why.

The Pheromone class has the strongest design as it uses Java Generics to future-proof the strength variable. It encapsulates its own decay logic, casting the generic Number to a float to calculate a 0.99x reduction before updating its state



**2.4.** Paste one code snippet that demonstrates your use of polymorphism or encapsulation.  Include an explanation of _how_ this demonstrates polymorphim or encapsulation.  Give a reference to a provided reading that talks about this type of polymorphism or encapsulation.

public void setHealth(int health) {this.health = health;}
    public int getHealth() { return health;}
    public void setStamina(int stamina) {this.stamina = stamina;}
    public int getStamina() {return stamina;}



---

## 3. Generics and Exceptions

**3.1.** List every place your code uses generics (e.g. `ArrayList<Actor>`, `Optional<Cell>`, `HashMap<String, Team>`). If you deliberately used none, explain why.


List<Worker> and List<Scout> are utilized in Main.java and Queen.java to track active ant populations  
Pheromone<Float>[][] manages the x and y tracking grid in Main.java
Pheromone<T Number extends> bounds the type parameter at the class definition
List<Pheromone<Float>> is initialized as a trail tracker in Worker.java


**3.2.** List every place your code handles exceptions (try/catch, throws, custom exception classes). What error is each protecting against?

ArrayIndexOutOfBoundsException: Caught in a try/catch block in Worker.java to prevent the simulation from crashing when a worker attempts to scan for pheromones outside the valid map bounds.  

QueenDeadException: Thrown by Queen.java when her stamina depletes and caught by Main.java to trigger a safe stopGame = true state. 




**3.3.** Paste a code snippet showing either a generic class/method or a try/catch block.


            for (int i = 0; i < 4; i++) {
            try {
                int checkX = cx + dirs[i][0];
                int checkY = cy + dirs[i][1];
                //if this tile isnt the same as the last one we stood on. 
                if (checkX == lastX && checkY == lastY) {
                    continue; 
                }
                
                if (Main.pheromoneGrid[checkX][checkY] != null) {
                    //generic type requirement
                    float str = ((Number) Main.pheromoneGrid[checkX][checkY].getStrength()).floatValue();
                    if (str < best && str > 0.01f) {
                        best = str;
                        nextX = checkX;
                        nextY = checkY;
                    }
                }
            } 
            catch (ArrayIndexOutOfBoundsException e) {
                // try/catch requirement
            }
        }


---

## 4. Log Book

Don't forget to submit an electronic version of your logbook.

**4.1.** Which week's activity taught you the most? What did you learn?
Week 4 taught me the most as it made our team to move from abstract concepts into pracitcal Object-Oriented programming design.
Planning class hierarchies and debating class responsibilities is often the most critical phase of software development.

Specifically, deciding to elevate Ant to a superclass to hold shared state variables, like health and stamina, outlined how  practical value inheritance is in programming. Furthermore, debating with my team whether findResource() and pheromone mechanics belonged in a general Explore branch  inside the Scout subclass taught me a lot about  encapsulation. It helped me realize that a subclass should only possess the specific behaviors relevant to its exact role, preventing bloated classes and keeping the overall architecture clean

---

## 5. Uniqueness and Creativity

**5.1.** List everything you added to the project that was not part of the in-class activities.

A centralized queenBrain simulation manager that actively monitors returning worker and scout timers
An panic-spawn system that forces the Queen to spend remaining stamina on emergency scouts if no ants return within 300 ticks (PANIC_LIMIT)
Position-memory tracking (lastX and lastY) for worker ants to prevent infinite loop pathfinding errors

**5.2.** Which feature required the most independent research or problem-solving? What did you learn from it?
Implementing the worker pathfinding logic. Initially, workers would get caught in infinite loops, stepping back and forth between the two freshest pheromone tiles. I learned how to implement a position-memory state (lastX and lastY) to force the entity to evaluate new tiles and ignore its previous footprint

**5.3.** Paste one code snippet that you are especially proud of. Explain why it goes beyond what was done in class.

if (noScoutTimer > PANIC_LIMIT) {
            System.out.println("Queen: no scouts have come home, must have died. Making a new one.");

            // new scout
            Scout backupScout = theQueen.prodScout();
            if (backupScout != null) {
            
                scoutList.add(backupScout);
            }
            noScoutTimer = 0; // reset so she doesnt spam a new scout every tick after this
        }