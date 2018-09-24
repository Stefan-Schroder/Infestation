# Infestation

Zombie "tower defence" game made for CSC2003 assignment.

## Running

To run this, go into the base folder.

Windows:
'''
gradlew.bat desktop:run
'''

Linux:
'''
./gradlew desktop:run
'''

### Prerequisites

This project requires Java 8 to run the lambda expressions.
Some gradle files will be downloaded on launch if you do not alreay have them.

## Need to know

There are still alot of features not yet implemented in the game.
Thus far, the main features included are:
* Particle system
    * This runs the AI (zombies)
* Post processing shaders (OpenGL)
* Random map generation
* Some weapon mechanics:
    * Implemented:
        * Weapon damage
        * Weapon terrain damage
        * Weapon drop delay

        * Turret
        * Cluster bombs
        * Large explosives

    * Not implemented:
        * No animations
        * No sound
        * No clear weapon indication

Less obvious
* Quad tree particle manager
* Zombie texture 'animation'
* Particle motor behaviour (AI)

## How to play

Currently the game is in early development, so only basic features are available.
The weapon system can be cylced through with tab, and is indecated on the bottom left.

* 1: turret
* 2: large bomb
* 3: cluster bomb

### Controls:
LMB will drop a bomb, or shoot turret at currently pointed location.
TAB will cycle through the currently selected weapon