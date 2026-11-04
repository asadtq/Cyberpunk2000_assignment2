# COMP2000 Worksheet 2 — Final Submission

**Student name:**
Asad Tariq
**GitHub repo URL:** (your own fork of your team's repository, not your team's URL)
https://github.com/asadtq/Cyberpunk2000_assignment2
---

## 1. Design Patterns

**1.1.** Which design pattern(s) did you implement? Name them and state where each appears in your codebase.


I implemented the Factory pattern in AntFactory.java, with the AntType enum in AntType.java.  
Every ant in the simulation is created through it. There are no new Scout, new Worker or new Spider calls anywhere in the code except inside AntFactory.
Its called from:

Queen.java: 
- prodWorker() 
- prodScout()
Main.java:
- initGrid()
    scouts.add((Scout) AntFactory.create(AntType.SCOUT));
    scouts.add((Scout) AntFactory.create(AntType.SCOUT));
    scouts.add((Scout) AntFactory.create(AntType.SCOUT));

    // add Spider
    spider = (Spider) AntFactory.create(AntType.SPIDER);



**1.2.** For each pattern, paste a code snippet showing the key interaction between participants.

// AntFactory.java: the only place that calls "new" on an ant
static Ant create(AntType type) {
    switch (type) {
        case SCOUT:
            return new Scout(100, 100);
        case WORKER:
            return new Worker(100, 100);
        case SPIDER:
            return new Spider(100, 100);
        default:
            return null; // unknown type
    }
}

// Queen.java: asks the factory instead of calling new
Worker worker = (Worker) AntFactory.create(AntType.WORKER);

// Main.java, initGrid()
spider = (Spider) AntFactory.create(AntType.SPIDER);






**1.3.** For each pattern, explain why you chose it over an alternative. What trade-off did you make?

Before implemeing the factory, every object was written in several different files. 
I chose a Factory so ant creation can reside in one place, this allowed for easy creation of objects without having to search through 600 lines in main.java. 
The process of adding a new ant, you can assign one value to AntType and one case in AntFactory, and none of the code that asks for ants has to change. Changing the ants variables at the start (like its health) is an easy change as well.

I considered two alternatives:
Plain new calls everywhere. This is simpler and doesnt need any extra classes, but code for one feature gets spread over many files, so every change means editing them.

An Abstract Factory. This would be more flexible, but I only need three ant types and one way of creating them, so it would complicate things by adding useless classes that would blaot the project.

The trade-off is that create() returns the base type Ant, so callers have to cast (for example (Worker)). That is less type-safe than new Worker(), because a wrong cast would only fail when the program runs. I also added an extra class and enum to maintain. I decided that was worth it for having creation in one place.


---

## 2. Streams / Lambdas / Parallelism

**2.1.** List every file in your project that uses lambda expressions or the Streams API.
Main.java:
scouts.removeIf(a -> !a.isAlive()) 
    and the same for workers, which removes ants the spider has eaten

IntStream.range(0, WIDTH).parallel().forEach(x -> { ... }), 
    the parallel pheromone decay
countFood() 
    as a stream (Arrays.stream, flatMap, filter, count)
drawStats() 
    with the groupingBy and partitioningBy collectors, plus a stream that counts the trail cells
lambda e -> { update(); panel.repaint(); } 
    the timer that runs game loop
Spider.java:
allAnts() 
    joins the scouts and workers into one stream
huntStep() 
    uses a stream with lambdas to find the closest living ant in sight
lbite() 
    uses a stream to find a living ant on the spider's cell




**2.2.** For the most sophisticated use, paste the code and explain: what does it do, and why was a stream/lambda better than a loop?
---
// drawStats() in Main.java
Map<String, Long> countByType = allAnts.stream()
    .collect(Collectors.groupingBy(a -> a.getClass().getSimpleName(), Collectors.counting()));

Map<Boolean, Long> fleeingCount = allAnts.stream()
    .collect(Collectors.partitioningBy(a -> a.isFleeing(), Collectors.counting()));
---
groupingBy sorts ants by type ({Scout=3, Worker=7}), while partitioningBy divides them by fleeing status ("Panicking"). Instead of traditional loops needing individual tally variables and conditional checks, collectors process new ant categories automatically using single statements.

Stream filtering in Spider.huntStep() simplifies target acquisition:
---
Optional<Ant> closest = allAnts(scouts, workers)
    .filter(Ant::isAlive)
    .filter(ant -> ant.getPosition().distance(position) <= SIGHT)
    .min(Comparator.comparingDouble(ant -> ant.getPosition().distance(position)));
---

This approach isolates living targets within range and grabs the nearest prey, skipping manual tracking logic that standard loops require.



**2.3.** Did you use parallel streams, custom collectors, or the Fork/Join framework? If yes, how did you ensure thread safety? If no, where could parallelism be introduced?

I used a parallel stream for the pheromone decay in main.java, in the update() method.
--
IntStream.range(0, WIDTH).parallel().forEach(x -> {
    for (int y = 0; y < HEIGHT; y++) {
        // decay pheromoneGrid[x][y] and dangerGrid[x][y]
    }
});
--
Without manually invoking Fork/Join or writing custom collectors, this routine executes via javas built-in ForkJoinPool under the hood.

Thread safety relies on four factors:
- Column isolation assigns each thread its own x coordinate, avoiding shared mutable state.
- Grid fading runs strictly after ant movement completes, preventing concurrent reads or writes.
- Swing Timer processes updating and rendering sequentially on one thread to prevent overlap.
- The forEach operation blocks further progress until all column tasks complete.

The main race condition involves Pheromone.decay(), where non-atomic read-modify-write steps on identical cells risk lost updates. Restricting threads to distinct columns eliminates this conflict while maintaining exact equivalence to standard sequential output.

On a 20 x 20 grid, parallel execution yields no speed advantages over standard loops because thread management overhead cancels out efficiency gains. Scale expansion is required to see performance improvements. 
Potential multi-threading additions include dividing the environment into regional threads (requiring checks for boundary-crossing ants) or offloading simulation ticks from the GUI rendering queue.


---

## 3. Uniqueness and Creativity

**3.1.** What did you add to your simulation that went beyond the in-class activities? Describe the feature and what inspired it.

- A spider predator (Spider.java, extends Ant). It chases the closest ant it can see, eats one, then is full for a while and just wanders. It cannot enter the area around the colony, so it cannot camp the spawn point.
- Danger pheromone. A second pheromone grid, dangerGrid, using the same generic Pheromone<Float> class as the food trails. Every kill leaves a 3x3 patch of it (strongest in the middle) that fades more slowly than the food trails, using a new Pheromone.decay(float rate). It is drawn in orange.
- Panic behaviour. An ant that steps into a danger patch turns around and runs back the way it came for a couple of seconds. The code is in the base class Ant, so scouts and workers share it.
- An AntFactory that creates all the ants .
- Live stats on screen using streams and collectors, and parallel pheromone decay .
- Bug fixes to the original colony: workers now check 8 directions instead of 4 (scouts lay diagonal trails), stuck workers actually move (the code moved them and then moved them straight back), and the queen ages more slowly so the colony lives long enough for workers to bring food back.
- A refactor: I moved the ant movement logic out of Queen into Main, because the queen does not control the ants.

The inspiration was real ants, which react to the dead body of a nestmate. I wanted a threat that made the pheromone trails matter, so the colony has something to react to.



**3.2.** Paste a code snippet or describe a design decision that demonstrates emergent behaviour, non-trivial complexity, or a genuinely surprising outcome.

// Spider.java: every kill marks a 3x3 danger patch
Main.dangerGrid[x][y] = new Pheromone<>(new Point(x, y), strength);

// Ant.java: any ant that steps into the patch runs back the way it came
public void checkForDanger(int oldX, int oldY) {
    if (fleeing) return;
    Point p = getPosition();
    if (Main.dangerGrid[p.x][p.y] != null) {
        fleeing = true;
        fleeTimer = FLEE_TICKS;
        fleeDx = oldX - p.x;
        fleeDy = oldY - p.y;
    }
}
Nothing in the code tells the ants where the spider is or tells the colony what to do. The behaviour comes from three simple rules combined: the spider kills ants, kills leave danger patches that fade slowly, and ants that step into a patch flee. When the spider hunts near a food trail, the ants on that trail get scattered or killed, the trail weakens as they stop refreshing it, and the queen's panic timers eventually spawn replacements. Sometimes the colony recovers and sometimes the disruption starves the queen, and I don't script either outcome


**3.3.** If you had another 6 weeks, what would you add or change?

Soldier ants that hunt the spider, added as a new case in AntFactory, so the colony can fight back.

Walls the user can draw with the mouse while the simulation runs, so ants and the spider have to route around obstacles and trails bend.

Finish the "ant reports a death to the queen" idea (the reportingDeath flag exists in Worker but nothing triggers it yet).

Split the grid into regions and simulate each on its own thread, with proper handling for ants crossing region borders, and measure the speedup on a larger world. Maybe even make random generated worlds.

Replace the stat text with a population chart over time, using a custom Collector.

Reduce how much Main does by moving the drawing code into its own class.



---

## 4. Log Book and Final Reflection

**4.1.** Attach or link your log book entries for Weeks 7–13.

Week7;
This week, I resolved compilation errors in the existing code and expanded the basic scout class. The compilation errors included lack of standard library imports such as “java.awt.Point” and changing of variable names. I pushed this version of the project to the main branch so that the project was running at its lowest form. Understanding code you didn’t write is a challenge and adhering to different programming standards is a unique experience. 
Reflection;
I expanded upon the Scout class and added a new method “wander()” and an encapsulated Point coordinate state. The scouter ant can wander across the map in random directions by subtracting 1 from a randomised number clamped at 3. This results in either a forward or backward motion, or no motion at all. In addition, there is basic collision detection which checks if the ants position on the grid is tagged as “food” it will delete that cell and set a Boolean flag to let the worker ants know that food is found. These changes were made to my branch of the project; I merged my code once I got confirmation from my classmates. There were no merge conflicts.  Programming this project is akin to game development and shifting focus to UI is a new experience for me. 
Future additions to the project include finalising scout behaviour and begin working on worker ants behaviour to follow the trail.

Week 8;
This week was mostly about exploring how Java uses design patterns like Decorator, Iterator, and State to solve common architectural problems, which all tie back to the Open/Closed Principle. The Decorator pattern felt a little unintuitive at first, but once I understood that it wraps a component to add responsibilities dynamically without modifying the underlying class, it made perfect sense. The State pattern was also a genuinely interesting concept to explore since it lets an object alter its behavior based on its internal state, making it look like it changes class entirely.

Week 9;
This week was about behavior parameterization and lambda expressions. I struggled with the concept of passing an executable block of code as an argument instead of just raw data. During our EPIC "Code Golf" activity, where we had to solve list manipulation puzzles using the fewest characters possible, my first instinct was to write out massive, clunky for loops to filter or sort the data.   
Seeing a massive loop get reduced to a single, readable line like;
words.removeIf(w -> w.length() < 3)
or using method references seems like to could be helpful to our project. After watching some YouTube videos for understanding, I understand that lambdas allow you to pass algorithms directly, effectively replacing the need to write entire concrete Strategy classes for simple tasks.   Moving into the next weeks, I am going to actively look for places where I have used bulky if-else chains and replace them with clean lambda expressions. 

Week 10;
Learned how to use the Java Streams API to process collections through pipelines consisting of source, intermediate, and terminal operations. Practised replacing loops with operations such as filter, map, and reduce, while evaluating streams' readability, performance, and correctness.
Reflection
This week focused on processing collections declaratively instead of using traditional loops. Initially, stream pipelines felt complex, particularly when tracking data through multiple intermediate operations.
Understanding lazy evaluation during practical activities helped clarify how streams work. I learned that intermediate operations are deferred until a terminal operation is called, allowing operations like filter and map to simplify data transformations.
Next, I plan to identify loops in my project that could be refactored into streams. This could improve readability and reduce code repetition, particularly when traversing grids or tracking entities in the simulation.

Week 11;

This week was about collecting. We covered toList, toSet and toMap, groupingBy for categorising data, summarizingDouble for min, max and average, and writing a custom Collector with Collector.of(). For my ant simulation I used groupingBy with counting() to count the ants by type, and partitioningBy to count how many ants are currently panicking. Both show up in the stats text on screen.

**Reflection**
One thing I noticed is that groupingBy leaves out a group that is empty, so I had to use getOrDefault to avoid a missing Scout or Worker count, whereas partitioningBy always gives both true and false. I didn't get to summarizingDouble or a custom Collector. A stamina summary or a custom collector for a population chart is what I'd try next.

## Week 12
This week was about parallelism. We covered threads, synchronized blocks and locks, parallel streams, race conditions and the ForkJoinPool. I made the pheromone decay a parallel stream, with each thread handling its own column of the grid.
**Reflection**
The race condition I had to avoid is that decay and the ants share the same grids, so decay has to run after the ants have moved, or an ant could read a cell while a thread is changing it. I expect the parallel version won't be faster on a 20x20 grid, because each column is so little work that the thread overhead costs more than it saves. The main thing I learned is that parallel code is only a good fit when the pieces are independent, and the ants aren't, because they share the grids, so decay was the safe place to start.


Week13:

In Week 13, I presented my ant colony simulator showcase to the class. I demonstrated how scouts explore food trails while workers deliver food back to sustain the queen. I introduced my new predator addition: a pink spider that hunts ants and leaves an orange danger pheromone grid, causing ants to panic and flee. I also walked through key bug fixes and refactored the ant movement logic out of the Queen class.   
During the code walkthrough, I highlighted:
Design Patterns: AntFactory.java implementing the Factory pattern for extensible ant creation.   Inheritance: The flee() method in Ant.java shared across sub-classes. 
  Lambdas & Concurrency: Utilizing removeIf(a -> !a.isAlive()) in Main.java to safely remove dead ants without incurring a ConcurrentModificationException, along with a Swing Timer lambda (e -> { update(); panel.repaint(); }).   
During the peer showcase, another student's presentation caught my attention for its unique visual mechanics. His simulation featured a directional cone sweeping across the screen, which detected and erased target symbols whenever they fell inside its field of view. To keep track of which symbols to remove first, he used a heapsort algorithm to quickly rank them based on how close they were to the cone. It was a really neat idea seeing a sorting algorithm used live to handle visual clean-up and dynamic target tracking on screen.






**4.2.** Reflect on the semester:

- What was the most important concept you learned?
Where code belongs. Putting the flee behaviour in the base class Ant meant scouts and workers share it, so I wrote it once. Moving the ant movement out of Queen into Main showed me how a class that does everything gets hard to change. I also learned that parallel code is not automatically faster: on a small grid the thread overhead is more than the work
And also to not procrastinate.

- What design trade-off did you face, and how did you resolve it?
I had to decide whether to copy the flee code into both Scout and Worker or put it in Ant. Copying was quicker, but any fix would need doing twice, so I put it in Ant and kept a small override in Worker for its trail memory. With the factory, I chose to accept an extra class and a cast on every call in return for having ant creation in one place.

- If you started over, what would you do differently?
I would plan which class is responsible for what before writing code, so the movement logic never ended up in Queen and had to be moved later. I would also build the window and the stats display earlier so I could see problems sooner, because the colony was dying within about ten seconds because of bugs I only spotted by watching it. And I would commit in smaller pieces more regularly.

- What feedback from the Week 7 submission did you act on?

I lost marks in Week 7 because I submitted a pull request link instead of the URL of my own fork. For this submission I forked the team repository to my own account and put the plain repository URL at the top of this worksheet.




